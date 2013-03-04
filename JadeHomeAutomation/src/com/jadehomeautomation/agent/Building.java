package com.jadehomeautomation.agent;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREResponder;

import java.io.IOException;
import java.util.LinkedList;

import com.jadehomeautomation.agent.HomeAutomation;
import com.jadehomeautomation.message.*;

public class Building extends Agent {
	// Rooms in the building
	private LinkedList<AgentMessage> rooms;
	
	private String name;
	private String description;
	
	@Override
	protected void setup() {						
		log("I'm started.");

		Object[] args = getArguments();
		if (args != null) {
			if (args.length > 0) this.name = (String) args[0]; 
			if (args.length > 1) this.description = (String) args[1];		
			System.out.println("Created Building with name " + this.name + " descr " + this.description);
		}
		
		this.rooms = new LinkedList<AgentMessage>();
		
		// Create the agent description.
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		
		// Create the services description.
		ServiceDescription roomListSD = new ServiceDescription();
		roomListSD.setType(HomeAutomation.SERVICE_BUILDING_ROOM_LIST);
		roomListSD.setName("JADE-bulding-room-list");
		
		ServiceDescription roomRegistrationSD = new ServiceDescription();
		roomRegistrationSD.setType(HomeAutomation.SERVICE_BUILDING_ROOM_REGISTRATION);
		roomRegistrationSD.setName("JADE-building-room-registration");
		
		// Add the services description to the agent description.
		dfd.addServices(roomListSD);
		dfd.addServices(roomRegistrationSD);
		
		try {
			// Register the service
			log("Registering '"+roomListSD.getType()+"' service named '"+roomListSD.getName()+"'" + "to the default DF...");

			DFService.register(this, dfd);			
			log("Waiting for request...");
			
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		// Add the behaviour serving queries from controller agents.
		MessageTemplate template = AchieveREResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_REQUEST);
		addBehaviour(new AchieveREResponder(this, template){
			
			@Override
			protected ACLMessage handleRequest(ACLMessage request) 
				throws NotUnderstoodException, RefuseException{
				
				//log("Handle request with content:" + request.getContent());
				return new ACLMessage(ACLMessage.AGREE);
			}
			
			@Override
			protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response){
				
				//log("Prepare result notification with content: " + request.getContent());
				response.setPerformative(ACLMessage.INFORM);
								
				Message message = null;
				try {
					message = (Message) request.getContentObject();
					
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if (message != null) {
					if (message.getService().equals(HomeAutomation.SERVICE_BUILDING_ROOM_LIST)) {
						try {							
							// send rooms array
							response.setContentObject(rooms);
							
						} catch (IOException e) {
							e.printStackTrace();
						}					
					}
					else if(message.getService().equals(HomeAutomation.SERVICE_BUILDING_ROOM_REGISTRATION)){
						RegistrationMessage regMessage = (RegistrationMessage) message;
						
						AgentMessage agentDesc = new AgentMessage(regMessage.getAid(), regMessage.getName(), regMessage.getDescription());
						rooms.add(agentDesc);
						
						log("Room " + request.getSender() + " successfully added to building's room list.");
					}	
				}
			
				return response;
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
