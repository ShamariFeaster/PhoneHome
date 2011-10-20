package edu.fsu.ed.feaster;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StopRingerReciever extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		String messageFromLauncherActivity = intent.getStringExtra("message");
		Log.v("StopRingerReciever message", messageFromLauncherActivity);
		Intent launchStopRinger = new Intent(context, StopRinger.class);
		launchStopRinger.putExtra("message", messageFromLauncherActivity);
		launchStopRinger.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(launchStopRinger);
		
	}

}
