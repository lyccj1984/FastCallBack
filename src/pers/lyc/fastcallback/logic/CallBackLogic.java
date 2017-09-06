package pers.lyc.fastcallback.logic;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSONObject;
import ch.qos.logback.classic.Logger;
import pers.lyc.fastcallback.common.HttpUnit;
import pers.lyc.fastcallback.model.CallBackModel;

class CallBackLogic extends Observable implements Runnable {

	private static Logger logger = (Logger) LoggerFactory.getLogger(CallBackLogic.class); // 日志记录

	@SuppressWarnings("rawtypes")
	Set<CallBackModel> callbackCache = new CopyOnWriteArraySet<CallBackModel>();
	int[] sendtime;
	String rusult;
	long sleepTime=5; //设置多长时间运行一次，发送数据 单位为秒

	public long getSleepTime() {
		return sleepTime;
	}

	public void setSleepTime(long sleepTime) {
		this.sleepTime = sleepTime;
	}

	public boolean addResultKV(String result) {
		if ((!result.equals(null) || result.equals(""))) {
			this.rusult = result;
			return true;
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	public boolean addCallBackModel(CallBackModel callBackModel) {
		if (callBackModel != null) {
			System.out.println(callbackCache.size());
			System.out.println("发送数据"+callBackModel.getCallbackdata()+"已添加到发送缓存");
			return callbackCache.add(callBackModel);
		}
		return false;
	}

	public boolean romveCallBackModel(@SuppressWarnings("rawtypes") CallBackModel cm) {
		return callbackCache.remove(cm);
	}

	/***
	 * 构造函数，传入的是发送时间隔，单位为秒，以能被5整除
	 * 
	 * @param senddata
	 */
	public CallBackLogic(int... senddata) {
		super();
		sendtime = new int[senddata.length];

		for (int i = 0; i < sendtime.length; i++) {
			sendtime[i] = senddata[i];
		}

	}

	@Override
	public void run() {
		Date datetime = new Date();
		while (true) {

			try {
				sendData(datetime);
				TimeUnit.SECONDS.sleep(sleepTime);
				System.out.println("我是发送数据线程====当前缓存中的数据为：" + this.callbackCache.size());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				logger.error("【计时线程】出错:" + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private synchronized void sendData(Date datatime) {
		for (CallBackModel callBackModel : callbackCache) {
			if (getsendcount(callBackModel)) {// 判断发送次数是否已超过设定次数
				// 根据时间判断是否可以发送数据
				try {
					if (isDateTime(callBackModel.getSendDate(), datatime)) {// 判断当前数据是否可以发
						String json = httpSendData(callBackModel);
						if (json != null) {
							if (this.checkResult(json)) {
								this.callbackCache.remove(callBackModel);
								System.out.println("发送完数据==当前缓存中条数为==" + this.callbackCache.size() + "发送数据次数为：="
										+ callBackModel.getSendCount());// 开始发送数据
								setChanged();// 改变事件状态
								this.notifyObservers(callBackModel);// 通知观察者发送数据
							} else {
								this.errorHandle(callBackModel);
							}
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					logger.error("【发送数据】发送数据出错" + e.getMessage());
					e.printStackTrace();
				}

			}
		}
	}

	private boolean checkResult(String json) {
		if (json != null && this.rusult != null) {
			if (json.contains(this.rusult)) {
				return true;
			}
			return false;
		}
		return false;
	}

	/***
	 * 发送回调处理
	 * 
	 * @param m
	 * @return
	 */
	private String httpSendData(CallBackModel m) {
		try {
			if (m.getSendUrl() != null && m.getCallbackdata() != null) {
				String json = HttpUnit.httpPost(m.getSendUrl(), m.getCallbackdata().toString(), false);
				if (json == null) {
					errorHandle(m);
					return null;
				} else {
					return json;
				}
			} else {
				romveCallBackModel(m);
				System.out.println("发送数据url地址为空" + this.callbackCache.size());
				return null;
			}
		} catch (Exception e) {
			errorHandle(m);
			logger.error("通过http发送数据时错误" + e.getMessage());
			return null;
		}
	}

	private boolean checkSendnumb(CallBackModel m) {
		if (m.getSendCount() > sendtime.length - 1)
			return false;
		return true;
	}

	/***
	 * 发送数据异常或错误时处理方法
	 * 
	 * @param m
	 */
	private void errorHandle(CallBackModel m) {
		this.callbackCache.remove(m);
		m.setSendCount(m.getSendCount() + 1);
		if (checkSendnumb(m)) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(m.getSendDate());
			calendar.add(calendar.SECOND, sendtime[m.getSendCount()]);
			m.setSendDate(calendar.getTime());
			System.out.println("下载发送时间为：" + m.getSendDate().getYear() + "年" + m.getSendDate().getMonth() + "月"
					+ m.getSendDate().getDay() + "日 " + m.getSendDate().getHours() + ":" + m.getSendDate().getMinutes()
					+ ":" + m.getSendDate().getSeconds() + "发送次数为：" + m.getSendCount()+"发送数据"+m.getCallbackdata());
			this.callbackCache.add(m);
		} else {
			this.callbackCache.remove(m);
		}
	}

	/***
	 * 根据发送时间判断是否是要发送的数据
	 * 
	 * @param d1
	 *            要发送数据的时间
	 * @param d2
	 *            当前系统时间
	 * @return
	 */
	@SuppressWarnings("unused")
	private Boolean isDateTime(Date d1, Date d2) {

		if (d2.compareTo(d1) <= 0) {
			return true;
		}
		return false;
	}

	/***
	 * 椐据发送次数判断是否要发送数据
	 * 
	 * @param callBackModel
	 * @return
	 */
	@SuppressWarnings("unused")
	private Boolean getsendcount(CallBackModel callBackModel) {
		if (callBackModel == null)
			return false;
		if (callBackModel.getSendCount() > sendtime.length) {
			this.callbackCache.remove(callBackModel);
			return false;
		}
		return true;
	}

}
