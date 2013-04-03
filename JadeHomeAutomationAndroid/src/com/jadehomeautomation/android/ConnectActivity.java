package com.jadehomeautomation.android;

import java.util.Random;
import java.util.logging.Level;


import jade.android.AndroidHelper;
import jade.android.MicroRuntimeService;
import jade.android.MicroRuntimeServiceBinder;
import jade.android.RuntimeCallback;
import jade.core.Agent;
import jade.core.MicroRuntime;
import jade.core.Profile;
import jade.util.Logger;
import jade.util.leap.Properties;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class ConnectActivity extends Activity {
	
	private SharedPreferences prefs;
	private EditText ipAddrEdit;
	private EditText portEdit;

	private MicroRuntimeServiceBinder microRuntimeServiceBinder;
	private ServiceConnection serviceConnection;
	private Logger logger = Logger.getJADELogger(this.getClass().getName());
	private MyHandler myHandler;
	private MyReceiver myReceiver;
	
	private static final int ROOMS_REQUEST = 0;
	
	private static final String CONNECTED_SIGNAL = "com.jadehomeautomation.android.CONNECTED_SIGNAL";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connect);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		ipAddrEdit = (EditText) findViewById(R.id.ipAddrEditText);
		portEdit = (EditText) findViewById(R.id.portEditText);
		
		ipAddrEdit.setText(prefs.getString("ipaddr", "0.0.0.0"));
		portEdit.setText(prefs.getString("port", "1099"));
		
		myHandler = new MyHandler();
		myReceiver = new MyReceiver();

		IntentFilter showChatFilter = new IntentFilter();
		showChatFilter.addAction(CONNECTED_SIGNAL);
		registerReceiver(myReceiver, showChatFilter);
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(myReceiver);
	}
	
	
	private RuntimeCallback<AgentController> agentStartupCallback = new RuntimeCallback<AgentController>() {
		@Override
		public void onSuccess(AgentController agent) {
		}

		@Override
		public void onFailure(Throwable throwable) {
			logger.log(Level.INFO, "Nickname already in use!");
			myHandler.postError("Nickname already in use!");
		}
	};
	
	private class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			if (bundle.containsKey("error")) {
				//infoTextView.setText("");
				String message = bundle.getString("error");
				showErrorDialog(message);
			}
		}

		public void postError(String error) {
			Message msg = obtainMessage();
			Bundle b = new Bundle();
			b.putString("error", error);
			msg.setData(b);
			sendMessage(msg);
		}
	}
	
	private class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			logger.log(Level.INFO, "Received intent " + action);
			if (action.equalsIgnoreCase(CONNECTED_SIGNAL)) {
				Intent launchRoomsActivity = new Intent(ConnectActivity.this,
						RoomsActivity.class);
				launchRoomsActivity.putExtra("nickname", agentName);
				ConnectActivity.this.startActivityForResult(launchRoomsActivity, ROOMS_REQUEST);
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ROOMS_REQUEST) {
			if (resultCode == RESULT_CANCELED) {
				// The chat activity was closed.
				//infoTextView.setText("");
				logger.log(Level.INFO, "Stopping Jade...");
				microRuntimeServiceBinder
						.stopAgentContainer(new RuntimeCallback<Void>() {
							@Override
							public void onSuccess(Void thisIsNull) {
							}

							@Override
							public void onFailure(Throwable throwable) {
								logger.log(Level.SEVERE, "Failed to stop the "
										+ SampleController.class.getName()
										+ "...");
								agentStartupCallback.onFailure(throwable);
							}
						});
			}
		}
	}
	
	/** Launched when the "Connect" button is clicked */
	public void connect(View v){
		
		// Save ipaddr and port
		
		EditText ipAddrEdit = (EditText) findViewById(R.id.ipAddrEditText);
		EditText portEdit = (EditText) findViewById(R.id.portEditText);
		
		String ipAddr = ipAddrEdit.getText().toString();
		String port = portEdit.getText().toString();
		
		SharedPreferences.Editor edit = prefs.edit();
		edit.putString("ipaddr", ipAddr);
		edit.putString("port", port);
		edit.commit();
		
		// Start Jade agents
		
		final Properties profile = new Properties();
		profile.setProperty(Profile.MAIN_HOST, ipAddr);
		profile.setProperty(Profile.MAIN_PORT, port);
		profile.setProperty(Profile.MAIN, Boolean.FALSE.toString());
		profile.setProperty(Profile.JVM, Profile.ANDROID);
		profile.setProperty(Profile.LOCAL_HOST,
				AndroidHelper.getLocalIPAddress());
		
		if (microRuntimeServiceBinder == null) {
			serviceConnection = new ServiceConnection() {
				public void onServiceConnected(ComponentName className,
						IBinder service) {
					microRuntimeServiceBinder = (MicroRuntimeServiceBinder) service;
					logger.log(Level.INFO, "Gateway successfully bound to MicroRuntimeService");
					startContainer(profile, agentStartupCallback);
				};

				public void onServiceDisconnected(ComponentName className) {
					microRuntimeServiceBinder = null;
					logger.log(Level.INFO, "Gateway unbound from MicroRuntimeService");
				}
			};
			logger.log(Level.INFO, "Binding Gateway to MicroRuntimeService...");
			bindService(new Intent(getApplicationContext(),
					MicroRuntimeService.class), serviceConnection,
					Context.BIND_AUTO_CREATE);
		} else {
			logger.log(Level.INFO, "MicroRumtimeGateway already binded to service");
			startContainer(profile, agentStartupCallback);
		}
		
	}
	
	private void startContainer(Properties profile,
			final RuntimeCallback<AgentController> agentStartupCallback) {
		if (!MicroRuntime.isRunning()) {
			microRuntimeServiceBinder.startAgentContainer(profile,
					new RuntimeCallback<Void>() {
						@Override
						public void onSuccess(Void thisIsNull) {
							logger.log(Level.INFO, "Successfully start of the container...");
							startAgent(agentStartupCallback);
						}

						@Override
						public void onFailure(Throwable throwable) {
							logger.log(Level.SEVERE, "Failed to start the container...");
						}
					});
		} else {
			startAgent(agentStartupCallback);
		}
	}
	
	// Data of the agent to start in this android app 
	private final Class<? extends Agent> agentToStart = SampleController.class; 
	public static final String agentName = "android-agent"+ new Random().nextInt(10000000);
	
	private void startAgent(final RuntimeCallback<AgentController> agentStartupCallback) {
		microRuntimeServiceBinder.startAgent(agentName, agentToStart.getName(),
				new Object[] { getApplicationContext() },
				new RuntimeCallback<Void>() {
			@Override
			public void onSuccess(Void thisIsNull) {
				logger.log(Level.INFO, "Successfully start of the "
						+ agentToStart.getName() + "...");
				try {
					agentStartupCallback.onSuccess(MicroRuntime
							.getAgent(agentName));
				} catch (ControllerException e) {
					// Should never happen
					agentStartupCallback.onFailure(e);
				}
				
				// also start the RoomsActivity (only for test??)
				Intent launchRoomsActivity = new Intent(ConnectActivity.this,
						RoomsActivity.class);
				launchRoomsActivity.putExtra("nickname", agentName);
				ConnectActivity.this.startActivityForResult(launchRoomsActivity, ROOMS_REQUEST);
				
			}

			@Override
			public void onFailure(Throwable throwable) {
				logger.log(Level.SEVERE, "Failed to start the "
						+ agentToStart.getName() + "...");
				agentStartupCallback.onFailure(throwable);
			}
		});
	}
	
	public void showErrorDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message).setCancelable(false)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_connect, menu);
		return true;
	}

}
