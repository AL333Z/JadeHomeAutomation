package com.jadehomeautomation.message;

import jade.core.AID;

public class RegistrationMessage extends Message {
	
	protected final String parentId;
	protected final String name;
	protected final String description;
	
	public RegistrationMessage(String service, AID aid, String parentId, String name, String description) {
		super(service, aid);
		this.parentId = parentId;
		this.name = name;
		this.description = description;
	}
	
	public String getParentId(){
		return this.parentId;
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getDescription(){
		return this.description;
	}
}
