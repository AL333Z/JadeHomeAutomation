package com.jadehomeautomation.android;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class ConnectActivity extends Activity {
	
	private SharedPreferences prefs;
	private EditText ipAddrEdit;
	private EditText portEdit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connect);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		ipAddrEdit = (EditText) findViewById(R.id.ipAddrEditText);
		portEdit = (EditText) findViewById(R.id.portEditText);
		
		ipAddrEdit.setText(prefs.getString("ipaddr", "0.0.0.0"));
		portEdit.setText(prefs.getString("port", "1099"));
	}
	
	
	/** Launched when the "Connect" button is clicked */
	public void connect(View v){
		
		EditText ipAddrEdit = (EditText) findViewById(R.id.ipAddrEditText);
		EditText portEdit = (EditText) findViewById(R.id.portEditText);
		
		String ipAddr = ipAddrEdit.getText().toString();
		String port = portEdit.getText().toString();
		
		SharedPreferences.Editor edit = prefs.edit();
		edit.putString("ipaddr", ipAddr);
		edit.putString("port", port);
		edit.commit();
		
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_connect, menu);
		return true;
	}

}
