package com.jadehomeautomation.agent;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
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
import java.util.TooManyListenersException;

import com.jadehomeautomation.message.MeshNetToDeviceMessage;
import com.mattibal.meshnet.Device;
import com.mattibal.meshnet.Layer3Base;
import com.mattibal.meshnet.SerialRXTXComm;


public class MeshNetGateway extends Agent {
	
	
	public static final String SEND_TO_DEVICE_SERVICE = "send-to-device";
	public static final String REGISTER_RECEIVE_LISTENER_SERVICE = "register-receive-listener";
	
	
	/** The MeshNet stack running on this JVM */
	Layer3Base base = null;
	
	
	@Override
	protected void setup(){
		
		try {
			setupMeshNetBase();
		} catch (Exception e) {
			// TODO properly handle exceptions, if I don't somebody can use this
			// agent, but he is unable to actually exchange messages with the
			// MeshNet network.
			e.printStackTrace();
			System.err.println("[MeshNetGateway] MeshNet base setup failed!! don't use me!!!");
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
					
					if(requestObj instanceof MeshNetToDeviceMessage){
						MeshNetToDeviceMessage msg = (MeshNetToDeviceMessage) requestObj;
						
						// use MeshNet libraries to send the message!
						int command = msg.getCommand();
						byte[] data = msg.getDataBytes();
						int devId = msg.getDestinationDeviceId();
						Device dev = Device.getDeviceFromUniqueId(devId);
						try {
							dev.getLayer4().sendCommandRequest(command, data);
						} catch (IOException e) {
							e.printStackTrace();
							// TODO set an error message in the "response" message?
						}
					}
					
					// TODO handle the other message type: RegisterReceiveListener
					
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
