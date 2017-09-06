package pers.lyc.fastcallback.logic;

import java.util.Observer;

import pers.lyc.fastcallback.model.CallBackModel;

public class LogicProx {

	CallBackLogic logic;

	public LogicProx(int... senddata) {
		logic = new CallBackLogic(senddata);
		Thread thread = new Thread(logic);
		thread.start();
	}

	/***
	 * ���Ҫ���͵�����
	 * 
	 * @param m
	 * @return
	 */
	public boolean addCallBackModel(CallBackModel m) {
		if (m != null)
			return logic.addCallBackModel(m);
		return false;
	}

	/***
	 * ����ص����ע�� ����Ҫ�Լ�����۲���ģʽ�Ľ��ն�Observer��
	 * 
	 * @param o
	 */
	public void register(Observer o) {
		logic.addObserver(o);
	}

	/***
	 * ��ӻص�������Ϊresultֵ�Ǳ�ʾ�ص��ɹ�
	 * 
	 * @param result
	 * @return
	 */
	public boolean addResult(String result) {
		return logic.addResultKV(result);
	}

	/***
	 * ����Ĭ�϶೤ʱ��ִ��һ�η��ͻص����ݵ�λΪ�룬Ĭ��Ϊ5��
	 * 
	 * @param sleepTime
	 */
	public void setSleepTime(long sleepTime) {
		logic.setSleepTime(sleepTime);
	}

}
