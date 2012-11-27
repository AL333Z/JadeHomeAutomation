/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
 *****************************************************************/

package chat.client.agent;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

import jade.content.ContentManager;
import jade.content.Predicate;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREInitiator;
import jade.util.Logger;
import jade.util.leap.Iterator;
import jade.util.leap.Set;
import jade.util.leap.SortedSetImpl;
import chat.ontology.ChatOntology;
import chat.ontology.Joined;
import chat.ontology.Left;
import android.content.Intent;
import android.content.Context;

/**
 * This agent implements the logic of the chat client running on the user
 * terminal. User interactions are handled by the ChatGui in a
 * terminal-dependent way. The ChatClientAgent performs 3 types of behaviours: -
 * ParticipantsManager. A CyclicBehaviour that keeps the list of participants up
 * to date on the basis of the information received from the ChatManagerAgent.
 * This behaviour is also in charge of subscribing as a participant to the
 * ChatManagerAgent. - ChatListener. A CyclicBehaviour that handles messages
 * from other chat participants. - ChatSpeaker. A OneShotBehaviour that sends a
 * message conveying a sentence written by the user to other chat participants.
 * 
 * @author Giovanni Caire - TILAB
 */
public class ChatClientAgent extends Agent {
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
					// request the list of rooms
					ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
					req.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
					for (int i = 0; i < agents.length; ++i) {
						log("Sending REQUEST for list of rooms to agent '"+
								agents[i].getName()+"'...");
						req.addReceiver(agents[i]);
					} 
					
					//TODO add request type with costants or with objects..
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
							
							//TODO add logic..
							for(AID aid : aids){
								System.out.println("++++Room: " +aid.getName()+ "++++");
								
								// perform action on devices..
								getDevices(aid);
								
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
	
	protected void getDevices(AID roomAID) {
		ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
		req.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);

		log("Sending REQUEST for list of devices to agent '"+ roomAID +"'...");
		req.addReceiver(roomAID);
	
		//TODO add request type with costants or with objects..
		req.setContent("room-device-list");
		
		/*
		 * Timeout is 10 seconds.
		 */
		req.setReplyByDate(new Date(System.currentTimeMillis() + 10000));

		//
		AchieveREInitiator reInitiator = new AchieveREInitiator(this, req){
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
				
				//TODO add logic..
				for(AID aid : aids){
					System.out.println("++++Device: " +aid.getName()+ "++++");
					
					log("Ready to do something..");
					
					switchBulb(aid);
					
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
		
		this.addBehaviour(reInitiator);
	}

	protected void switchBulb(AID deviceAID) {
		ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
		req.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);

		log("Sending REQUEST to switch bulb '"+ deviceAID +"'...");
		req.addReceiver(deviceAID);
	
		//TODO add request type with costants or with objects..
		req.setContent("bulb-control");
		
		/*
		 * Timeout is 10 seconds.
		 */
		req.setReplyByDate(new Date(System.currentTimeMillis() + 10000));

		AchieveREInitiator reInitiator = new AchieveREInitiator(this, req){
			protected void handleInform(ACLMessage inform) {
				log("Agent "+inform.getSender().getName()+" successfully performed the requested action");
				
				String str = null;
				try {
					str = (String)inform.getContentObject();
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
		
		this.addBehaviour(reInitiator);
	}
	
	@Override
	protected void takeDown() {
		log("I'm done.");
	}
	
	private void log(String msg) {
		System.out.println("["+getName()+"]: "+msg);
	}
}
