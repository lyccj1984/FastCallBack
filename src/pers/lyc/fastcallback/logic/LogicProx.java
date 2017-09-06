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
	 * 添加要发送的数据
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
	 * 处理回调结果注册 （需要自己定义观察者模式的接收都Observer）
	 * 
	 * @param o
	 */
	public void register(Observer o) {
		logic.addObserver(o);
	}

	/***
	 * 添加回调参数的为result值是表示回调成功
	 * 
	 * @param result
	 * @return
	 */
	public boolean addResult(String result) {
		return logic.addResultKV(result);
	}

	/***
	 * 设置默认多长时间执行一次发送回调数据单位为秒，默认为5秒
	 * 
	 * @param sleepTime
	 */
	public void setSleepTime(long sleepTime) {
		logic.setSleepTime(sleepTime);
	}

}
