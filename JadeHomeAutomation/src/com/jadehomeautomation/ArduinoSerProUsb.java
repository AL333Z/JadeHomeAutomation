package com.jadehomeautomation;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import com.alvie.arduino.serpro.ProtocolHDLC;
import com.alvie.arduino.serpro.SerPro;
import com.alvie.arduino.serpro.SerProProtocol;


/**
 * Classe per comunicare con l'arduino che usa la libreria SerPro, che praticamente
 * implementa uno strato Link Layer con il protocollo standard HDLC sopra una porta seriale,
 * in questo caso quella emulata sopra l'USB 
 *
 */
public class ArduinoSerProUsb implements SerialPortEventListener{
	
	private SerPro serPro;
	private SerialPort serialPort;
	private ArduinoSerProCommands commands;
	
	//debug
	private InputStream input;

	private static final String PORT_NAMES[] = { 
		"/dev/tty.usbserial-A9007UX1", // Mac OS X
		"/dev/ttyACM0",
		"/dev/ttyUSB0"// Linux
	};
	
	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 2000;
	/** Default bits per second for COM port. */
	private static final int DATA_RATE = 9600;
	
	
	
	public ArduinoSerProUsb() throws IOException{
		try{
			initializeSerial();
			SerProProtocol protocol = new ProtocolHDLC();
			protocol.setPort(serialPort, DATA_RATE);
			serPro = new SerPro(protocol, ArduinoSerProCommands.numCommands);
			commands = new ArduinoSerProCommands(serPro);
			
			//debug
			//input = serialPort.getInputStream();
			//serialPort.addEventListener(this);
			//serialPort.notifyOnDataAvailable(true);
		} catch(Exception e){
			throw new IOException(e);
		}
	}
	
	public ArduinoSerProCommands getSerProCommands(){
		return commands;
	}
	
	
	private void initializeSerial() throws IOException, UnsupportedCommOperationException, PortInUseException, TooManyListenersException, InterruptedException {
		CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

		// iterate through, looking for the port
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			for (String portName : PORT_NAMES) {
				if (currPortId.getName().equals(portName)) {
					portId = currPortId;
					break;
				}
			}
		}

		if (portId == null) {
			System.out.println("Could not find COM port.");
			return;
		}

		// open serial port, and use class name for the appName.
		serialPort = (SerialPort) portId.open(this.getClass().getName(),
				TIME_OUT);

		// TODO da eliminare?
		// set port parameters
		serialPort.setSerialPortParams(DATA_RATE,
				SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);


		Thread.sleep(2000);
	}
	
	
	// Serve per il debugging
	/**
	 * Handle an event on the serial port. Read the data and print it.
	 */
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				int available = input.available();
				System.out.println("Ricevuti "+available+" bytes");
				byte chunk[] = new byte[available];
				input.read(chunk, 0, available); // TODO c'è un bug!! non è detto che legge il numero "available" di byte!!

				// Displayed results are codepage dependent
				System.out.print(new String(chunk));
			} catch (Exception e) {
				System.err.println(e.toString());
			}
		}
		// Ignore all the other eventTypes, but you should consider the other ones.
	}
	
	
	/** Metodo di test */
	public static void main(String[] args) {
		try {
			ArduinoSerProUsb comm = new ArduinoSerProUsb();
			Thread.sleep(1000);
			comm.commands.sendCommand0SumThreeNumbers(1, 2, 4);
			Thread.sleep(1000);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
