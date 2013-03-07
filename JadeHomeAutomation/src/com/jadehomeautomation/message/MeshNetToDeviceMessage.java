package com.jadehomeautomation.message;

import java.io.Serializable;

/**
 * This is a message to be sent to the MeshNetGateway agent, to request him
 * to actually send this message to the specified MeshNet device.
 */
public class MeshNetToDeviceMessage implements Serializable {
	
	/** The destination device of this message */
	private final int meshnetDeviceId;
	
	/** The actual data of this message */
	private final byte[] msgData;
	
	/** This is the "command" of the layer4 packet (a low level detail...) */ 
	private final int command;

	public MeshNetToDeviceMessage(int meshnetDeviceId, byte[] msgData, int command) {
		this.meshnetDeviceId = meshnetDeviceId;
		this.msgData = msgData;
		this.command = command;
	}
	
	
	public int getDestinationDeviceId(){
		return meshnetDeviceId;
	}
	
	public byte[] getDataBytes(){
		return msgData;
	}
	
	public int getCommand(){
		return command;
	}

}
