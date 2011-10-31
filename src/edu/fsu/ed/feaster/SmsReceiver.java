
package edu.fsu.ed.feaster;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

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
    protected static final int TURN_ON_SYSTEMS_CHECK = 2;
    private static final String TAG = "SMSReciever";
    String mOriginatingAddress;
    SharedPreferences preferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        preferences = context.getSharedPreferences("preferences", 3);
        SharedPreferences.Editor editor = preferences.edit();

        String[] commands_list = {
                "com1", "com2", "com3", "com4"
        };// possible commands to perform remote operations
        Map<String, Integer> command_map = new HashMap<String, Integer>();
        for (int x = 0; x < commands_list.length; x++) {
            command_map.put(commands_list[x], x);
        }

        String pw = preferences.getString("password", null);
        String messsageFromLauncherActivity = preferences.getString("message", null);
        Log.d(TAG, "Password: " + pw);
        Log.d(TAG, "Message: " + messsageFromLauncherActivity);

        Bundle bundle = intent.getExtras();

        // decode SMS message
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            final SmsMessage[] messages = new SmsMessage[pdus.length];

            for (int i = 0; i < pdus.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
            }
            String msg = messages[0].getMessageBody();
            // END decode SMS message

            // search for command+password
            // format should be "command:password"
            for (int x = 0; x < commands_list.length; x++) {
                String key_pw = commands_list[x] + ":" + pw;

                if (msg.contains(key_pw)) {
                    Intent i = new Intent(context, SMSLocatorService.class);

                    editor.putInt("command", x);
                    
                    // Message from the owner
                    i.putExtra("message", messsageFromLauncherActivity);
                    Log.v("SmsReciver Message", messsageFromLauncherActivity);
                    if (!messages[0].isEmail()) {
                    	mOriginatingAddress = messages[0].getOriginatingAddress();
                    	Log.v("SmsReceiver", mOriginatingAddress);
                        editor.putString("sender_phone", mOriginatingAddress);
                        editor.putBoolean("isEmail", false);

                    } else {

                        // This is untested. Need to test on real phone
                        editor.putString("sender_email", messages[0]
                                .getDisplayOriginatingAddress());
                        editor.putBoolean("isEmail", true);

                    }

                    context.startService(i);
                }
                editor.commit();
            }
            // END search for command+password

        }// END if (line: 31)
    }// END onRecieve
}// END Class

