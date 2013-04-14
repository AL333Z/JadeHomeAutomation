package com.jadehomeautomation.demo;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Demo extends Agent {

	protected void setup() {						
		log("I'm started.");
		
		this.addBehaviour(new OneShotBehaviour(this){

			@Override
			public void action() {
				// TODO Auto-generated method stub

				ContainerController cc = getContainerController();
				AgentController ac;
				
				// create a building, with an ID, name and description
				String buildingId = "b001";
				try {
					String[] args = {buildingId, "build001", "first building"};
					ac = cc.createNewAgent("building001", "com.jadehomeautomation.agent.Building", args);
					ac.start();
				} catch (StaleProxyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// create a room, with an ID, name and description, associated with a building
				String roomId1 = "r001";
				try {
					String[] args = {roomId1, buildingId, "room001", "first room"};
					ac = cc.createNewAgent("room001", "com.jadehomeautomation.agent.Room", args);
					ac.start();
				} catch (StaleProxyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				// create a room, with an ID, name and description, associated with a building
				String roomId2 = "r002";
				try {
					String[] args = {roomId2, buildingId, "room002", "second room"};
					ac = cc.createNewAgent("room002", "com.jadehomeautomation.agent.Room", args);
					ac.start();
				} catch (StaleProxyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				
				// create a toggleswitch, with an ID, name and description, associated with a room
				String ts001Id = "ts001";
				try {
					final int meshnetDeviceId = 394932;
					String[] args = {ts001Id, roomId1, "ts001", "first toggleswitch", meshnetDeviceId+""};
					ac = cc.createNewAgent("toggleswitch001", "com.jadehomeautomation.agent.ToggleSwitch", args);
					ac.start();
				} catch (StaleProxyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				// create 3 bulb, with an ID, name and description, associated with a room001, that listen to a toggleswitch
				for (int i = 0; i < 3; i++) {
					String bulbId = "bulb00"+i;
					try {
						final int meshnetDeviceId = 384932;
						String[] args = {bulbId, roomId1, "bulb00"+i, "bulb "+i, meshnetDeviceId+"", ""};
						ac = cc.createNewAgent(roomId1+bulbId, "com.jadehomeautomation.agent.Bulb", args);
						ac.start();
					} catch (StaleProxyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				// create 2 bulb, with an ID, name and description, associated with a room001, that listen to a toggleswitch
				for (int i = 0; i < 2; i++) {
					String bulbId = "bulb00"+i;
					try {
						final int meshnetDeviceId = 394932;
						String[] args = {bulbId, roomId2, "bulb00"+i, "bulb "+i, meshnetDeviceId+"", ""};
						ac = cc.createNewAgent(roomId2+bulbId, "com.jadehomeautomation.agent.Bulb", args);
						ac.start();
					} catch (StaleProxyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});	
	}

	/*
	 * Remember to deregister the services offered by the agent upon shutdown,
	 * because the JADE platform does not do it by itself!
	 */
	@Override
	protected void takeDown() {
		try {
			log("De-registering myself from the default DF...");
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		log("I'm done.");
	}
		
	private void log(String msg) {
		System.out.println("["+getName()+"]: "+msg);
	}	
	
}
