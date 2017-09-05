package pers.lyc.fastcallback.test;

import java.util.Observable;
import java.util.Observer;

public class TestObServer implements Observer {

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		System.out.println("接收者收到数据："+((SObject) arg).getSendUrl());
	}

}
