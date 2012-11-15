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
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.StringTokenizer;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;

/**
 * Adapted from Giovanni Caire's Book Trading example in examples.bookTrading
 * within JADE distribution. This is the buyer agent, showing how to query
 * JADE DF in order to look for a desired service.
 * 
 * @author s.mariani@unibo.it
 */
public class BookBuyerAgent extends Agent {

	/*
	 * The title of the book to buy.
	 */
	private String targetBookTitle;
	/*
	 * The list of discovered seller agents.
	 */
	private AID[] sellerAgents;
	/*
	 * The agent who provides the best offer.
	 */
	private AID bestSeller;
	/*
	 * The best offered price.
	 */
	private float bestPrice;
	/*
	 * Overall number of book trading attempts, used for termination.
	 */
	private int overallAttempts = 0;

	@Override
	protected void setup() {
		
		log("I'm started.");
		/*
		 * Periodic behaviour performing random book requests.
		 */
		addBehaviour(new TickerBehaviour(this, 10000) {
			
			@Override
			protected void onTick() {
				
				/*
				 * Termination condition.
				 */
				if(overallAttempts==30)
					stop();
				/*
				 * Randomly draw the book to buy from .catalog file.
				 */
				targetBookTitle = bootBookTitle();
				/*
				 * Resets fields and increase attempts counter.
				 */
				bestSeller = null;
				bestPrice = 0f;
				overallAttempts++;
				log("Trying to buy '"+targetBookTitle+"'...");
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
				sd.setType("book-selling");
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
					sellerAgents = new AID[result.length];
					for (int i = 0; i < result.length; ++i) {
						/*
						 * 7- Collect found service providers' AIDs.
						 */
						sellerAgents[i] = result[i].getName();
						log("Agent '"+sellerAgents[i].getName()+"' found.");
					}
				} catch (FIPAException fe) {
					fe.printStackTrace();
				}

				/*
				 * If we found at least one agent offering the desired service,
				 * we try to buy the book using a custom FSM-like behaviour.
				 */
				if(result.length != 0){
					ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
					cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
					for (int i = 0; i < sellerAgents.length; ++i) {
						log("Sending CFP for book '"+targetBookTitle+"' to agent '"+
								sellerAgents[i].getName()+"'...");
						cfp.addReceiver(sellerAgents[i]);
					} 
					cfp.setContent(targetBookTitle);
					/*
					 * Timeout is 10 seconds.
					 */
					cfp.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
					/*
					 * Here's the ContractNetInitiator handling all the
					 * conversation stages.
					 */
					ContractNetInitiator cni = new ContractNetInitiator(myAgent, cfp){
						/*
						 * A PROPOSAL arrived.
						 */
						@Override
						protected void handlePropose(ACLMessage propose, java.util.Vector acceptances) {
							log("Received proposal '"+propose.getContent()+"' from '"+
									propose.getSender().getName()+"'.");
						};
						/*
						 * A REFUSAL arrived.
						 */
						@Override
						protected void handleRefuse(ACLMessage refuse) {
							log("Received refusal '"+refuse.getContent()+"' from '"+
									refuse.getSender().getName()+"'.");
						};
						/*
						 * An agent failed.
						 */
						@Override
						protected void handleFailure(ACLMessage failure) {
							if (failure.getSender().equals(myAgent.getAMS())) {
								/*
								 * The receiver of my CFP does not exists anymore.
								 */
								log("Received failure '"+failure.getContent()+"' from the AMS.");
							} else {
								log("Received failure '"+failure.getContent()+"' from '"+
										failure.getSender().getName()+"'.");
							}
						};
						/*
						 * NB: NOW we have either
						 * 		(i) collected all the responses (positive or negative doesn't matter)
						 * 		(ii) reached timeout (someone failed)
						 * In both cases we can now discover the best seller and
						 * 		(1) accept its proposal
						 * 		(2) reject other sellers proposals
						 */
						@Override
						protected void handleAllResponses(java.util.Vector responses, java.util.Vector acceptances) {
							log("Proposals collection phase ends (all responses or timeout).");
							ACLMessage accept = null;
							Enumeration e = responses.elements();
							while (e.hasMoreElements()) {
								ACLMessage msg = (ACLMessage) e.nextElement();
								if (msg.getPerformative() == ACLMessage.PROPOSE) {
									ACLMessage reply = msg.createReply();
									reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
									reply.setContent(targetBookTitle);
									acceptances.addElement(reply);
									float price = Float.parseFloat(msg.getContent());
									if (bestSeller == null || price < bestPrice) {
										bestPrice = price;
										bestSeller = msg.getSender();
										accept = reply;
									}
								}
							}
							if (accept != null) {
								log("Sending purchase order for book '"+targetBookTitle+"' to agent '"+
										bestSeller.getName()+"'...");
								accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
							}
						};
						/*
						 * Purchase confirmation received.
						 */
						@Override
						protected void handleInform(ACLMessage inform) {
							log("Received confirmation '"+inform.getContent()+"' from '"+
									inform.getSender().getName()+"'.");
							log("Book '"+targetBookTitle+"' has been successfully purchased" +
									" from agent '"+inform.getSender().getName()+"'.");
						};
					};
					myAgent.addBehaviour(cni);
				}else
					log("No suitable services found, retrying in 10 seconds...");
				
			}
			
			@Override
			public int onEnd() {
				log("Terminating...");
				myAgent.doDelete();
				return super.onEnd();
			};
			
		} );

	}

	@Override
	protected void takeDown() {
		log("I'm done.");
	}
	
	/*
	 * Just draw a random book title from an input file.
	 */
	private String bootBookTitle() {
		try {
			BufferedReader br = new BufferedReader(new FileReader("src/com/jadehomeautomation/demo/books.cat"));
			String line;
			StringTokenizer st;
			LinkedList<String> titles = new LinkedList<String>();
			line = br.readLine();
			while(line != null){
				st = new StringTokenizer(line, ";");
				titles.add(st.nextToken());
				line = br.readLine();
			}
			br.close();
			return titles.get((int) Math.round(Math.random()*(titles.size()-1)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			doDelete();
		} catch (IOException e) {
			e.printStackTrace();
			doDelete();
		}
		return "";
	}
	
	private void log(String msg) {
		System.out.println("["+getName()+"]: "+msg);
	}
	
}
