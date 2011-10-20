package edu.fsu.ed.feaster;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ParseException;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class SMSLocatorService extends Service {
	private NotificationManager mNM;
    private int NOTIFICATION = R.string.local_service_started;
    protected LocationManager mLocationManager;
    private MyLocationListener mMyListener = new MyLocationListener();
    private Location mLocation; 

	private int mCommand;
	private String mMessage;
	private MediaPlayer mMediaPlayer;
	final int GPS_UPDATE_TIME_INTERVAL = 1800000;
	final int GPS_UPDATE_DISTANCE_INTERVAL = 0;
	
	    // Binding Section
	    public class LocalBinder extends Binder {

	    	SMSLocatorService getService() {
	    		return SMSLocatorService.this;
	    	}
	    	
	    	void stopPlayer() {
	    		SMSLocatorService.this.mMediaPlayer.stop();
	    		SMSLocatorService.this.mMediaPlayer.release();
	    	}
	    }
	    
	    private final IBinder mBinder = new LocalBinder();
	    
	    @Override
	    public IBinder onBind(Intent intent) {
	        return mBinder;
	    }
	    // END Binding Section
	    
	    
	    @Override
	    public void onCreate() {
	        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
	        //Toast.makeText(this,R.string.local_service_started, Toast.LENGTH_SHORT).show();
	     
	    }
	    

	    @Override
	    public int onStartCommand(Intent intent, int flags, int startId) {
	    	Log.v("myService", "has started");
	    	if(intent != null) {
		    	if(intent.hasExtra("command")) {
			    	mCommand = intent.getIntExtra("command", 5);
		    	if( intent.hasExtra("message")) {
		    		mMessage = intent.getStringExtra("message");
		    		}
		    	}
	    	switch(mCommand) {
		    	case SmsReceiver.TURN_ON_RINGER:
		    		//turn on ringer
		    		turnOnRinger();
		    		playRinger();
		    		startStopRinger();
		    		break;
		    	case SmsReceiver.TURN_ON_LOCATION:
		    		//make phone ring
		    		enableGPS(true);
		    		turnonLocationService();
		    		break;
		    	default:
		    		;
		    	
		    	}
	    	}
	        return START_STICKY;
	    }

	    private void turnonLocationService() {
	    	   
	        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	        showNotification();
	        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
	        		GPS_UPDATE_TIME_INTERVAL, GPS_UPDATE_DISTANCE_INTERVAL, mMyListener );
	        //getJson is called in listener it requires an inital location, provided here
	         mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	    }
	    
	    private void turnOnRinger() {
	    	AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	    	int maxStreamVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
	    	mAudioManager.setRingerMode(AudioManager.VIBRATE_SETTING_OFF);
	    	mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
	    	mAudioManager.setStreamVolume(AudioManager.STREAM_RING, 
	    				maxStreamVolume,AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
	    }
	    
	    private void playRinger() {
		    	
	    	mMediaPlayer = MediaPlayer.create(this, R.raw.alarm);
	    	mMediaPlayer.setLooping(true);
	    	mMediaPlayer.start();
    		
	    	// TODO bring up activity that allows user to stop alarm
    		
    		
	    }
	    
	    private void startStopRinger() {

	    	Intent broadcast = new Intent();
	    	broadcast.setAction("StopRinger");
	    	sendBroadcast(broadcast);
	    }
	    
	    private void enableGPS(boolean enable) {
	        String provider = Settings.Secure.getString(getContentResolver(), 
	            Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

	        if(provider.contains("gps") == enable) {
	            return; // the GPS is already in the requested state
	        }

	        final Intent poke = new Intent();
	        poke.setClassName("com.android.settings", 
	            "com.android.settings.widget.SettingsAppWidgetProvider");
	        poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
	        poke.setData(Uri.parse("3"));
	        sendBroadcast(poke);
	    }
	    
	    @Override
	    public void onDestroy() {
	        mNM.cancel(NOTIFICATION);
	        mLocationManager.removeUpdates(mMyListener);
	    }

	    private void showNotification() {
	        // In this sample, we'll use the same text for the ticker and the expanded notification
	        CharSequence text = getText(R.string.local_service_started);

	        // Set the icon, scrolling text and timestamp
	        Notification notification = new Notification(android.R.drawable.ic_dialog_alert, text,
	                System.currentTimeMillis());

	        // The PendingIntent to launch our activity if the user selects this notification
	        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
	                new Intent(this, LauncherActivity.class), 0);

	        // Set the info for the views that show in the notification panel.
	        notification.setLatestEventInfo(this, getText(R.string.local_service_label),
	                       text, contentIntent);

	        // Send the notification.
	        mNM.notify(NOTIFICATION, notification);
	    }
	    
	    private String GetJson() {
	        StringBuilder builder = new StringBuilder();
	   		HttpClient client = new DefaultHttpClient();
	   		
	   		
	   	 
	   		HttpGet httpGet = new HttpGet(
	   				
	   				"http://maps.googleapis.com/maps/api/geocode/json?latlng="
	   				 +mLocation.getLatitude()+","+mLocation.getLongitude()+"&sensor=true");
	   		try {
	   			HttpResponse response = client.execute(httpGet);
	   			StatusLine statusLine = response.getStatusLine();
	   			int statusCode = statusLine.getStatusCode();
	   			if (statusCode == 200) {
	   				HttpEntity entity = response.getEntity();
	   				InputStream content = entity.getContent();
	   				BufferedReader reader = new BufferedReader(
	   						new InputStreamReader(content));
	   				String line;
	   				while ((line = reader.readLine()) != null) {
	   					builder.append(line);
	   				}
	   			} else {
	   				Log.e(ParseException.class.toString(), "Failed to download file");
	   				builder.append("");
	   				}
		   		} catch (ClientProtocolException e) {
		   			Log.e("ClientProtocolException", "wrong protocol");
		   			e.printStackTrace();
		   			builder.append("");
		   		} catch (IOException e) {
		   			Log.e("IOException", "something went wrong try block: line 180");
		   			e.printStackTrace();
		   			builder.append(""); //incase nothing comes back the return statement wont fail
		   		}
		return builder.toString();
    }

	    private class MyLocationListener implements LocationListener {
	    	        public void onLocationChanged(Location location) {
	        	
	    	            String message = String.format(
	    	            			"New Location \n Longitude: %1$s \n Latitude: %2$s",
    	            				location.getLongitude(), location.getLatitude()
	    	            			);
	    	      
	    	            String response = GetJson();
	    	            if(!response.equals("")) {
		    	            try {
									JSONObject jsonObject = new JSONObject(response);
									JSONArray resultObj = jsonObject.getJSONArray("results");
									message = "You're In:\n"
											+ resultObj.getJSONObject(0).getString("formatted_address");
								} catch (JSONException e) {
									e.printStackTrace();
									Log.v("JSON","JSON not well formatted");
									}
								Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    	        			} else {
    	        				Log.e("JSON Error","Failed to get JSON file");
    	        			}
	    	        	}

	    	        public void onStatusChanged(String s, int i, Bundle b) {
	    	        	Toast.makeText(SMSLocatorService.this, "Provider status changed",
	    	            Toast.LENGTH_LONG).show();
	    	        	}
	    	
	    	        public void onProviderDisabled(String s) {
	    	            Toast.makeText(SMSLocatorService.this,
	    	            "Provider disabled by the user. GPS turned off",
	    	            Toast.LENGTH_LONG).show();
	    	        	}

	    	        public void onProviderEnabled(String s) {
    	        		Toast.makeText(SMSLocatorService.this,
        				"Provider enabled by the user. GPS turned on",
        				Toast.LENGTH_LONG).show();
	    	        	}
	  
	    			}

}