package com.jadehomeautomation.agent;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
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
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TooManyListenersException;

import com.jadehomeautomation.message.MeshNetCommandMsg;
import com.jadehomeautomation.message.MeshNetRegisterListenerMsg;
import com.mattibal.meshnet.Device;
import com.mattibal.meshnet.Layer3Base;
import com.mattibal.meshnet.SerialRXTXComm;
import com.mattibal.meshnet.Layer4SimpleRpc;


@SuppressWarnings("serial")
public class MeshNetGateway extends Agent implements Device.CommandReceivedListener {
	
	
	public static final String SEND_TO_DEVICE_SERVICE = "send-to-device";
	public static final String REGISTER_RECEIVE_LISTENER_SERVICE = "register-receive-listener";
	
	
	/** The MeshNet base stack running on this JVM */
	Layer3Base base = null;
	
	
	/** 
	 * The listeners of a received packet destined to a specific device.
	 * 
	 * The outer map, maps a MeshNet device id to a set of the AID
	 * of agents that want to be notified for the messages destined
	 * to that device id. 
	 */
	Map<Integer, HashSet<AID>> receiveListeners = new HashMap<Integer, HashSet<AID>>();
	
	
	@Override
	protected void setup(){
		
		try {
			setupMeshNetBase();
			log("network setup completed!!");
		} catch (Exception e) {
			// TODO properly handle exceptions, if I don't somebody can use this
			// agent, but he is unable to actually exchange messages with the
			// MeshNet network.
			e.printStackTrace();
			log("MeshNet base setup failed!! don't use me, because I will silently completely ignore your messages!!!");
		}
		
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		
		// Create the services description.
		
		ServiceDescription sendToDeviceService = new ServiceDescription();
		sendToDeviceService.setType(SEND_TO_DEVICE_SERVICE);
		sendToDeviceService.setName(SEND_TO_DEVICE_SERVICE);
		
		ServiceDescription registerReceiveListenerService = new ServiceDescription();
		registerReceiveListenerService.setType(REGISTER_RECEIVE_LISTENER_SERVICE);
		registerReceiveListenerService.setName(REGISTER_RECEIVE_LISTENER_SERVICE);
		
		// Add the services description to the agent description.

		//TODO add here other Sevice Descriptions
		dfd.addServices(sendToDeviceService);
		dfd.addServices(registerReceiveListenerService);
		
		try {
			// Register the service
			log("Registering '"+sendToDeviceService.getType()+"' service named '"+sendToDeviceService.getName()+"'" + "to the default DF...");
			
			DFService.register(this, dfd);
			
			log("Waiting for request...");
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		
		MessageTemplate template = AchieveREResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_REQUEST);
		addBehaviour(new AchieveREResponder(this, template){
			
			@Override
			protected ACLMessage handleRequest(ACLMessage request) 
				throws NotUnderstoodException, RefuseException{
				
				log("Handle request");
				return new ACLMessage(ACLMessage.AGREE);
			}
			
			@Override
			protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response){
				
				log("Prepare result notification");
				response.setPerformative(ACLMessage.INFORM);
				
				try{
					Object requestObj = request.getContentObject();
					
					// handling received messages in the Akka style!! :)
					
					if(requestObj instanceof MeshNetCommandMsg){
						MeshNetCommandMsg msg = (MeshNetCommandMsg) requestObj;
						
						// use MeshNet libraries to send the message!
						int command = msg.getCommand();
						byte[] data = msg.getDataBytes();
						int devId = msg.getMeshNetDeviceId();
						Device dev = Device.getDeviceFromUniqueId(devId);
						if(dev != null){
							try {
								dev.sendCommand(command, data);
							} catch (IOException e) {
								e.printStackTrace();
								// TODO set an error message in the "response" message?
							}
						} else {
							// There is no device with this ID connected to the MeshNet network
							return response; // TODO should put an error message in "response"?
						}
					} else if(requestObj instanceof MeshNetRegisterListenerMsg){
						MeshNetRegisterListenerMsg msg = (MeshNetRegisterListenerMsg) requestObj;
						
						int deviceId = msg.getDeviceId();
						AID listenerAid = msg.getListenerAid();
						
						// Register my object to the MeshNet stack as a listener
						Device dev = Device.getDeviceFromUniqueId(deviceId);
						dev.addCommandReceivedListener(MeshNetGateway.this);
						
						// Put the agent in my "list" of listeners
						HashSet<AID> devListeners = receiveListeners.get(deviceId);
						if(devListeners == null){
							devListeners = new HashSet<AID>();
							receiveListeners.put(deviceId, devListeners);
						}
						devListeners.add(listenerAid);
					}
			
					
				} catch(UnreadableException e){
					e.printStackTrace();
				}
				
				response.setContent("ok"); // TODO is correct??
				
				return response;
			}
		});
		
		
	}
	
	
	
	/**
	 * Initializes the MeshNet base protocol stack, using a local serial port
	 * as a network interface. 
	 */
	private void setupMeshNetBase() throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException, TooManyListenersException, InterruptedException{
		
		base = new Layer3Base();
		SerialRXTXComm serial = new SerialRXTXComm("/dev/ttyUSB0", base);
		Thread.sleep(4000);
		Layer3Base.NetworkSetupThread setup = base.new NetworkSetupThread();
		Thread setupThread = new Thread(setup);
		setupThread.start();
		setupThread.join();
		// Now the network should be ready!
		
	}
	
	
	
	/**
	 * This method is called by the MeshNet stack when a packet is sent to
	 * a device id that I'm listening to.
	 */
	@Override
	public void onCommandReceived(final int command, final int deviceId, final ByteBuffer data) {
		
		/*System.out.print("MeshNetGateway onCommandReceived command="+command+" deviceId="+deviceId+" data=");
		byte[] dataA = data.array();
		for(byte b : dataA){
			System.out.print(b+" ");
		}
		System.out.println("");*/
		
		addBehaviour(new OneShotBehaviour(this) {
			@Override
			public void action() {
				
				// Send a message to the agents registered as listeners
				
				log("received packet from deviceId: "+deviceId+" command:"+command);
				
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
				
				HashSet<AID> listeners = receiveListeners.get(deviceId);
				if(listeners!=null){
					for(AID listener : listeners){
						msg.addReceiver(listener);
					}
				}
				
				MeshNetCommandMsg cmdMsg = new MeshNetCommandMsg(deviceId, data.array(), command);
				try {
					msg.setContentObject(cmdMsg);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				send(msg);
				
				// Old (bad) way to send the message
				/*msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
				AchieveREInitiator reInitiator = new AchieveREInitiator(this.myAgent, msg){
					@Override
					protected void handleInform(ACLMessage inform) {
						stop();
					}
				};
				myAgent.addBehaviour(reInitiator);*/
				
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
