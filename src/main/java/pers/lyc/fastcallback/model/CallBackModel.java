package pers.lyc.fastcallback.model;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public abstract class CallBackModel<T extends Object> {

	/**
	 * 要发送的数据
	 * 
	 * @return
	 */
	public T getCallbackdata() {
		return callbackdata;
	}

	public void setCallbackdata(T callbackdata) {
		this.callbackdata = callbackdata;
	}

	public String getSendUrl() {
		return sendUrl;
	}

	public void setSendUrl(String sendUrl) {
		this.sendUrl = sendUrl;
	}

	public Date getSendDate() {
		return (Date) sendDate.clone();
	}

	public void setSendDate(Date sendDate) {
		this.sendDate =(Date) sendDate.clone();
	}

	public Integer getSendCount() {
		return sendCount;
	}

	public void setSendCount(int sendCount) {

		this.sendCount = sendCount;
	}

	public Integer getOuttiem() {
		return outtiem;
	}

	public void setOuttiem(int outtiem) {
		this.outtiem = outtiem;
	}

	public T callbackdata;

	private String sendUrl;

	private Date sendDate=new Date();

	private Integer sendCount=0;

	private Integer outtiem = 12000;

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CallBackModel<T> other = (CallBackModel<T>) obj;
		if (sendUrl == null) {
			if (other.sendUrl != null) {
				return false;
			}
		} else if (!this.sendUrl.equals(other.sendUrl)) {
			return false;
		}

		if (callbackdata == null) {
			if (other.callbackdata != null) {
				return false;
			}
		} else if (!this.callbackdata.equals(other.callbackdata)) {
			return false;
		}

		if (sendDate == null) {
			if (other.sendDate != null) {
				return false;
			}
		} else if (!this.sendDate.equals(other.sendDate)) {
			return false;
		}

		if (sendCount == null) {
			if (other.sendCount != null) {
				return false;
			}
		} else if (!this.sendCount.equals(other.sendCount)) {
			return false;
		}

		if (outtiem == null) {
			if (other.outtiem != null) {
				return false;
			}
		} else if (!this.outtiem.equals(other.outtiem)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		final int prime = 31;
		int result = 1;
		result = prime * result + ((callbackdata == null) ? 0 : callbackdata.hashCode());
		result = prime * result + ((sendUrl == null) ? 0 : sendUrl.hashCode());
		result = prime * result + ((sendDate == null) ? 0 : sendDate.hashCode());
		result = prime * result + ((sendCount == null) ? 0 : sendCount.hashCode());
		result = prime * result + ((outtiem == null) ? 0 : outtiem.hashCode());
		return result;
		// return super.hashCode();
	}

}
