package pers.lyc.fastcallback.logic;

import java.util.LinkedList;
import java.util.List;
import java.util.Observer;

import pers.lyc.fastcallback.model.CallBackModel;

public class LogicProx {

	// ExecutorService exec;
	List<CallBackLogic> threadlist = new LinkedList<CallBackLogic>();

	public LogicProx(int threadNumb, Observer o, long sleepTime, String result, int... senddata) {

		// exec = Executors.newCachedThreadPool();
		for (int i = 0; i < threadNumb; i++) {
			CallBackLogic cb = new CallBackLogic(senddata);
			cb.setSleepTime(sleepTime);
			cb.addResultKV(result);
			cb.addObserver(o);
			// exec.execute(cb);
			Thread ts = new Thread(cb);
			ts.setName("callbackthread" + i);
			threadlist.add(cb);
			ts.start();
			System.out.println(ts.getName() + " ==" + threadlist.size());

		}
		// exec.shutdown();

	}

	/***
	 * 添加要发送的数据
	 * 
	 * @param m
	 * @return
	 */

	public boolean addCallBackModel(CallBackModel m) {
		if (m != null) {
			return getMinQueue().addSendCallbackData(m);
		}
		return false;
	}

	/*
	 * private CallBackLogic getMinQueue() { CallBackLogic tempcallback=null;
	 * for (int i = 0; i < this.threadlist.size(); i++) { if(tempcallback==null)
	 * { tempcallback = threadlist.get(i); }else { if
	 * (tempcallback.callbackCacheQueue.size() >
	 * threadlist.get(i).callbackCacheQueue.size()) { tempcallback =
	 * threadlist.get(i); } }
	 * 
	 * for (int j = i + 1; j < this.threadlist.size(); j++) { if
	 * (tempcallback.callbackCacheQueue.size() >
	 * threadlist.get(j).callbackCacheQueue.size()) { tempcallback =
	 * threadlist.get(j); } } } return tempcallback; }
	 */

	private CallBackLogic getMinQueue() {
		CallBackLogic tempcallback = null;
		int tem = 0;
		for (int i = 1; i < this.threadlist.size(); i++) {
			if (threadlist.get(tem).queueSize.get() > threadlist.get(i).queueSize.get()) {
				tem = i;
			}
		}
		return threadlist.get(tem);
	}

	/***
	 * 处理回调结果注册 （需要自己定义观察者模式的接收都Observer）
	 * 
	 * @param o
	 */
	/*
	 * public void register(Observer o) { logic.addObserver(o); }
	 */
	/***
	 * 添加回调参数的为result值是表示回调成功
	 * 
	 * @param result
	 * @return
	 */
	/*
	 * public boolean addResult(String result) { return
	 * logic.addResultKV(result); }
	 */

	/***
	 * 设置默认多长时间执行一次发送回调数据单位为秒，默认为5秒
	 * 
	 * @param sleepTime
	 */
	/*
	 * public void setSleepTime(long sleepTime) { logic.setSleepTime(sleepTime);
	 * }
	 */

}
