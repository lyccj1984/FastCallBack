package pers.lyc.fastcallback.logic;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;
import pers.lyc.fastcallback.common.HttpUnit;
import pers.lyc.fastcallback.exception.FastException;
import pers.lyc.fastcallback.model.CallBackModel;

class CallBackLogic extends Observable {

	private static Logger logger = (Logger) LoggerFactory.getLogger(CallBackLogic.class); // 日志记录

	public volatile int maxQueueNumb = Integer.MAX_VALUE;// 有界队列最大数

	static final float DEFAULT_LOAD_FACTOR = 0.75f;
	@SuppressWarnings("rawtypes")
	LinkedBlockingQueue<CallBackModel> callbackCacheQueue;
	int[] sendtime;
	String rusult;
	long sleepTime = 5; // 设置多长时间运行一次，发送数据 单位为秒
	Thread run;// 线程类
	private boolean interrup = true;// 中断线程标

	private boolean threadrunfalg = true;// 线程while循环标识

	public AtomicInteger queueSize = new AtomicInteger(0);// 当前队列中的对象数目

	public boolean isInterrup() {
		return interrup;
	}

	public synchronized void setInterrup(boolean interrup) {
		if (run != null) {
			this.interrup = interrup;
			run.interrupt();
		}
	}

	/**
	 * 获取队列数与最大值是否相等
	 * 
	 * @return大于或等于返回true 否则为fasle
	 */
	public boolean isQueueFull() {
		if (this.queueSize.get() >= this.maxQueueNumb * DEFAULT_LOAD_FACTOR)
			return true;

		return false;
	}

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

	/**
	 * 添加一个回调实体
	 * 
	 * @param isonce
	 *            是否马上发送一次
	 * @param callBackModel
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private boolean addCallBackModel(CallBackModel callBackModel) {
		if (callBackModel != null) {

			boolean tempadd = callbackCacheQueue.offer(callBackModel);
			if (tempadd) {
				this.queueSize.incrementAndGet();
				System.out.println("对像" + callBackModel.getCallbackdata() + "加入队列");
				return true;
			}
			return false;

		}
		return false;
	}

	public boolean addSendCallbackData(CallBackModel m) {
		m.setSendDate(addDateTime(m.getSendDate(), sendtime[0]));
		m.setSendCount(1);
		return addCallBackModel(m);
	}

	/**
	 * 回调实体出队(删除)
	 * 
	 * @return
	 */
	public CallBackModel takecallBackModel() {
		CallBackModel tempm;
		tempm = callbackCacheQueue.poll();
		if (tempm != null) {
			this.queueSize.decrementAndGet();
			System.out.println("====出队已完成当前数据量为：" + this.queueSize.get());
			return tempm;
		}
		return null;
	}

	/***
	 * 构造函数，传入的是发送时间隔，单位为秒，以能被5整除
	 * 
	 * @param senddata
	 */
	public CallBackLogic(int maxQueue, int... senddata) {
		super();
		if (senddata.length < 1)
			throw new FastException("回调时间，不能为空");
		this.maxQueueNumb = maxQueue;
		callbackCacheQueue = new LinkedBlockingQueue<CallBackModel>(maxQueueNumb);
		sendtime = new int[senddata.length];
		for (int i = 0; i < sendtime.length; i++) {
			sendtime[i] = senddata[i];
		}

	}

	/**
	 * 启动线程
	 */
	public void start() {
		run = new Thread(new RunThread());
		run.start();
	}

	class RunThread extends Thread {

		@Override
		public void run() {

			while (threadrunfalg) {
				try {
					if (!this.isInterrupted()) {
						Date datetime = new Date();
						sendData(datetime);
						TimeUnit.SECONDS.sleep(sleepTime);
						System.out.println(
								this.hashCode() + "当前缓存中的数据===：" + queueSize.get() + "===" + callbackCacheQueue.size());
					} else {
						throw new InterruptedException();
					}
				} catch (InterruptedException e) {
					threadrunfalg = false;
					logger.error("【计时线程】出错:" + e.getMessage());
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private void sendData(Date datatime) {
		/* CallBackModel callBackModel = this.takecallBackModel(); */
		CallBackModel m = this.callbackCacheQueue.peek();
		if (m == null)
			return;
		if (isDateTime(m.getSendDate(), datatime) && this.checkSendnumb(m)) {
			String json = httpSendData(m);
			if (json != null) {
				this.takecallBackModel();
				setChanged();// 改变事件状态
				notifyObservers(m);
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
				System.err.println("当前正要发送数据为==" + m.getCallbackdata() + "次数为:" + m.getSendCount());
				String json = HttpUnit.httpPost(m.getSendUrl(), m.getCallbackdata().toString(), false);
				if (json == null) {
					errorHandle(m);
					return null;
				} else {
					return json;
				}
			} else {
				System.out.println("发送数据url地址为空" + this.queueSize);
				return null;
			}
		} catch (Exception e) {
			errorHandle(m);
			System.out.println("发送数据错误" + m.getCallbackdata());
			return null;
		}
	}

	private boolean checkSendnumb(CallBackModel m) {
		if (m.getSendCount() > sendtime.length) {
			takecallBackModel();
			// System.err.println("当前发送次数为" +
			// m.getSendCount()+"已超过发送次数"+sendtime.length);
			return false;
		}
		return true;
	}

	/***
	 * 发送数据异常或错误时处理方法
	 * 
	 * @param m
	 */
	private void errorHandle(CallBackModel m) {
		this.takecallBackModel();
		m.setSendCount(m.getSendCount() + 1);
		if (checkSendnumb(m)) {
			m.setSendDate(addDateTime(m.getSendDate(), sendtime[m.getSendCount() - 2]));
			System.out.println("共计累加次数==：" + (m.getSendCount() - 1) + "被发送的数据==" + m.getCallbackdata());
			this.addCallBackModel(m);
		}
	}

	private Date addDateTime(Date dt, int second) {
		Date dtc = null;
		if (dt != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(dt);
			calendar.add(calendar.SECOND, second);
			dtc = calendar.getTime();
		}
		return dtc;
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
		if (d1.before(d2) || d1.equals(d2))
			return true;

		return false;
	}

}
