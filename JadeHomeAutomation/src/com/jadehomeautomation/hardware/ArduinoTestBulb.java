package com.jadehomeautomation.hardware;

import java.io.IOException;

/**
 * Implementazione dell'interfaccia ILightBulb (cioè di una lampadina)
 * che fa accendere un led sull'Arduino. Serve per fare testing
 */

public class ArduinoTestBulb implements ILightBulb{
	
	private ArduinoSerProCommands commands;
	
	public ArduinoTestBulb() throws IOException{
		commands = ArduinoSerProUsb.getInstance().getSerProCommands();
	}

	@Override
	public void on() throws IOException {
		commands.sendCommand1SetLed13State(1);
	}

	@Override
	public void off() throws IOException {
		commands.sendCommand1SetLed13State(0);
	}

}
