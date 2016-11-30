package com.vunke.sharehome.model;

import java.util.List;

public class UpdateContacts {

	private String userName;
	private List<Contacts> contacts;

	public void setUsername(String username) {
		this.userName = username;
	}

	public String getUsername() {
		return userName;
	}

	public void setContacts(List<Contacts> contacts) {
		this.contacts = contacts;
	}

	public List<Contacts> getContacts() {
		return contacts;
	}

	public class Contacts {

		private String face;
		private String groupId;
		private String lastName;
		private String email;
		private String companyPhone;
		private String userId;
		private String userName;
		private String firstName;
		private String homePhone;
		private String contactName;

		public void setFace(String face) {
			this.face = face;
		}

		public String getFace() {
			return face;
		}

		public void setGroupid(String groupid) {
			this.groupId = groupid;
		}

		public String getGroupid() {
			return groupId;
		}

		public void setLastname(String lastname) {
			this.lastName = lastname;
		}

		public String getLastname() {
			return lastName;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getEmail() {
			return email;
		}

		public void setCompanyphone(String companyphone) {
			this.companyPhone = companyphone;
		}

		public String getCompanyphone() {
			return companyPhone;
		}

		public void setUserid(String userid) {
			this.userId = userid;
		}

		public String getUserid() {
			return userId;
		}

		public void setUsername(String username) {
			this.userName = username;
		}

		public String getUsername() {
			return userName;
		}

		public void setFirstname(String firstname) {
			this.firstName = firstname;
		}

		public String getFirstname() {
			return firstName;
		}

		public void setHomephone(String homephone) {
			this.homePhone = homephone;
		}

		public String getHomephone() {
			return homePhone;
		}

		public void setContactname(String contactname) {
			this.contactName = contactname;
		}

		public String getContactname() {
			return contactName;
		}

	}
}