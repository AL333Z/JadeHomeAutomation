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
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREInitiator;

import java.util.Date;
import java.util.LinkedList;
import java.util.Vector;


/*
 * This class is used to test the function of returning the list of the rooms in a building.
 */

public class RoomListController extends Agent {
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
				
				log("Trying to get the list of rooms from Building Agents ...");
				
				//TODO use constant for types..
				ServiceDescription sd = new ServiceDescription();
				sd.setType("building-room-list");
				
				DFAgentDescription template = new DFAgentDescription();
				template.addServices(sd);

				SearchConstraints all = new SearchConstraints();
				all.setMaxResults(new Long(-1));
				DFAgentDescription[] result = null;
				try {

					log("Searching '"+sd.getType()+"' service in the default DF...");
					
					result = DFService.search(myAgent, template, all);
					agents = new AID[result.length];
					for (int i = 0; i < result.length; ++i) {

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
						log("Sending REQUEST for list of rooms to agent '"+
								agents[i].getName()+"'...");
						req.addReceiver(agents[i]);
					} 
					
					//TODO put String constant instead of this message..
					req.setContent("building-room-list");
					
					/*
					 * Timeout is 10 seconds.
					 */
					req.setReplyByDate(new Date(System.currentTimeMillis() + 10000));

					//
					AchieveREInitiator reInitiator = new AchieveREInitiator(this.myAgent, req){
						protected void handleInform(ACLMessage inform) {
							log("Agent "+inform.getSender().getName()+" successfully performed the requested action");
							
							LinkedList<AID> aids = null;
							try {
								LinkedList<AID> contentObject = (LinkedList<AID>)inform.getContentObject();
								aids = contentObject;
							} catch (UnreadableException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
		
							for(AID aid : aids){
								System.out.println("++++Resp: " +aid.getName());
							}
							
						}
						protected void handleRefuse(ACLMessage refuse) {
							log("Agent "+refuse.getSender().getName()+" refused to perform the requested action");
						}
						protected void handleFailure(ACLMessage failure) {
							if (failure.getSender().equals(myAgent.getAMS())) {
								// FAILURE notification from the JADE runtime: the receiver
								// does not exist
								log("Responder does not exist");
							}
							else {
								log("Agent "+failure.getSender().getName()+" failed to perform the requested action");
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