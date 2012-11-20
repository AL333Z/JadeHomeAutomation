package com.jadehomeautomation.agent;

import java.util.LinkedList;

import com.jadehomeautomation.agent.aid.DeviceAID;

import jade.core.AID;
import jade.core.Agent;

public class Room extends Agent {
	// Devices in the room
	private LinkedList<DeviceAID> agents;
}
