package pers.lyc.fastcallback.logic;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import pers.lyc.fastcallback.model.CallBackModel;

public class LogicProx {

	// ExecutorService exec;
	List<CallBackLogic> threadlist = Collections.synchronizedList(new LinkedList<CallBackLogic>());
	Timer timer, createtimer;
	private int threadnumb;
	private long sleepTime;
	private int[] snddatatime;
	private Observer o;
	private String result;
	private int maxQueue = Integer.MAX_VALUE;
	ExecutorService addcallback;
    
	//private int cpunumb=Runtime.getRuntime().availableProcessors();
	
	public LogicProx(int threadNumb, int maxQueue, Observer o, long sleepTime, String result, int... senddata) {

		this.threadnumb = threadNumb;
		this.sleepTime = sleepTime;
		this.o = o;
		this.snddatatime = senddata.clone();
		this.result = result;
		this.maxQueue = maxQueue;
		addcallback = Executors.newCachedThreadPool();//Executors.newFixedThreadPool(threadnumb);
		init();
		timetask();
		createThread();

	}

	private void init() {
		for (int i = 0; i < threadnumb; i++) {
			creatThread();
		}
	}

	private void timetask() {
		timer = new Timer();
		TimerTask timertask = new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				shutDownThread();

			}
		};
		timer.schedule(timertask, 5000, 3000);

	}

	private void createThread() {
		this.createtimer = new Timer();
		createtimer.schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (isThreadQueue(true)) {
					creatThread();
				}
			}

		}, 5000, 1000);
	}

	/**
	 * 判断线程是否有空运行的，如果有则中断线程
	 */
	private void shutDownThread() {

		for (Iterator i = threadlist.iterator(); i.hasNext();) {
			CallBackLogic cbk = (CallBackLogic) i.next();
			if (cbk.queueSize.get() == 0 && cbk.isInterrup() == true && threadlist.size() > 1) {
				cbk.setInterrup(false);
				i.remove();
				System.out.println("关闭线程操作,当前线程数为:" + threadlist.size());
			}
		}

	}

	/**
	 * 检查当前所有线程队列是否已满
	 * 
	 * @param bool
	 * @return ture 为已满，false为未满
	 */
	private boolean isThreadQueue(boolean bool) {
		for (CallBackLogic i : threadlist) {
			if (!(bool && i.isQueueFull()))
				return false;
		}
		return true;
	}

	/**
	 * 创建线程
	 * 
	 */
	private void creatThread() {
		CallBackLogic cb = new CallBackLogic(this.maxQueue, snddatatime);
		cb.setSleepTime(sleepTime);
		cb.addResultKV(result);
		cb.addObserver(o);
		cb.start();
		threadlist.add(cb);
		System.out.println("============================创建线程成功！！");
	}

	/***
	 * 添加要发送的数据
	 * 
	 * @param m
	 * @return
	 */

	public boolean addCallBackModel(CallBackModel m) {
		if (m != null) {

			Future<Boolean> res = addcallback.submit(new Callable<Boolean>() {

				@Override
				public Boolean call() throws Exception {
					// TODO Auto-generated method stub
					return getMinQueue().addSendCallbackData(m);
				}
			});

			try {
				return res.get();
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return false;

	}

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

	

}
