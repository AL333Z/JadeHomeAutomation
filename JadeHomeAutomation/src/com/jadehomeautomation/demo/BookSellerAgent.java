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

package com.jadehomeautomation.demo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.StringTokenizer;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetResponder;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;

/**
 * Adapted from Giovanni Caire's Book Trading example in examples.bookTrading
 * within JADE distribution. This is the seller agent, showing how to register
 * to JADE DF in order to offer a service.
 * 
 * @author s.mariani@unibo.it
 */
public class BookSellerAgent extends Agent {
	
	/*
	 *  The catalogue of books for sale (maps the title of a book to its price).
	 */
	private Hashtable<String, Float> catalogue;

	@Override
	protected void setup() {
		
		log("I'm started.");
		catalogue = new Hashtable<String, Float>();
		/*
		 * Boot catalogue from .catalog file (drawing random prices) and print
		 * out the outcome.
		 */
		bootCatalogue();
		printCatalogue();

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
		sd.setType("book-selling");
		sd.setName("JADE-book-trading");
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
			log("Waiting for CFPs...");
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		/*
		 *  Add the behaviour serving queries from buyer agents.
		 */
		addBehaviour(new ContractNetResponder(this,
				ContractNetResponder.createMessageTemplate(
						FIPANames.InteractionProtocol.FIPA_CONTRACT_NET)){
			@Override
			protected ACLMessage handleCfp(ACLMessage cfp)
					throws RefuseException, FailureException,
					NotUnderstoodException {
				log("Received CFP '"+cfp.getContent()+"' from '"+
						cfp.getSender().getName()+"'.");
				/*
				 * We expect the title of the book as the content of the message.
				 */
				String title = cfp.getContent();
				ACLMessage reply = cfp.createReply();
				/*
				 * We check availability of the requested book.
				 */
				log("Checking availability for book '"+title+"'...");
				Float price = catalogue.get(title);
				if (price != null) {
					/*
					 * The requested book is available, reply with its price.
					 */
					log("Book '"+title+"' available, proposing price...");
					reply.setPerformative(ACLMessage.PROPOSE);
					reply.setContent(String.valueOf(price));
				} else {
					/*
					 * The requested book is NOT available, reply accordingly.
					 */
					log("Book '"+title+"' NOT available, informing client...");
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("not-available");
				}
				return reply;
			}
			@Override
			protected ACLMessage handleAcceptProposal(
					ACLMessage cfp, ACLMessage propose,
					ACLMessage accept) throws FailureException {
				log("Received purchase order '"+accept.getContent()+"' from '"+
						accept.getSender().getName()+"'.");
				/*
				 * We expect the title of the book as the content of the message.
				 */
				String title = accept.getContent();
				ACLMessage reply = accept.createReply();
				Float price = catalogue.remove(title);
				/*
				 * The requested book may be sold to another buyer in the
				 * meanwhile...
				 */
				if (price != null) {
					log("Selling book '"+title+"' to agent '"+accept.getSender().getName()+"'...");
					reply.setPerformative(ACLMessage.INFORM);
				} else {
					log("Sorry, book '"+title+"' is not available anymore.");
					reply.setPerformative(ACLMessage.FAILURE);
					reply.setContent("not-available");
				}
				return reply;
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
	
	/*
	 * Just reads books and random prices from an input file.
	 */
	private void bootCatalogue() {
		try {
			BufferedReader br = new BufferedReader(new FileReader("src/com/jadehomeautomation/demo/books.cat"));
			String line;
			StringTokenizer st;
			String title;
			LinkedList<Float> prices;
			line = br.readLine();
			while(line != null){
				st = new StringTokenizer(line, ";");
				title = st.nextToken();
				prices = new LinkedList<Float>();
				while(st.hasMoreTokens()){
					prices.add(Float.parseFloat(st.nextToken()));
				}
				catalogue.put(title, prices.get((int) Math.round(Math.random()*(prices.size()-1))));
				line = br.readLine();
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			doDelete();
		} catch (IOException e) {
			e.printStackTrace();
			doDelete();
		}
	}
	
	private void printCatalogue() {
		Enumeration<String> keys = catalogue.keys();
		String key;
		log("My catalogue is:");
		for(int i=0; i<catalogue.size(); i++){
			key = keys.nextElement();
			System.out.println("	title: "+key+" price: "+catalogue.get(key));
		}
	}
	
	private void log(String msg) {
		System.out.println("["+getName()+"]: "+msg);
	}
	
}
