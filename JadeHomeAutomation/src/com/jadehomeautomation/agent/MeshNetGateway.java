package com.jadehomeautomation.agent;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.util.Date;
import java.util.TooManyListenersException;
import java.util.Vector;

import com.mattibal.meshnet.Layer3Base;
import com.mattibal.meshnet.SerialRXTXComm;
import com.mattibal.meshnet.Layer3Base.NetworkSetupThread;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;


public class MeshNetGateway extends Agent {
	
	
	public static final String SEND_TO_DEVICE_SERVICE = "send-to-device";
	public static final String REGISTER_RECEIVE_LISTENER_SERVICE = "register-receive-listener";
	
	
	/** The MeshNet stack running on this JVM */
	Layer3Base base = null;
	
	@Override
	protected void setup(){
		
		try {
			//setupMeshNetBase();
		} catch (Exception e) {
			// TODO properly handle exceptions, if I don't somebody can use this
			// agent, but he is unable to actually exchange messages with the
			// MeshNet network.
			e.printStackTrace();
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
				
				//TODO check request type with costants or with objects..
				if (request.getContent().equals(SEND_TO_DEVICE_SERVICE)) {
					
					log("[meshnetgateway] received "+SEND_TO_DEVICE_SERVICE);
					
				} else if(request.getContent().equals(REGISTER_RECEIVE_LISTENER_SERVICE)){
					
					log("[meshnetgateway] received "+REGISTER_RECEIVE_LISTENER_SERVICE);
					
				}
				
				response.setContent("ok");
				
				return response;
			}
		});
		
		
	}
	
	
	// TODO add behaviours that let other agents exchange messages with the
	// MeshNet network using this agent as a "gateway"
	
	
	
	
	
	
	
	
	
	/**
	 * Initializes the MeshNet base protocol stack, using a local serial port
	 * as a network interface. 
	 */
	private void setupMeshNetBase() throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException, TooManyListenersException, InterruptedException{
		
		base = new Layer3Base();
		SerialRXTXComm serial = new SerialRXTXComm("/dev/ttyACM0", base);
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
