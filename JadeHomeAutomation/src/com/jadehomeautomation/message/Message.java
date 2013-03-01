package com.jadehomeautomation.message;

import java.io.Serializable;

import jade.core.AID;


public class Message implements Serializable {

	protected String service;
	protected AID aid;
	
	public Message(String service, AID aid) {
		this.service = service;
		this.aid = aid;
	}
	
	public String getService(){
		return this.service;
	}
	
	public AID getAid(){
		return this.aid;
	}
	
}
