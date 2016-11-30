package com.vunke.sharehome.model;

import java.util.List;

public class ShareContactsBean {
	public String code;
	public String message;
	public List<ShareContacts> shareContacts;

	public class ShareContacts {

		public String requestCode;
		public String status;
		public String userDn;
		public String userId;
		public String userIp;
		public String userMobile;
		public String userPass;
		public String userType;
		@Override
		public String toString() {
			return "ShareContacts [requestCode=" + requestCode + ", status="
					+ status + ", userDn=" + userDn + ", userId=" + userId
					+ ", userIp=" + userIp + ", userMobile=" + userMobile
					+ ", userPass=" + userPass + ", userType=" + userType + "]";
		}

	}
}
