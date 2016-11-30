package com.vunke.sharehome.model;

import java.io.Serializable;
import java.util.List;

public class Newfriendsbean implements Serializable{

	public String code;
	public String message;
	public List<Data> data;

	public class Data {
		

		public String friendAccount;
		public String friendHead;
		public String friendId;
		public String friendName;
		public String remark;
		public String status;
		public String username;
		@Override
		public String toString() {
			return "Data [friendAccount=" + friendAccount + ", friendHead="
					+ friendHead + ", friendId=" + friendId + ", friendName="
					+ friendName + ", remark=" + remark + ", status=" + status
					+ ", username=" + username + "]";
		}
		

	}





	@Override
	public String toString() {
		return "Newfriendsbean [code=" + code + ", data=" + data + ", message="
				+ message + "]";
	}
	
	
}