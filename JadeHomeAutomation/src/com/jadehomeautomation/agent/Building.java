package com.jadehomeautomation.agent;

import java.io.IOException;
import java.util.LinkedList;

import com.jadehomeautomation.ArduinoUsbCommunicator;
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
	private LinkedList<AID> rooms;
	
	@Override
	protected void setup() {						
		log("I'm started.");

		this.rooms = new LinkedList<AID>();
		
		// Create the agent description.
		 
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		
		/*
		 * Create the services description.
		 */
		ServiceDescription roomListSD = new ServiceDescription();
		roomListSD.setType("building-room-list");
		roomListSD.setName("JADE-bulding-room-list");
		
		ServiceDescription roomRegistrationSD = new ServiceDescription();
		roomRegistrationSD.setType("building-room-registration");
		roomRegistrationSD.setName("JADE-building-room-registration");
		
		/*
		 * Add the services description to the agent description.
		 */
		//TODO add here other Sevice Descriptions
		dfd.addServices(roomListSD);
		dfd.addServices(roomRegistrationSD);
		
		try {
			/*
			 *  6- Register the service (through the agent description multiple
			 *  services can be registered in one shot).
			 */
			log("Registering '"+roomListSD.getType()+"' service named '"+roomListSD.getName()+"'" +
					"to the default DF...");
			DFService.register(this, dfd);
			
			//TODO pass the BuildingAID, not the simple AID!!!
//			DFService.register(this, dfName, dfd);
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
				
				log("Handle request with content:" + request.getContent());
				return new ACLMessage(ACLMessage.AGREE);
			}
			
			@Override
			protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response){
				
				log("Prepare result notification with content: " + request.getContent());
				response.setPerformative(ACLMessage.INFORM);
				
				if (request.getContent().equals("building-room-list")) {
					try {
						
						//TODO remove followings lines.. only test..
						
						// send test array...
						LinkedList<AID> aids = new LinkedList<AID>();
						
						AID room1 = new AID("room001");
						aids.add(room1);

						AID room2 = new AID("room002");
						aids.add(room2);
						
						response.setContentObject(aids);
						
						/*
						// send rooms array
						response.setContentObject(rooms);
						*/
						
					} catch (IOException e) {
						e.printStackTrace();
					}					
				}
				else if(request.getContent() == "building-room-registration"){
					
					
					
					
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
