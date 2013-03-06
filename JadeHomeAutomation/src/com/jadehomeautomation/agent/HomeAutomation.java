package com.jadehomeautomation.agent;


/*
 * TODO maybe it's better to move these fields inside the class of
 * the corresponding agent, to improve encapsulation 
 */

public interface HomeAutomation {
	
	// Exposed services for building agents
	public static final String SERVICE_BUILDING_ROOM_LIST = "BUILDING-ROOM-LIST";
	public static final String SERVICE_BUILDING_ROOM_REGISTRATION = "BUILDING_ROOM_REGISTRATION";
	
	// Exposed services for room agents
	public static final String SERVICE_ROOM_DEVICE_LIST = "ROOM_DEVICE_LIST";
	public static final String SERVICE_ROOM_DEVICE_REGISTRATION = "ROOM_DEVICE_REGISTRATION";
	
	// Exposed services for bulb agents
	public static final String SERVICE_BULB_CONTROL = "BULB_CONTROL";
	
	//TODO add other services for other device agents..
	
}
