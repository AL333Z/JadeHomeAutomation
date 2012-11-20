package com.jadehomeautomation.agent.aid;

import jade.core.AID;
import jade.util.leap.Serializable;

public class BuildingAID extends AID implements Serializable {
	private String buildingID;
	private String buildingName;
	private String buildingDescription;
	
	public BuildingAID(String buildingID){
		super();
		this.buildingID = buildingID;
	}
	
	public String getBuildingAID(){
		return this.buildingID;
	}
	
	public String getBuildingName(){
		return this.buildingName;
	}
	
	public void setBuildingName(String buildingName){
		this.buildingName = buildingName;
	}
	
	public String getDescription(){
		return this.buildingDescription;
	}
	
	public void setBuildingDescription(String buildingDescription) {
		this.buildingDescription = buildingDescription;
	}
}
