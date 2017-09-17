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

	private static Logger logger = (Logger) LoggerFactory.getLogger(CallBackLogic.class); // ��־��¼

	public volatile int maxQueueNumb = Integer.MAX_VALUE;// �н���������

	static final float DEFAULT_LOAD_FACTOR = 0.75f;
	@SuppressWarnings("rawtypes")
	LinkedBlockingQueue<CallBackModel> callbackCacheQueue;
	int[] sendtime;
	String rusult;
	long sleepTime = 5; // ���ö೤ʱ������һ�Σ��������� ��λΪ��
	Thread run;// �߳���
	private boolean interrup = true;// �ж��̱߳�

	private boolean threadrunfalg = true;// �߳�whileѭ����ʶ

	public AtomicInteger queueSize = new AtomicInteger(0);// ��ǰ�����еĶ�����Ŀ

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
	 * ��ȡ�����������ֵ�Ƿ����
	 * 
	 * @return���ڻ���ڷ���true ����Ϊfasle
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
	 * ���һ���ص�ʵ��
	 * 
	 * @param isonce
	 *            �Ƿ����Ϸ���һ��
	 * @param callBackModel
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private boolean addCallBackModel(CallBackModel callBackModel) {
		if (callBackModel != null) {

			boolean tempadd = callbackCacheQueue.offer(callBackModel);
			if (tempadd) {
				this.queueSize.incrementAndGet();
				System.out.println("����" + callBackModel.getCallbackdata() + "�������");
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
	 * �ص�ʵ�����(ɾ��)
	 * 
	 * @return
	 */
	public CallBackModel takecallBackModel() {
		CallBackModel tempm;
		tempm = callbackCacheQueue.poll();
		if (tempm != null) {
			this.queueSize.decrementAndGet();
			System.out.println("====��������ɵ�ǰ������Ϊ��" + this.queueSize.get());
			return tempm;
		}
		return null;
	}

	/***
	 * ���캯����������Ƿ���ʱ�������λΪ�룬���ܱ�5����
	 * 
	 * @param senddata
	 */
	public CallBackLogic(int maxQueue, int... senddata) {
		super();
		if (senddata.length < 1)
			throw new FastException("�ص�ʱ�䣬����Ϊ��");
		this.maxQueueNumb = maxQueue;
		callbackCacheQueue = new LinkedBlockingQueue<CallBackModel>(maxQueueNumb);
		sendtime = new int[senddata.length];
		for (int i = 0; i < sendtime.length; i++) {
			sendtime[i] = senddata[i];
		}

	}

	/**
	 * �����߳�
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
								this.hashCode() + "��ǰ�����е�����===��" + queueSize.get() + "===" + callbackCacheQueue.size());
					} else {
						throw new InterruptedException();
					}
				} catch (InterruptedException e) {
					threadrunfalg = false;
					logger.error("����ʱ�̡߳�����:" + e.getMessage());
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
				setChanged();// �ı��¼�״̬
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
	 * ���ͻص�����
	 * 
	 * @param m
	 * @return
	 */
	private String httpSendData(CallBackModel m) {
		try {
			if (m.getSendUrl() != null && m.getCallbackdata() != null) {
				System.err.println("��ǰ��Ҫ��������Ϊ==" + m.getCallbackdata() + "����Ϊ:" + m.getSendCount());
				String json = HttpUnit.httpPost(m.getSendUrl(), m.getCallbackdata().toString(), false);
				if (json == null) {
					errorHandle(m);
					return null;
				} else {
					return json;
				}
			} else {
				System.out.println("��������url��ַΪ��" + this.queueSize);
				return null;
			}
		} catch (Exception e) {
			errorHandle(m);
			System.out.println("�������ݴ���" + m.getCallbackdata());
			return null;
		}
	}

	private boolean checkSendnumb(CallBackModel m) {
		if (m.getSendCount() > sendtime.length) {
			takecallBackModel();
			// System.err.println("��ǰ���ʹ���Ϊ" +
			// m.getSendCount()+"�ѳ������ʹ���"+sendtime.length);
			return false;
		}
		return true;
	}

	/***
	 * ���������쳣�����ʱ������
	 * 
	 * @param m
	 */
	private void errorHandle(CallBackModel m) {
		this.takecallBackModel();
		m.setSendCount(m.getSendCount() + 1);
		if (checkSendnumb(m)) {
			m.setSendDate(addDateTime(m.getSendDate(), sendtime[m.getSendCount() - 2]));
			System.out.println("�����ۼӴ���==��" + (m.getSendCount() - 1) + "�����͵�����==" + m.getCallbackdata());
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
		if (d1.before(d2) || d1.equals(d2))
			return true;

		return false;
	}

}
