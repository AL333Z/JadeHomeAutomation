package com.jadehomeautomation.android;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
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

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.Vector;
import java.util.logging.Level;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.ArrayAdapter;

import com.jadehomeautomation.agent.HomeAutomation;
import com.jadehomeautomation.android.RoomsActivity.RoomItems;
import com.jadehomeautomation.message.AgentMessage;
import com.jadehomeautomation.message.Message;


/*
 * This class is used to test the function of returning the list of the rooms in a building, 
 * retrieve devices in every room and perform simple action
 */

public class SampleController extends Agent {
	/*
	 * The list of discovered seller agents.
	 */
	private AID[] agents;
	
	private Context context;
	private MyReceiver myReceiver;

	@Override
	protected void setup() {
		
		log("I'm started.");
		
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			if (args[0] instanceof Context) {
				context = (Context) args[0];
				
				// Register the Intent receiver
				myReceiver = new MyReceiver();
				IntentFilter intFilter = new IntentFilter();
				intFilter.addAction(RoomsActivity.ROOM_SELECTED);
				intFilter.addAction(DevicesActivity.DEVICE_SELECTED);
				context.registerReceiver(myReceiver, intFilter);
			}
		}
			
		addBehaviour(new OneShotBehaviour(this) {
			
			@Override
			public void action() {
				log("Trying to get the list of rooms from Building Agents to forward to roomsActivity");
				
				ServiceDescription sd = new ServiceDescription();
				sd.setType(HomeAutomation.SERVICE_BUILDING_ROOM_LIST);
				
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
		
					Message message = new Message(HomeAutomation.SERVICE_BUILDING_ROOM_LIST, getAID());
					try {
						req.setContentObject(message);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					// Timeout is 10 seconds.
					req.setReplyByDate(new Date(System.currentTimeMillis() + 10000));

					AchieveREInitiator reInitiator = new AchieveREInitiator(this.myAgent, req){
						protected void handleInform(ACLMessage inform) {
							log("Agent "+inform.getSender().getName()+" successfully performed the requested action");
							
							LinkedList<AgentMessage> agentsDes = null;
							try {
								agentsDes = (LinkedList<AgentMessage>)inform.getContentObject();
								//rooms = new Room[agentsDes.size()];
							} catch (UnreadableException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
										
							log("number of rooms found: "+agentsDes.size());
							
							// Prepare objects to pass to roomsActivity
							String[] roomNames = new String[agentsDes.size()];
							AID[] aids = new AID[agentsDes.size()];
							
							for (int i = 0; i < agentsDes.size(); i++) {
								AgentMessage agentMsg = agentsDes.get(i);
								System.out.println("++++Room: " +agentMsg.getName()+ "++++");
								
								roomNames[i] = agentMsg.getName();
								aids[i] = agentMsg.getAid();
							}

							RoomsActivity.RoomItems roomItems = new RoomsActivity.RoomItems(roomNames, aids); 
							
							// Send the RoomItems object (serialized) to the Activity that will display them
							Intent broadcast = new Intent();
							broadcast.setAction(RoomsActivity.ROOM_LIST);
							broadcast.putExtra(RoomsActivity.ROOM_LIST_EXTRA, roomItems);
							context.sendBroadcast(broadcast);
							
						}
						protected void handleRefuse(ACLMessage refuse) {
							log("Agent "+refuse.getSender().getName()+" refused to perform the requested action");
						}
						protected void handleFailure(ACLMessage failure) {
							if (failure.getSender().equals(myAgent.getAMS())) {
								// FAILURE notification from the JADE runtime: the receiver does not exist
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
		});
		
		/*
		addBehaviour(new TickerBehaviour(this, 5000) {
			
			@Override
			protected void onTick() {
				
				// --------- Mattia - codice di esempio --------------------------
				
				// Creo una lista di item di stanze fasulle da mandare all'activity
				String[] roomNames = {"Cucina", "Bagno", "Salotto"};
				AID[] aids = {new AID(), new AID(), new AID()};
				RoomsActivity.RoomItems roomItems = 
					new RoomsActivity.RoomItems(roomNames, aids); 
				
				// Send the RoomItems object (serialized) to the Activity that will display them
				Intent broadcast = new Intent();
				broadcast.setAction(RoomsActivity.ROOM_LIST);
				broadcast.putExtra(RoomsActivity.ROOM_LIST_EXTRA, roomItems);
				context.sendBroadcast(broadcast);
				// ----------------------------------------------------------------
				
				
				
				log("Trying to get the list of rooms from Building Agents ...");
				
				ServiceDescription sd = new ServiceDescription();
				sd.setType(HomeAutomation.SERVICE_BUILDING_ROOM_LIST);
				
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
		
					Message message = new Message(HomeAutomation.SERVICE_BUILDING_ROOM_LIST, getAID());
					try {
						req.setContentObject(message);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					// Timeout is 10 seconds.
					req.setReplyByDate(new Date(System.currentTimeMillis() + 10000));

					AchieveREInitiator reInitiator = new AchieveREInitiator(this.myAgent, req){
						protected void handleInform(ACLMessage inform) {
							log("Agent "+inform.getSender().getName()+" successfully performed the requested action");
							
							LinkedList<AgentMessage> agentsDes = null;
							try {
								agentsDes = (LinkedList<AgentMessage>)inform.getContentObject();
								//rooms = new Room[agentsDes.size()];
							} catch (UnreadableException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							//TODO add logic..
							
							for(AgentMessage agentMessage : agentsDes){
								System.out.println("++++Room: " +agentMessage.getName()+ "++++");
								
								
								
								// perform action on devices..
								getDevices(agentMessage.getAid());
								
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
		*/
	}
	
	/**
	 * This receives the Intents (messages) from the Activity
	 */
	private class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equalsIgnoreCase(RoomsActivity.ROOM_SELECTED)) {
			
				Serializable obj = intent.getSerializableExtra(RoomsActivity.ROOM_AID_EXTRA);
				if(obj instanceof AID){
				
					AID roomAid = (AID) obj;
					
					// I think here we can just add an OneTickBehaviour
					// that will do what we want to do, not directly interact
					// with the Agent, because the code here is executed by
					// an Android thread different from the Jade agent thread!!
					
					log("room agent selected in the list: " + roomAid.getName());
					
					// get devices in the selected room
					getDevicesAndNotify(roomAid);
					
				}
			}
			else if (action.equalsIgnoreCase(DevicesActivity.DEVICE_SELECTED)) {
				Serializable obj = intent.getSerializableExtra(DevicesActivity.DEVICE_AID_EXTRA);
				if(obj instanceof AID){
				
					AID deviceAID = (AID) obj;
					
					// I think here we can just add an OneTickBehaviour
					// that will do what we want to do, not directly interact
					// with the Agent, because the code here is executed by
					// an Android thread different from the Jade agent thread!!
					
					log("device agent selected in the list: " + deviceAID.getName());
					
					//TODO add some logic for different device types: now we are using only one kind of device (bulb)...
					
					// send action to device
					switchBulb(deviceAID);
					
				}
			}
		}
	}
	
	protected void getDevicesAndNotify(AID roomAID) {
		ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
		req.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);

		log("Sending REQUEST for list of devices to agent '"+ roomAID +"'...");
		req.addReceiver(roomAID);
			
		Message mess = new Message(HomeAutomation.SERVICE_ROOM_DEVICE_LIST, getAID());
		try {
			req.setContentObject(mess);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// Timeout is 10 seconds.
		req.setReplyByDate(new Date(System.currentTimeMillis() + 10000));

		AchieveREInitiator reInitiator = new AchieveREInitiator(this, req){
			protected void handleInform(ACLMessage inform) {
				log("Agent "+inform.getSender().getName()+" successfully performed the requested action");
				
				LinkedList<AgentMessage> agentMessages = null;
				try {
					agentMessages = (LinkedList<AgentMessage>)inform.getContentObject();
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
								
				log("number of devices found: "+agentMessages.size());
				
				// Prepare devices object to pass to roomsActivity
				String[] devicesName = new String[agentMessages.size()];
				AID[] aids = new AID[agentMessages.size()];
				
				for (int i = 0; i < agentMessages.size(); i++) {
					AgentMessage agentMsg = agentMessages.get(i);
					System.out.println("++++Device: " +agentMsg.getName()+ "++++");
					
					devicesName[i] = agentMsg.getName();
					aids[i] = agentMsg.getAid();
				}

				DevicesActivity.DeviceItems deviceItem = new DevicesActivity.DeviceItems(devicesName, aids); 
				
				// Send the RoomItems object (serialized) to the Activity that will display them
				Intent broadcast = new Intent();
				broadcast.setAction(DevicesActivity.DEVICE_LIST);
				broadcast.putExtra(DevicesActivity.DEVICE_LIST_EXTRA, deviceItem);
				context.sendBroadcast(broadcast);
				
				log("devices send to RoomsActivity.");
			}
			protected void handleRefuse(ACLMessage refuse) {
				log("Agent "+refuse.getSender().getName()+" refused to perform the requested action");
			}
			protected void handleFailure(ACLMessage failure) {
				if (failure.getSender().equals(myAgent.getAMS())) {
					// FAILURE notification from the JADE runtime: the receiver does not exist
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
	
		Message mess = new Message(HomeAutomation.SERVICE_BULB_CONTROL, getAID());
		try {
			req.setContentObject(mess);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// Timeout is 10 seconds.
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
