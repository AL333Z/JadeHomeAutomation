package com.jadehomeautomation.agent;

import java.io.IOException;
import java.util.LinkedList;

import com.jadehomeautomation.ArduinoUsbCommunicator;
import com.jadehomeautomation.agent.aid.RoomAID;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

public class Building extends Agent {
	// Rooms in the building
	private LinkedList<RoomAID>[] rooms;
	
	@Override
	protected void setup() {						
		log("I'm started.");

		/*
		 *  1- Create the agent description.
		 */
		DFAgentDescription dfd = new DFAgentDescription();
		/*
		 *  2- Fill its mandatory fields.
		 */
		dfd.setName(getAID());
		/*
		 *  3- Create the services description.
		 */
		ServiceDescription roomListSD = new ServiceDescription();
		roomListSD.setType("building-room-list");
		roomListSD.setName("JADE-bulding-room-list");
		
		/*
		 *  5- Add the services description to the agent description.
		 */
		//TODO add here other Sevice Descriptions
		dfd.addServices(roomListSD);
		
		try {
			/*
			 *  6- Register the service (through the agent description multiple
			 *  services can be registered in one shot).
			 */
			log("Registering '"+roomListSD.getType()+"' service named '"+roomListSD.getName()+"'" +
					"to the default DF...");
			DFService.register(this, dfd);
			log("Waiting for request...");
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		/*
		 *  Add the behaviour serving queries from controller agents.
		 */
		MessageTemplate template = AchieveREResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_REQUEST);
		addBehaviour(new AchieveREResponder(this, template){
			
			@Override
			protected ACLMessage handleRequest(ACLMessage request) 
				throws NotUnderstoodException, RefuseException{
				
				log("Handle request..");
				log("Req content - handleReq: " + request.getContent());
				
				return new ACLMessage(ACLMessage.AGREE);
			}
			
			@Override
			protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response){
				
				log("Prepare result");
				log("Req content - prepareResultNotification: " + request.getContent());
				
				//
				ACLMessage res = new ACLMessage(ACLMessage.INFORM);
					try {
						// send rooms array..
						
						// TODO test array...
						RoomAID[] test = new RoomAID[3];
						test[0] = new RoomAID("room001");
						test[0].setBuildingDescription("MyFirstFuckingRoom001");
						
						//TODO return real value, not test...
						
						
						res.setContentObject(test);
					} catch (IOException e) {
						e.printStackTrace();
					}
				
				return res;
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
