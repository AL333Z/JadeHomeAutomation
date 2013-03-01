package com.jadehomeautomation.message;

import jade.core.AID;

public class RegistrationMessage extends Message {
	

	protected String name;
	protected String description;
	
	public RegistrationMessage(String service, AID aid, String name, String description) {
		super(service, aid);
		this.name = name;
		this.description = description;
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getDescription(){
		return this.description;
	}
}
