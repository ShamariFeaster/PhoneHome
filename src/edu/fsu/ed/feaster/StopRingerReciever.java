package edu.fsu.ed.feaster;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StopRingerReciever extends BroadcastReceiver{
	private static final String TAG = "StopRinger";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive");
		Intent launchStopRinger = new Intent(context, StopRinger.class);
		launchStopRinger.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(launchStopRinger);
		Log.d(TAG, "StopRinger Intent Sent");
		
	}

}
