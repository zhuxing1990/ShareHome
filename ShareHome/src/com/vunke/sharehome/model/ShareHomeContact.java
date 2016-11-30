package com.vunke.sharehome.model;

import java.util.ArrayList;
import java.util.List;

import com.vunke.sharehome.greendao.dao.bean.Contact;

public class ShareHomeContact {

	private String code;
	private String message;
	private ArrayList<Contact> contacts;

	@Override
	public String toString() {
		return "ShareHomeContact [code=" + code + ", message=" + message
				+ ", contacts=" + contacts + "]";
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setContacts(ArrayList<Contact> contacts) {
		this.contacts = contacts;
	}

	public List<Contact> getContacts() {
		return contacts;
	}

	
}