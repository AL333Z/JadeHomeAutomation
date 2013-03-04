package com.jadehomeautomation.agent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

import java.io.IOException;
import java.util.Date;
import java.util.Vector;

import com.jadehomeautomation.agent.HomeAutomation;
import com.jadehomeautomation.message.Message;

public class Controller extends Agent {
	/*
	 * The list of discovered seller agents.
	 */
	private AID[] agents;
	
	@Override
	protected void setup() {
		
		log("I'm started.");
		
		addBehaviour(new TickerBehaviour(this, 5000) {
			
			@Override
			protected void onTick() {
				
				log("Trying to switch bulb ...");
				
				/*
				 * 1- Create the agent description template.
				 */
				DFAgentDescription template = new DFAgentDescription();
				/*
				 * 2- Create the service description template.
				 */
				ServiceDescription sd = new ServiceDescription();
				/*
				 * 3- Fill its fields you look for.
				 */
				sd.setType(HomeAutomation.SERVICE_BULB_CONTROL);
				/*
				 * 4- Add the service template to the agent template.
				 */
				template.addServices(sd);
				/*
				 * 5- Setup your preferred search constraints.
				 */
				SearchConstraints all = new SearchConstraints();
				all.setMaxResults(new Long(-1));
				DFAgentDescription[] result = null;
				try {
					/*
					 * 6- Query the DF about the service you look for.
					 */
					log("Searching '"+sd.getType()+"' service in the default DF...");
					
					result = DFService.search(myAgent, template, all);
					agents = new AID[result.length];
					for (int i = 0; i < result.length; ++i) {
						/*
						 * 7- Collect found service providers' AIDs.
						 */
						agents[i] = result[i].getName();
						log("Agent '"+agents[i].getName()+"' found.");
					}
				} catch (FIPAException fe) {
					fe.printStackTrace();
				}
				
				if(result.length != 0){
					ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
					req.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
					for (int i = 0; i < agents.length; ++i) {
						log("Sending REQUEST for bulb to agent '"+
								agents[i].getName()+"'...");
						req.addReceiver(agents[i]);
					} 
					
					Message mess = new Message(HomeAutomation.SERVICE_BULB_CONTROL, getAID());
					try {
						req.setContentObject(mess);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					// Timeout is 10 seconds.
					 
					req.setReplyByDate(new Date(System.currentTimeMillis() + 10000));

					AchieveREInitiator reInitiator = new AchieveREInitiator(this.myAgent, req){
						protected void handleInform(ACLMessage inform) {
							System.out.println("Agent "+inform.getSender().getName()+" successfully performed the requested action");
						}
						protected void handleRefuse(ACLMessage refuse) {
							System.out.println("Agent "+refuse.getSender().getName()+" refused to perform the requested action");
						}
						protected void handleFailure(ACLMessage failure) {
							if (failure.getSender().equals(myAgent.getAMS())) {
								// FAILURE notification from the JADE runtime: the receiver
								// does not exist
								System.out.println("Responder does not exist");
							}
							else {
								System.out.println("Agent "+failure.getSender().getName()+" failed to perform the requested action");
							}
						}
						protected void handleAllResultNotifications(Vector notifications) {
							log("HandleAllResultNotification");
						}
					};
					
					myAgent.addBehaviour(reInitiator);	
				}
			}	
		} );
	}

	@Override
	protected void takeDown() {
		log("I'm done.");
	}
	
	private void log(String msg) {
		System.out.println("["+getName()+"]: "+msg);
	}
}
