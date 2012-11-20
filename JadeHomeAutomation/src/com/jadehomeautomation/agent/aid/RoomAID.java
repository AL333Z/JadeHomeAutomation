package com.jadehomeautomation.agent.aid;

import jade.core.AID;
import jade.util.leap.Serializable;

public class RoomAID extends AID implements Serializable {
	private String roomID;
	private String roomName;
	private String roomDescription;
	private int floorNumber;
	
	public RoomAID(String roomID){
		super();
		this.roomID = roomID;
	}
	
	public String getBuildingAID(){
		return this.roomID;
	}
	
	public String getBuildingName(){
		return this.roomName;
	}
	
	public void setBuildingName(String roomName){
		this.roomName = roomName;
	}
	
	public String getDescription(){
		return this.roomDescription;
	}
	
	public void setBuildingDescription(String roomDescription) {
		this.roomDescription = roomDescription;
	}
	
	public int getFloorNumber(){
		return this.floorNumber;
	}

	public void setFloorNumber(int floorNumber){
		this.floorNumber = floorNumber;
	}
}
