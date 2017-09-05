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

	public boolean addCallBackModel(CallBackModel m) {
		if (m != null)
			return logic.addCallBackModel(m);
		return false;
	}

	public void register(Observer o) {
		logic.addObserver(o);
	}

	public boolean addResult(String result) {
		return logic.addResultKV(result);
	}

}
