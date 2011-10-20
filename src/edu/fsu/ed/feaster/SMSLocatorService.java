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
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

public class SMSLocatorService extends Service {
	private NotificationManager mNM;
    private int NOTIFICATION = R.string.local_service_started;
    protected LocationManager mLocationManager;
    private MyLocationListener mMyListener = new MyLocationListener();

	private int mCommand;
	private String mMessage;
	private MediaPlayer mMediaPlayer;
	
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
	        
	        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	        showNotification();
	        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0,mMyListener );
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
		    		playRinger();
		    		break;
		    	default:
		    		;
		    	
		    	}
	    	}
	        // We want this service to continue running until it is explicitly
	        // stopped, so return sticky.
	        return START_STICKY;
	    }

	    public void turnOnRinger() {
	    	AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	    	int maxStreamVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
	    	mAudioManager.setRingerMode(AudioManager.VIBRATE_SETTING_OFF);
	    	mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
	    	mAudioManager.setStreamVolume(AudioManager.STREAM_RING, 
	    				maxStreamVolume,AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
	    }
	    
	    public void playRinger() {
		    	
	    	mMediaPlayer = MediaPlayer.create(this, R.raw.alarm);
	    	mMediaPlayer.setLooping(true);
	    	mMediaPlayer.start();
    		
	    	// TODO bring up activity that allows user to stop alarm
    		
    		
	    }
	    
	    public void startStopRinger() {
	    	/* TODO need to send intent to broadreciever who will launch activity
	    	 *  then activity will bind to this service so it can cut alarm sound off
	    	*/
	    	
	    	Intent broadcast = new Intent();
	    	broadcast.setAction("StopRinger");
	    	sendBroadcast(broadcast);
	    }
	    
	    @Override
	    public void onDestroy() {
	        // Cancel the persistent notification.
	        mNM.cancel(NOTIFICATION);
	        mLocationManager.removeUpdates(mMyListener);
	        //mMediaPlayer.stop();
	        //mMediaPlayer.release();
	        // Tell the user we stopped.
	        //Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
	    	;
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
	   		
	   		Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	   	 
	   		HttpGet httpGet = new HttpGet(
	   				
	   				"http://maps.googleapis.com/maps/api/geocode/json?latlng="
	   				 +location.getLatitude()+","+location.getLongitude()+"&sensor=true");
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
	   			}
	   		} catch (ClientProtocolException e) {
	   			e.printStackTrace();
	   		} catch (IOException e) {
	   			e.printStackTrace();
	   		}
		return builder.toString();
    }

	    private class MyLocationListener implements LocationListener {
	    	        public void onLocationChanged(Location location) {
	    	        	
	    	        	Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

	    	        	v.vibrate(300);
	        	
	    	            String message = String.format(
	    	            			"New Location \n Longitude: %1$s \n Latitude: %2$s",
    	            				location.getLongitude(), location.getLatitude()
	    	            			);
	    	      
	    	            String response = GetJson();
	    	            try {
								JSONObject jsonObject = new JSONObject(response);
								JSONArray resultObj = jsonObject.getJSONArray("results");
								message = "You're In:\n"
										+ resultObj.getJSONObject(1).getString("formatted_address");
							} catch (JSONException e) {
								e.printStackTrace();
							}
								Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
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