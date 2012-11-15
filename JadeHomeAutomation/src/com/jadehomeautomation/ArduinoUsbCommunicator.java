package com.jadehomeautomation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

/**
 * Classe scritta in puro Java che serve per comunicare con l'Arduino via porta seriale USB. 
 */
public class ArduinoUsbCommunicator implements SerialPortEventListener, ILightBulb {

	private SerialPort serialPort;

	private static final String PORT_NAMES[] = { 
		"/dev/tty.usbserial-A9007UX1", // Mac OS X
		"/dev/ttyACM0" // Linux
	};

	/** Buffered input stream from the port */
	private InputStream input;
	/** The output stream to the port */
	private OutputStream output;
	private PrintWriter outWriter;
	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 2000;
	/** Default bits per second for COM port. */
	private static final int DATA_RATE = 9600;
	
	
	public ArduinoUsbCommunicator() throws IOException {
		try {
			initialize();
		} catch(Exception e){
			throw new IOException(e);
		}
	}

	
	public void initialize() throws IOException, UnsupportedCommOperationException, PortInUseException, TooManyListenersException, InterruptedException {
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

		// set port parameters
		serialPort.setSerialPortParams(DATA_RATE,
				SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);

		// open the streams
		input = serialPort.getInputStream();
		output = serialPort.getOutputStream();
		outWriter = new PrintWriter(output);

		// add event listeners
		serialPort.addEventListener(this);
		serialPort.notifyOnDataAvailable(true);

		Thread.sleep(2000);
	}

	
	/**
	 * This should be called when you stop using the port.
	 * This will prevent port locking on platforms like Linux.
	 */
	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	
	/**
	 * Handle an event on the serial port. Read the data and print it.
	 */
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				int available = input.available();
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
	
	
	public void sendString(String str) throws IOException, InterruptedException {
		outWriter.println(str);
		outWriter.flush();
		Thread.sleep(1000);
	}
	
	
	// METODI LAMPADINA
	@Override
	public void on() throws IOException {
		try {
			sendString("1,0,0");
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void off() throws IOException {
		try {
			sendString("0,0,0");
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}
	


	/**
	 * Main che fa partire dei test
	 */
	public static void main(String[] args) {
		
		ArduinoUsbCommunicator comm = null;
		try {
			
			comm = new ArduinoUsbCommunicator();
			
			while(true){
				comm.on();
				comm.off();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(comm!=null) comm.close();
		}
		
	}

}
