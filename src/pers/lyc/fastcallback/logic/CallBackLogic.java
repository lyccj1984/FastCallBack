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

	private static Logger logger = (Logger) LoggerFactory.getLogger(CallBackLogic.class); // ��־��¼

	@SuppressWarnings("rawtypes")
	Set<CallBackModel> callbackCache = new CopyOnWriteArraySet<CallBackModel>();
	int[] sendtime;
	String rusult;
	long sleepTime=5; //���ö೤ʱ������һ�Σ��������� ��λΪ��

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
			System.out.println("��������"+callBackModel.getCallbackdata()+"����ӵ����ͻ���");
			return callbackCache.add(callBackModel);
		}
		return false;
	}

	public boolean romveCallBackModel(@SuppressWarnings("rawtypes") CallBackModel cm) {
		return callbackCache.remove(cm);
	}

	/***
	 * ���캯����������Ƿ���ʱ�������λΪ�룬���ܱ�5����
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
				System.out.println("���Ƿ��������߳�====��ǰ�����е�����Ϊ��" + this.callbackCache.size());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				logger.error("����ʱ�̡߳�����:" + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private synchronized void sendData(Date datatime) {
		for (CallBackModel callBackModel : callbackCache) {
			if (getsendcount(callBackModel)) {// �жϷ��ʹ����Ƿ��ѳ����趨����
				// ����ʱ���ж��Ƿ���Է�������
				try {
					if (isDateTime(callBackModel.getSendDate(), datatime)) {// �жϵ�ǰ�����Ƿ���Է�
						String json = httpSendData(callBackModel);
						if (json != null) {
							if (this.checkResult(json)) {
								this.callbackCache.remove(callBackModel);
								System.out.println("����������==��ǰ����������Ϊ==" + this.callbackCache.size() + "�������ݴ���Ϊ��="
										+ callBackModel.getSendCount());// ��ʼ��������
								setChanged();// �ı��¼�״̬
								this.notifyObservers(callBackModel);// ֪ͨ�۲��߷�������
							} else {
								this.errorHandle(callBackModel);
							}
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					logger.error("���������ݡ��������ݳ���" + e.getMessage());
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
	 * ���ͻص�����
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
				System.out.println("��������url��ַΪ��" + this.callbackCache.size());
				return null;
			}
		} catch (Exception e) {
			errorHandle(m);
			logger.error("ͨ��http��������ʱ����" + e.getMessage());
			return null;
		}
	}

	private boolean checkSendnumb(CallBackModel m) {
		if (m.getSendCount() > sendtime.length - 1)
			return false;
		return true;
	}

	/***
	 * ���������쳣�����ʱ������
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
			System.out.println("���ط���ʱ��Ϊ��" + m.getSendDate().getYear() + "��" + m.getSendDate().getMonth() + "��"
					+ m.getSendDate().getDay() + "�� " + m.getSendDate().getHours() + ":" + m.getSendDate().getMinutes()
					+ ":" + m.getSendDate().getSeconds() + "���ʹ���Ϊ��" + m.getSendCount()+"��������"+m.getCallbackdata());
			this.callbackCache.add(m);
		} else {
			this.callbackCache.remove(m);
		}
	}

	/***
	 * ���ݷ���ʱ���ж��Ƿ���Ҫ���͵�����
	 * 
	 * @param d1
	 *            Ҫ�������ݵ�ʱ��
	 * @param d2
	 *            ��ǰϵͳʱ��
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
	 * 駾ݷ��ʹ����ж��Ƿ�Ҫ��������
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
