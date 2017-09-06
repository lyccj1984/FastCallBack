package pers.lyc.fastcallback.test;
import java.util.concurrent.TimeUnit;
import pers.lyc.fastcallback.logic.LogicProx;

public class Test {

	static int data = 0;
	static String datas = "aadflkasdjflkasjdfl;aksjdjfl";

	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		LogicProx lp = new LogicProx(5, 10, 20, 30, 40, 50);
		lp.addResult("0");
		lp.setSleepTime(1);
		lp.register(new TestObServer());
		while (true) {
			data++;
			TimeUnit.MILLISECONDS.sleep(20000);
			SObject temp = new SObject();
			temp.setSendUrl("http://192.168.223.1:8090/webapi/UserBehaviour/report");
			temp.setCallbackdata(String.valueOf(data));
			lp.addCallBackModel(temp);			
			System.out.println("已添加数据" + data);
			
			
		}

	}

}
