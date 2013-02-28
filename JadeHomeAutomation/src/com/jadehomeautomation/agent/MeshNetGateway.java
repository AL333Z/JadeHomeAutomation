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
		}
		
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
