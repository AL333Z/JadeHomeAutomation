package com.jadehomeautomation.agent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import jade.proto.SubscriptionResponder;
import jade.util.leap.LinkedList;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Vector;

import com.jadehomeautomation.message.MeshNetCommandMsg;
import com.jadehomeautomation.message.MeshNetRegisterListenerMsg;
import com.jadehomeautomation.message.Message;
import com.jadehomeautomation.message.RegistrationMessage;
import com.jadehomeautomation.message.SubscriptionMessage;
import com.mattibal.meshnet.Device;

public class ToggleSwitch extends Agent {
	
	/** Room of the device */
	private AID roomAID;
	
	private String id;
	private String roomId;
	private String name;
	private String description;
	
	private HashSet<AID> subscribedAgents = new HashSet<AID>();
	
	
	
	/** The ID of the device in MeshNet network where there is this toggleswitch */
	private int meshnetDeviceId;
	
	/** The command number that the MeshNet device launch when the switch is pressed */
	public static final int MESHNET_COMMAND = 5;

	@Override
	protected void setup() {		
		this.roomAID = null;
		
		Object[] args = getArguments();
		if (args != null) {
			if (args.length > 0) this.id = (String) args[0]; 
			if (args.length > 1) this.roomId = (String) args[1]; 
			if (args.length > 2) this.name = (String) args[2]; 
			if (args.length > 3) this.description = (String) args[3];
			if (args.length > 4) this.meshnetDeviceId = Integer.parseInt((String) args[4]);
			System.out.println("Created Bulb with id "+ this.id +" name " + this.name + " descr " + this.description);
		}
		
		// Register the device to a room
		
		/*
		 * Check every 5 seconds a room to couple with..
		 */
		addBehaviour(new TickerBehaviour(this, 5000) {
			
			@Override
			protected void onTick() {
				
				log("Trying to get the list of room Agents to couple with...");
				
				ServiceDescription sd = new ServiceDescription();
				sd.setType(HomeAutomation.SERVICE_ROOM_DEVICE_REGISTRATION);
				
				DFAgentDescription template = new DFAgentDescription();
				template.addServices(sd);

				SearchConstraints all = new SearchConstraints();
				all.setMaxResults(new Long(-1));
				
				DFAgentDescription[] result = null;
				AID[] agents = null;
				
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
			
					for(AID aid : agents ){
						log("Added receiver "+aid);
						req.addReceiver(aid);
					}
					
					Message mess = new Message(HomeAutomation.SERVICE_ROOM_DEVICE_REGISTRATION, getAID());
					try {
						req.setContentObject(mess);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					RegistrationMessage regMessage = new RegistrationMessage(HomeAutomation.SERVICE_ROOM_DEVICE_REGISTRATION, getAID(), roomId, name, description);
					try {
						req.setContentObject(regMessage);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					// Timeout is 10 seconds.
					req.setReplyByDate(new Date(System.currentTimeMillis() + 10000));

					//
					AchieveREInitiator reInitiator = new AchieveREInitiator(this.myAgent, req){
						protected void handleInform(ACLMessage inform) {
							log("Agent "+inform.getSender().getName()+" successfully performed the requested action");
							
							roomAID = inform.getSender();
							
							// start listening requests..
							registerDevice();
							
							// stop loocking for building
							stop();
							
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
				
				// Register to MeshNetGateway to receive message from the device
				DFAgentDescription template2 = new DFAgentDescription();
				ServiceDescription sd2 = new ServiceDescription();
				sd2.setType(MeshNetGateway.REGISTER_RECEIVE_LISTENER_SERVICE);
				template2.addServices(sd2);
				SearchConstraints all2 = new SearchConstraints();
				all2.setMaxResults(new Long(-1));
				result = null;
				try {
					log("Searching '"+sd.getType()+"' service in the default DF...");

					result = DFService.search(myAgent, template2, all2);
					agents = new AID[result.length];

					ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
					req.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
					
					for(int i = 0; i < agents.length; i++){
						agents[i] = result[i].getName();
						log("Agent '"+agents[i].getName()+"' found.");

						log("Sending REQUEST for register for messages from a MeshNet device.. '"+
								agents[i].getName()+"'...");
						req.addReceiver(agents[i]);
					}


					MeshNetRegisterListenerMsg msg = new MeshNetRegisterListenerMsg(meshnetDeviceId, myAgent.getAID());
					
					req.setContentObject(msg);

					// Timeout is 10 seconds.
					req.setReplyByDate(new Date(System.currentTimeMillis() + 10000));

					AchieveREInitiator reInitiator = new AchieveREInitiator(myAgent, req){
						protected void handleInform(ACLMessage inform) {
							log("Agent "+inform.getSender().getName()+" successfully registered for incoming messages from MeshNet device");

						}
						protected void handleRefuse(ACLMessage refuse) {
							log("Agent "+refuse.getSender().getName()+" refused registration for incoming messages from MeshNet device");
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
//							log("HandleAllResultNotification");
						}
					};

					myAgent.addBehaviour(reInitiator);

				} catch (FIPAException fe) {
					fe.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}	
		} );
		
		
		
		// Code to be executed when I receive a message from the MeshNet device
		
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
				
				try{
					Object requestObj = request.getContentObject();
					
					// handling received messages in the Akka style!! :)
					
					if(requestObj instanceof MeshNetCommandMsg){
						MeshNetCommandMsg msg = (MeshNetCommandMsg) requestObj;
						
						// Read the incoming message
						int command = msg.getCommand();
						byte[] data = msg.getDataBytes();
						int devId = msg.getMeshNetDeviceId();
						if(devId == meshnetDeviceId){
							if(command == MESHNET_COMMAND){
								
								// Send a message to the subscribed agents
								
								ACLMessage notification = new ACLMessage(ACLMessage.INFORM);
								notification.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
							
								for (AID aid : subscribedAgents) {
									notification.addReceiver(aid);
								}
					
								notification.setContent("daje cazzo");

								send(notification);
							}
						}
					}
				} catch(UnreadableException e){
					e.printStackTrace();
				}
				
				response.setContent("ok"); // TODO is correct??
				
				return response;
			}
		});
		
	}
	
	/*
	 * Method that register the device agent to the df and defines requests management  
	 */
	protected void registerDevice() {
		log("I'm started.");

		// Create the agent description.
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		
		// Create the service description.
		ServiceDescription sd = new ServiceDescription();
		sd.setType(HomeAutomation.SERVICE_TOGGLESWICTH_LISTEN);
		sd.setName("JADE-toggleswitch-control");
		
		// Add the service description to the agent description.
		dfd.addServices(sd);
		try {
			// Register the service (through the agent description multiple
			log("Registering '"+sd.getType()+"' service named '"+sd.getName()+"'" + "to the default DF...");
			
			DFService.register(this, dfd);
			
			log("Waiting for request...");
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		ParallelBehaviour parallelBehaviour = new ParallelBehaviour();
		parallelBehaviour.addSubBehaviour(this.subscribeResponderBehavior());
		//parallelBehaviour.addSubBehaviour(fooBehaviour());
		
		addBehaviour(parallelBehaviour);
	}
	
	// return the behaviour for subscribe initiators
	protected Behaviour subscribeResponderBehavior() {
		MessageTemplate template = MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);			
		return new SubscriptionResponder(this, template){
			
			@Override
			protected ACLMessage handleSubscription(ACLMessage subscription) {
				ACLMessage res = subscription.createReply();

				// check toggleswith id
				SubscriptionMessage message = null;
				try {
					message = (SubscriptionMessage) subscription.getContentObject();
					
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if (message.getToggleSwitchId().equals(id)) {
					// perform registartion
					try {
						log("perform registration");
						subscribedAgents.add(subscription.getSender());
						
						res.setPerformative(ACLMessage.AGREE);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						
						res.setPerformative(ACLMessage.REFUSE);
						e.printStackTrace();
					}
				}
				else {
					res.setPerformative(ACLMessage.REFUSE);
					log("refuse registration");
				}
				
				return res;
			}
			
			@Override
			protected ACLMessage handleCancel(ACLMessage cancel) {
				log("perform cancel");
				
				subscribedAgents.remove(cancel.getSender());
				return null;
			}
		};
	}
	
	//TODO remove this behaviour once test will be over
	// return behavior to do some test stuff
	/*protected Behaviour fooBehaviour() {
		return new TickerBehaviour(this, 5000) {
			
			@Override
			protected void onTick() {
				log("sending notification..");
				// creating msg
				ACLMessage notification = new ACLMessage(ACLMessage.INFORM);
				notification.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
			
				for (int i = 0; i < subscribedAgents.size() ; i++) {
					AID aid = (AID) subscribedAgents.get(i);
					notification.addReceiver(aid);
				}
	
				notification.setContent("daje cazzo");

				send(notification);
			}
		};
	}*/
	
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
