package edu.fsu.ed.feaster;

import java.util.HashMap;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
/*Currently the priority isn't set high enough in the manifest
 * eventually I want this to swallow up messages with the given keyword using
 * abortBroadcast()
 * 
 * there isn't a list of commands yet to trigger actions
 * 
 */
public class SmsReceiver extends BroadcastReceiver {

	protected static final int TURN_ON_RINGER = 0;
	protected static final int TURN_ON_LOCATION = 1;
	private static final String TAG = "SMSReciever";
	SharedPreferences preferences;
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive");
		preferences = context.getSharedPreferences("preferences", 3);

		SharedPreferences.Editor editor = preferences.edit();

		//SharedPreferences mSettings;
		String[] commands_list = {"com1","com2","com3","com4"};//possible commands to perform remote operations
		Map<String, Integer> command_map = new HashMap<String, Integer>();
		for(int x = 0; x < commands_list.length; x++) {
			command_map.put(commands_list[x], x);
		}
		
		
		//mSettings = context.getSharedPreferences(LauncherActivity.SHARED_PREF_NAME,1);
		//String pw = mSettings.getString("key", "");//password user set in LauncherActivity
		//String messageFromLauncherActivity = mSettings.getString("msg", "");//message user set in LauncherActivity
		
		String pw = preferences.getString("password", "");
		String messsageFromLauncherActivity = preferences.getString("message", "");
		Log.d(TAG, "Password: " + pw);
		Log.d(TAG, "Message: " + messsageFromLauncherActivity);
		
		
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
            		
            		editor.putLong("command", x);
            		
                    i.putExtra("command", x);//command owner wants performed
 
                    //message already in preference with key "message"
                    
            		i.putExtra("message", messsageFromLauncherActivity); //message from owner
            		Log.v("SmsReciver Message", messsageFromLauncherActivity);
            		if(!messages[0].isEmail()) {
            			
            			editor.putString("sender_phone", messages[0].getOriginatingAddress());
            			editor.putBoolean("isEmail", false);
            			
            			i.putExtra("sender_phone", messages[0].getOriginatingAddress());
            			i.putExtra("isEmail", false);
            			} else {
            				
            				// This is untested. Need to test on real phone
            				editor.putString("sender_email",messages[0].getDisplayOriginatingAddress());
            				editor.putBoolean("isEmail", false);
            				
            				i.putExtra("sender_email", messages[0].getDisplayOriginatingAddress());
            				i.putExtra("isEmail", true);
            			}
            		
            		context.startService(i);
            	}
               	editor.commit();
            }
            //END search for command+password
            
        }//END if (line: 31)
	}//END onRecieve
}//END Class


