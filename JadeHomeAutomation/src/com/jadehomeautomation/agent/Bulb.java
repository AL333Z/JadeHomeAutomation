package com.jadehomeautomation.agent;

import java.util.Hashtable;

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
import jade.proto.ContractNetResponder;
import jade.proto.AchieveREResponder;

public class Bulb extends Agent {

	private boolean state;
	@Override
	protected void setup() {		
		this.state = false;
		
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
		 *  3- Create the service description.
		 */
		ServiceDescription sd = new ServiceDescription();
		/*
		 *  4- Fill its mandatory fields.
		 */
		sd.setType("bulb-control");
		sd.setName("JADE-buld-control");
		/*
		 *  5- Add the service description to the agent description.
		 */
		dfd.addServices(sd);
		try {
			/*
			 *  6- Register the service (through the agent description multiple
			 *  services can be registered in one shot).
			 */
			log("Registering '"+sd.getType()+"' service named '"+sd.getName()+"'" +
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
				
				return new ACLMessage(ACLMessage.AGREE);
			}
			
			@Override
			protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response)
				throws FailureException{
				
				log("Prepare result");
				
				return new ACLMessage(ACLMessage.INFORM);
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
