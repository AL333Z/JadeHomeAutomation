package com.jadehomeautomation.message;

import jade.core.AID;

/**
 * This is a message to be sent to the MeshNetGateway agent, to request him
 * to actually send this message to the specified MeshNet device 
 */
public class MeshNetToDeviceMessage extends Message {
	
	/** The destination device of this message */
	private final int meshnetDeviceId;
	
	/** The actual data of this message */
	private final byte[] msgData;

	public MeshNetToDeviceMessage(String service, AID aid, int meshnetDeviceId, byte[] msgData) {
		super(service, aid);
		this.meshnetDeviceId = meshnetDeviceId;
		this.msgData = msgData;
	}
	
	
	public int getDestinationDeviceId(){
		return meshnetDeviceId;
	}
	
	public byte[] getDataBytes(){
		return msgData;
	}

}
