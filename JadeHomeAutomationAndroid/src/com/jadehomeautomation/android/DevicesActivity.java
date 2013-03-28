package com.jadehomeautomation.android;

import jade.core.AID;
import jade.core.MicroRuntime;
import jade.util.Logger;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

import java.io.Serializable;
import java.util.logging.Level;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

public class DevicesActivity extends ListActivity {
	
	private SampleController agent;
	private MyReceiver myReceiver;
	private ArrayAdapter<String> adapter;
	private String[] listValues = {"No devices"};
	private AID[] agentAIDs;
	
	// Action names of Intents broadcasted to/from the Jade Agents
	public static final String DEVICE_LIST = "com.jadehomeautomation.android.DEVICE_LIST";
	public static final String DEVICE_SELECTED = "com.jadehomeautomation.android.DEVICE_SELECTED";
	
	// Extras names of Intents broadcasted to/from the Jade Agents
	public static final String DEVICE_LIST_EXTRA = "deviceList";
	public static final String DEVICE_AID_EXTRA = "deviceAid";
	
	/**
	 * The Agent must send an object of this class to display the device list
	 */
	@SuppressWarnings("serial")
	public static class DeviceItems implements Serializable{
		public final String[] deviceName;
		public final AID[] aid;
		
		public DeviceItems(String[] deviceName, AID[] aid){
			this.deviceName = deviceName;
			this.aid = aid;
		}
	}
	
	private Logger logger = Logger.getJADELogger(this.getClass().getName());

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();		
		Serializable obj = intent.getSerializableExtra(DevicesActivity.DEVICE_LIST_EXTRA);
		DeviceItems devices = null;
		if(obj instanceof DeviceItems){
			// Put the new data on the list view
			devices = (DeviceItems) obj;
		}
		
		listValues = devices.deviceName;
		agentAIDs = devices.aid;
		
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listValues);
		setListAdapter(adapter);
		
		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, 
					int position, long id) {
				// Inform the Agent (or any other receiver) that a device has been selected
				logger.log(Level.INFO, "clicked on index: "+position+" agent selected: "+listValues[position]);
				Intent broadcast = new Intent();
				broadcast.setAction(DevicesActivity.DEVICE_SELECTED);
				broadcast.putExtra(DEVICE_AID_EXTRA, agentAIDs[position]);
				DevicesActivity.this.sendBroadcast(broadcast);
				
			}
		});
		
		try {
			agent =  MicroRuntime.getAgent(ConnectActivity.agentName).getO2AInterface(SampleController.class);
		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ControllerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		myReceiver = new MyReceiver();

		IntentFilter deviceListFilter = new IntentFilter();
		deviceListFilter.addAction(DEVICE_LIST);
		registerReceiver(myReceiver, deviceListFilter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(myReceiver);
	}

	private class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			logger.log(Level.INFO, "Received intent " + action);
			if (action.equalsIgnoreCase(DEVICE_LIST)) {
				logger.log(Level.INFO, "received device list intent");
				Serializable obj = intent.getSerializableExtra(DEVICE_LIST_EXTRA);
				if(obj instanceof DeviceItems){
					// Put the new data on the list view
					DeviceItems device = (DeviceItems) obj;
					listValues = device.deviceName;
					agentAIDs = device.aid;
					adapter = new ArrayAdapter<String>(DevicesActivity.this, android.R.layout.simple_list_item_1, listValues);
					setListAdapter(adapter);
				}
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.devices, menu);
		return true;
	}
}
