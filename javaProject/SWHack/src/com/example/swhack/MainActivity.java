package com.example.swhack;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

import com.neurosky.thinkgear.TGDevice;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	BluetoothAdapter 		bluetoothAdapter;
	TGDevice 				device;
	final boolean			rawEnabled 		= true;
	
	EditText				name;
	EditText				charc;
	Button					register;
	Button					classify;
	TextView				textView;
	Socket					socketObject;
	String 					Operation;
	String[] 				signal ;
	PrintStream 			out;
	String 					st, FinalSignal = "";
	int 					i				= 0;
	String 					serverResult;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		name = (EditText)findViewById(R.id.editText1);
		charc = (EditText)findViewById(R.id.editText2);
		textView = (TextView)findViewById(R.id.textView3);
		register = (Button)findViewById(R.id.button1);
		classify = (Button)findViewById(R.id.button2);
		textView.append( "Android version: " + Integer.valueOf(android.os.Build.VERSION.SDK) + "\n" );
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if( bluetoothAdapter == null ) {            
			// Alert user that Bluetooth is not available
			Toast.makeText( this, "Bluetooth not available", Toast.LENGTH_LONG ).show();
			//	finish();
			return;
		} else {
			// create the TGDevice 	
			device = new TGDevice(bluetoothAdapter, handler);

		} 
		textView.append("NeuroSky: " + device.getVersion() );
		textView.append("\n" );
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void registerMethod(View view){
		if(name.getText()==null && charc.getText()==null)
		{
			Toast.makeText( this, " Please enter subject ", Toast.LENGTH_LONG ).show();
		}
		else
		{	
			Operation = "register";
			if( device.getState() != TGDevice.STATE_CONNECTING && device.getState() != TGDevice.STATE_CONNECTED ) {
				device.connect( true );
				device.EKGstartDetection();
				signal 			= new String[2561];
				
			}
		}
	}
	public void classifyMethod(View view){
		if(name.getText()==null && charc.getText()==null)
		{
			Toast.makeText( this, " Please enter subject ", Toast.LENGTH_LONG ).show();
		}
		else
		{	
			Operation = "refresh";
			if( device.getState() != TGDevice.STATE_CONNECTING && device.getState() != TGDevice.STATE_CONNECTED ) {
				device.connect( true );
				device.EKGstartDetection();
				signal 			= new String[2561];
			 }
		}
	}
	
	/**
	 * Handles messages from TGDevice
	 */
	final Handler handler = new Handler() {  	  	
		@Override
		public void handleMessage( Message msg )  {
			
			switch( msg.what ) {
			case TGDevice.MSG_STATE_CHANGE:
				switch( msg.arg1 ) {
				case TGDevice.STATE_IDLE:
					break;
				case TGDevice.STATE_CONNECTING:       	
					textView.append( "Connecting...\n" );
					break;	
				case TGDevice.STATE_CONNECTED:
					textView.append( "Connected.\n" );
					device.start();
					break;
				case TGDevice.STATE_NOT_FOUND:
					textView.append( "Could not connect any of the paired BT devices.  Turn them on and try again.\n" );
					break;
				case TGDevice.STATE_DISCONNECTED:
					textView.append( "Disconnected.\n" );
				} /* end switch on msg.arg1 */
				break;
			case TGDevice.MSG_RAW_DATA:
				String st = Integer.toString(msg.arg1);
				signal[i] = st;
				i++;
				if(i==2561){
					i = 0;
					device.EKGstopDetection();
					device.close();
					FinalSignal = Arrays.toString(signal);
					textView.append( "Sending Data.\n" );
					new Thread(new Client()).start();
					while(true){
						try{
							Thread.sleep(100);
							
						}catch(Exception e){ }
						if(serverResult != null)
							break;
					}
					if(serverResult.equalsIgnoreCase("Done")){
						textView.append(serverResult);
					}
					if(serverResult.equalsIgnoreCase("c"))
					{
						 String number = "tel:4804342570";
					     Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(number)); 
					     startActivity(callIntent);
					}					
					if(serverResult.equalsIgnoreCase("i")){
						Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
						startActivity(intent);
						startActivity(intent);
					}
					if(serverResult.equalsIgnoreCase("m")){
						Intent sendIntent = new Intent(Intent.ACTION_VIEW);         
						sendIntent.setData(Uri.parse("sms:"));
						startActivity(sendIntent);
					}
					if(serverResult.equalsIgnoreCase("e"))
					{
						 String number = "tel:911";
					     Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(number)); 
					     startActivity(callIntent);
					}
					if(serverResult.equalsIgnoreCase("r"))
					{
						Toast.makeText( MainActivity.this, " Refesh Done!! ", Toast.LENGTH_LONG ).show();
					}
					cleanData();
				}
				break;
			default:
				break;
			} /* end switch on msg.what */
		} /* end handleMessage() */
	}; /* end Handler */
	
	
	class Client implements Runnable  {
		public void run(){
			try{
				String ip ="192.168.0.14";
						//"10.144.146.197";
				socketObject = new Socket();
				
				socketObject.connect(new InetSocketAddress(ip,10008),1000);
				if(!socketObject.isConnected())
					Toast.makeText( MainActivity.this, " Please enter subject ", Toast.LENGTH_LONG ).show();
				BufferedReader 	in = new BufferedReader(new InputStreamReader(socketObject.getInputStream()));
				try{
					out = new PrintStream (socketObject.getOutputStream());
					out.println(Operation);
					out.flush();
					out.println(name.getText());
					out.flush();
					out.println(charc.getText());
					out.flush();
					out.println(FinalSignal);
					out.flush();
					out.println(Integer.toString(FinalSignal.length()));
					out.flush();
					serverResult = in.readLine();
				}finally{
					//socketObject.close();
				}
			}catch(Exception E){
				E.printStackTrace();
			}
		}
	}
	void cleanData(){
		i=0;
		Operation = "";
		textView.setText("");
		textView.append("NeuroSky: " + device.getVersion() );
		textView.append("\n" );
		FinalSignal = "";
		serverResult = null;
	}
}
