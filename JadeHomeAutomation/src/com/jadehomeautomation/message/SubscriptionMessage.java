package com.jadehomeautomation.message;

import jade.core.AID;

public class SubscriptionMessage extends Message {
	protected String toggleSwitchId;
	
	public SubscriptionMessage(String service, AID aid, String toggleSwitchId) {
		super(service, aid);
		this.toggleSwitchId = toggleSwitchId;
	}
	
	public String getToggleSwitchId() {
		return this.toggleSwitchId;
	}
}
