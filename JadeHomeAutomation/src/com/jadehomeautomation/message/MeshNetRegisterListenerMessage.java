package com.jadehomeautomation.message;

import jade.core.AID;

import java.io.Serializable;

@SuppressWarnings("serial")
public class MeshNetRegisterListenerMessage implements Serializable {
	
	private final int deviceId;
	private final AID listenerAid;
	
	public MeshNetRegisterListenerMessage(int deviceId, AID listenerAid){
		this.deviceId = deviceId;
		this.listenerAid = listenerAid;
	}
	
	public int getDeviceId(){
		return deviceId;
	}
	
	public AID getListenerAid(){
		return listenerAid;
	}
}
