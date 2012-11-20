package com.jadehomeautomation.agent.aid;

import jade.core.AID;
import jade.util.leap.Serializable;

public class DeviceAID extends AID implements Serializable {
	private String deviceID;
	private String deviceName;
	private String deviceDescription;
	private String deviceFamily;
	
	public DeviceAID(String deviceID){
		super();
		this.deviceID = deviceID;
	}
	
	public String getBuildingAID(){
		return this.deviceID;
	}
	
	public String getBuildingName(){
		return this.deviceName;
	}
	
	public void setBuildingName(String deviceName){
		this.deviceName = deviceName;
	}
	
	public String getDescription(){
		return this.deviceDescription;
	}
	
	public void setBuildingDescription(String deviceDescription) {
		this.deviceDescription = deviceDescription;
	}
	
	public String getDeviceFamily(){
		return this.deviceFamily;
	}
	
	public void setDeviceFamily(String deviceFamily) {
		this.deviceFamily = deviceFamily;
	}
}
