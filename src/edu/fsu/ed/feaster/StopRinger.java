package edu.fsu.ed.feaster;

import edu.fsu.ed.feaster.SMSLocatorService.LocalBinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class StopRinger extends Activity {

	private String mMessage;
	private CountDownTimer mCountDown;
	private LocalBinder mBoundService;
	private Boolean mIsBound = false;

	TextView mNumberDisplayTextView;
	TextView mMessageTextView;
	Button mOffTimerButton;

	SharedPreferences preferences;

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
		bindService(new Intent(this, SMSLocatorService.class), mConnection,
				Context.BIND_AUTO_CREATE);
		mIsBound = true;
		Log.v("Bind", "Service Bound");
	}

	void doUnbindService() {
		if (mIsBound) {
			// Detach our existing connection.
			unbindService(mConnection);
			mIsBound = false;
			Log.v("Bind", "Service UnBound");
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v("StopRinger.java", "activity started");
		setContentView(R.layout.stop_timer_layout);

		preferences = getSharedPreferences("preferences", MODE_WORLD_READABLE);

		mMessageTextView = (TextView) findViewById(R.id.message_text);
		mNumberDisplayTextView = (TextView) findViewById(R.id.number_display);
		mOffTimerButton = (Button) findViewById(R.id.stop_timer_button);

		Intent intent = getIntent();
		// This
		if (intent.hasExtra("message")) {
			mMessage = intent.getStringExtra("message");
			mMessageTextView.setText(mMessage);
		} else {
			mMessageTextView.setText("This Phone Is Lost. Please Call One Of "
					+ "My Contact & Let Them Know. Thank You");
		}

		// countdown timer
		mCountDown = new CountDownTimer(30000, 1000) {
			public void onTick(long millisUntilFinished) {
				mNumberDisplayTextView.setText("seconds remaining: "
						+ millisUntilFinished / 1000);
			}

			public void onFinish() {
				// TODO: send sms back to user saying timer timed out
				if (mBoundService != null) {
					mBoundService.stopPlayer();
					mBoundService
							.sendResponse("Timer Expired: Phone Not Found");
				}
				stopService(new Intent(getApplicationContext(),
						SMSLocatorService.class));
				finish();
			}
		}.start();
		// END countdown timer

		mOffTimerButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// TODO: send sms back to user saying button was pushed
				if (mBoundService != null) {
					mBoundService.sendResponse("Timer Turned Off: Phone Found");
					mBoundService.stopPlayer();
				}
				mCountDown.cancel();
				finish();

			}
		});

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (!mIsBound) {
			doBindService();
		}
		Log.v("StopRinger", "OnResume");
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (mIsBound) {
			doUnbindService();
		}
		Log.v("StopRinger", "OnPause");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mIsBound) {
			doUnbindService();
		}
		if (stopService(new Intent(getApplicationContext(),
				SMSLocatorService.class))) {
			Log.v("StopRinger", "Service Stopped");
		}
		Log.v("StopRinger", "OnDestroy");
	}
}
