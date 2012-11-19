package com.jadehomeautomation;

import com.alvie.arduino.serpro.SerPro;
import com.alvie.arduino.serpro.SerPro.SerProBuffer;
import com.alvie.arduino.serpro.SerProPacket;


public class ArduinoSerProCommands {
	
	/** Numero totale di comandi, tenerlo aggiornato! */
	public static final int numCommands = 1;
	
	private SerPro serPro;
	
	public ArduinoSerProCommands(SerPro serPro){
		this.serPro=serPro;
		
		// Aggiungere quì il listener quando si crea un nuovo comando!
		serPro.addCommandListener((byte) 0, new Command0SumThreeNumbers());
		
	}
	
	
	// LISTA DEI COMANDI
	
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

}