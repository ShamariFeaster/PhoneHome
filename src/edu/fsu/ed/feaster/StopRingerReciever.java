package edu.fsu.ed.feaster;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StopRingerReciever extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent launchStopRinger = new Intent(context, StopRinger.class);
		launchStopRinger.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(launchStopRinger);
		
	}

}
