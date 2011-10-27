package edu.fsu.ed.feaster;

import edu.fsu.ed.feaster.SMSLocatorService.LocalBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class StopRinger extends Activity{
	
	String mMessage;
	CountDownTimer mCountDown;
	TextView numberDisplayTextView;
		
	private LocalBinder mBoundService; 
	private Boolean mIsBound;
	
	private class MyServiceConnection implements ServiceConnection {

		public void onServiceConnected(ComponentName name, IBinder service) {
			mBoundService = (LocalBinder) service;
		}

		public void onServiceDisconnected(ComponentName name) {
			mBoundService = null;
		}
	};

	private MyServiceConnection mConnection = new MyServiceConnection();
	
	void doBindService() {
	    bindService(new Intent(this, SMSLocatorService.class), mConnection, Context.BIND_AUTO_CREATE);
	    mIsBound = true;
	    Log.v("Bind", "Service Bound");
	}

	void doUnbindService() {
	    if (mIsBound) {
	        // Detach our existing connection.
	        unbindService(mConnection);
	        mIsBound = false;
	    }
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.v("StopRinger.java", "activity started");
		setContentView(R.layout.stop_timer_layout);
		doBindService();
		
		TextView messageTextView = (TextView) findViewById(R.id.message_text);
		numberDisplayTextView = (TextView) findViewById(R.id.number_display);
		Button offTimerButton = (Button) findViewById(R.id.stop_timer_button);
		
		
		Intent intent = getIntent();
		// This 
		if(intent.hasExtra("message")) {
			mMessage = intent.getStringExtra("message");
			messageTextView.setText(mMessage);
		} else {
			messageTextView.setText("This Phone Is Lost. Please Call One Of My Contact & Let Them Know. Thank You");
		}
		
		// countdown timer
		mCountDown = new CountDownTimer(30000, 1000) {
		     public void onTick(long millisUntilFinished) {
		    	 numberDisplayTextView.setText("seconds remaining: " + millisUntilFinished / 1000);
		     }
		     public void onFinish() {
		       	// TODO send sms back to user saying timer timed out
		    	 mBoundService.stopPlayer();
		    	 mBoundService.sendResponse("Timer Expired: Phone Not Found");
		    	 finish();
		     }
		  }.start();
		// END countdown timer
		  
		offTimerButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				mCountDown.cancel(); 
				StopRinger.this.mBoundService.stopPlayer();
				// TODO send sms back to user saying button was pushed
				mBoundService.sendResponse("Timer Turned Off: Phone Found");
				finish();
			}
		}); 
		 
		
	}
	
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    doUnbindService();
	}
	
	
}


