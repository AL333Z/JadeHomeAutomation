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

public class RoomsActivity extends ListActivity {
	
	private SampleController agent;
	private MyReceiver myReceiver;
	private ArrayAdapter<String> adapter;
	private String[] listValues = {"No rooms"};
	private AID[] agentAIDs;
	
	// Action names of Intents broadcasted to/from the Jade Agents
	public static final String ROOM_LIST = "com.jadehomeautomation.android.ROOM_LIST";
	public static final String ROOM_SELECTED = "com.jadehomeautomation.android.ROOM_SELECTED";
	
	// Extras names of Intents broadcasted to/from the Jade Agents
	public static final String ROOM_LIST_EXTRA = "roomList";
	public static final String ROOM_AID_EXTRA = "roomAid";
	
	
	/**
	 * The Agent must send an object of this class to display the room list
	 */
	@SuppressWarnings("serial")
	public static class RoomItems implements Serializable{
		public final String[] roomName;
		public final AID[] aid;
		
		public RoomItems(String[] roomName, AID[] aid){
			this.roomName = roomName;
			this.aid = aid;
		}
	}
	
	
	private Logger logger = Logger.getJADELogger(this.getClass().getName());

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listValues);
		setListAdapter(adapter);
		
		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, 
					int position, long id) {
				// Inform the Agent (or any other receiver) that a room has
				// been selected
				Intent broadcast = new Intent();
				broadcast.setAction(RoomsActivity.ROOM_SELECTED);
				broadcast.putExtra(ROOM_AID_EXTRA, agentAIDs[position]);
				RoomsActivity.this.sendBroadcast(broadcast);
				
				/////////////////
				// Only for testing!
				for (int i = 0; i < listValues.length; i++) {
					logger.log(Level.INFO, ""+listValues[i]);
//					adapter.add(listValues[i]);
				}
				adapter.addAll(listValues);
				//reload listview!
				adapter.notifyDataSetChanged();
				
				///////////
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

		IntentFilter roomListFilter = new IntentFilter();
		roomListFilter.addAction(ROOM_LIST);
		registerReceiver(myReceiver, roomListFilter);
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
			if (action.equalsIgnoreCase(ROOM_LIST)) {
				/*final TextView chatField = (TextView) findViewById(R.id.chatTextView);
				chatField.append(intent.getExtras().getString("sentence"));
				scrollDown();*/
				logger.log(Level.INFO, "received room list intent");
				Serializable obj = intent.getSerializableExtra(ROOM_LIST_EXTRA);
				if(obj instanceof RoomItems){
					// Put the new data on the list view
					RoomItems rooms = (RoomItems) obj;
					listValues = rooms.roomName;
					agentAIDs = rooms.aid;
					
					for (int i = 0; i < listValues.length; i++) {
						logger.log(Level.INFO, ""+listValues[i]);
					}
				}
			}
		}
	}
	
	
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.rooms, menu);
		return true;
	}

}
