package com.jadehomeautomation.message;

import java.io.Serializable;

import jade.core.AID;

public class AgentMessage implements Serializable {
	protected AID aid;
	protected String parentId;
	protected String name;
	protected String description;
	
	public AgentMessage (AID aid, String parentId, String name, String description){
		this.aid = aid;
		this.parentId = parentId;
		this.name = name;
		this.description = description;
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getDescription(){
		return this.description;
	}
	
	public AID getAid(){
		return this.aid;
	}

}
