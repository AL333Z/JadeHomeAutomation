package com.jadehomeautomation.message;

import jade.core.AID;

import java.io.Serializable;

@SuppressWarnings("serial")
public class MeshNetRegisterListenerMsg implements Serializable {
	
	private final int deviceId;
	private final AID listenerAid;
	
	public MeshNetRegisterListenerMsg(int deviceId, AID listenerAid){
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
