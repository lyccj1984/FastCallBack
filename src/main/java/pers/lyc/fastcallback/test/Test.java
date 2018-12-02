package pers.lyc.fastcallback.test;

import java.util.concurrent.TimeUnit;
import pers.lyc.fastcallback.logic.LogicProx;

public class Test {

	static int data = 0;
	static String datas = "aadflkasdjflkasjdfl;aksjdjfl";

	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		LogicProx lp = new LogicProx(3, 5, new TestObServer(), 1L, "0", 5, 10,15,20);
		int availProcessors = Runtime.getRuntime().availableProcessors();
        System.out.println("avail processors count: " + availProcessors);
		// System.err.println(tests(true));
		while (true) {
			data++;
			TimeUnit.MILLISECONDS.sleep(1000);
			if (data > 100)
				return;
			SObject so = new SObject();
			so.setCallbackdata(String.valueOf(data));
			so.setSendUrl("http://192.168.223.1:8090//webapi/UserBehaviour/report");
			System.err.println("��ӷ�������"+lp.addCallBackModel(so));
			System.out.println("������������==" + data);

		}

	}

	private static boolean tests(boolean a) {
		boolean[] temp = new boolean[] { true, true, false, false };

		for (boolean i : temp) {
			if (!(a & i)) {
				return false;
			}

		}
		return true;

	}

}
