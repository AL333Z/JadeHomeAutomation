package com.jadehomeautomation.hardware;

import com.alvie.arduino.serpro.SerPro;
import com.alvie.arduino.serpro.SerPro.SerProBuffer;
import com.alvie.arduino.serpro.SerProPacket;


public class ArduinoSerProCommands {
	
	/** Numero totale di comandi, tenerlo aggiornato! */
	public static final int numCommands = 2;
	
	private SerPro serPro;
	
	public ArduinoSerProCommands(SerPro serPro){
		this.serPro=serPro;
		
		// Aggiungere quì il listener quando si crea un nuovo comando!
		serPro.addCommandListener((byte) 0, new Command0SumThreeNumbers());
		serPro.addCommandListener((byte) 1, new Command1SetLed13State());
	}
	
	
	// LISTA DEI COMANDI
	
	// Comando 0
	
	public static final byte commNum0 = 0; 

	public void sendCommand0SumThreeNumbers(int a, int b, int c){
		SerProPacket packet = serPro.createPacket(commNum0);
		// TODO controlla che l'int sia signed a 16 bit, cioè tra -32,767 e 32,767
		packet.addS16(a);
		packet.addS16(b);
		packet.addS16(c);
		serPro.sendPacket(packet);
	}
	
	private class Command0SumThreeNumbers implements SerPro.CommandListener {
		@Override
		public void handleCommand(SerPro serpro, SerProBuffer buffer) {
			int ret = buffer.getU16(); // TODO ma è signed o non signed??
			System.out.println("Ricevuto comando 0: "+ ret);
		}
	}
	
	// Comando 1
	
	public static final byte commNum1 = 1; 

	public void sendCommand1SetLed13State(int ledState){
		SerProPacket packet = serPro.createPacket(commNum1);
		// TODO controlla che l'int sia signed a 16 bit, cioè tra -32,767 e 32,767
		packet.addS16(ledState);
		serPro.sendPacket(packet);
	}
	
	private class Command1SetLed13State implements SerPro.CommandListener {
		@Override
		public void handleCommand(SerPro serpro, SerProBuffer buffer) {
			int ret = buffer.getU16(); // TODO ma è signed o non signed??
			if(ret==1){
				System.out.println("set dello stato del led 13 effettuato");
			} else {
				System.err.println("Errore nel settare lo stato del led 13: ritorno: "+ret);
			}
		}
	}

}