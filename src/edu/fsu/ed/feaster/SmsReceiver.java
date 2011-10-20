package edu.fsu.ed.feaster;

import java.util.HashMap;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
/*Currently the priority isn't set high enough in the manifest
 * eventually I want this to swallow up messages with the given keyword using
 * abortBroadcast()
 * 
 * there isn't a list of commands yet to trigger actions
 * 
 */
public class SmsReceiver extends BroadcastReceiver {

	protected static final int TURN_ON_RINGER = 0;
	protected static final int PLAY_RINGER = 1;
	
	
	@Override
	public void onReceive(Context context, Intent intent) {

		SharedPreferences mSettings;
		String[] commands_list = {"com1","com2","com3","com4"};//possible commands to perform remote operations
		Map<String, Integer> command_map = new HashMap<String, Integer>();
		for(int x = 0; x < commands_list.length; x++) {
			command_map.put(commands_list[x], x);
		}
		
		
		mSettings = context.getSharedPreferences(LauncherActivity.SHARED_PREF_NAME,1);
		String pw = mSettings.getString("key", "");//password user set in LauncherActivity
		String message = mSettings.getString("msg", "");//message user set in LauncherActivity
		Bundle bundle = intent.getExtras();
		
		//decode SMS message
		if (bundle != null) {
            Object[] pdus = (Object[])bundle.get("pdus");
            final SmsMessage[] messages = new SmsMessage[pdus.length];
            
            for (int i = 0; i < pdus.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
            }
            String msg = messages[0].getMessageBody();
        // END decode SMS message
        
            //search for command+password
            for(int x = 0; x < commands_list.length; x++) {
               	String key_pw = commands_list[x]+":"+pw;//format should be "command:password"
            	if(msg.contains(key_pw)) {
                	Intent i = new Intent(context, SMSLocatorService.class);
                    i.putExtra("command", x);//command owner wants performed
            		i.putExtra("message", message); //message from owner
            		context.startService(i);
            	}
            }
            //END search for command+password
            
        }//END if (line: 31)
	}//END onRecieve
}//END Class


