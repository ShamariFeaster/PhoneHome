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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.provider.Settings;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class SMSLocatorService extends Service {
	private static final String TAG = "SMSlocatorService";
	final int GPS_UPDATE_TIME_INTERVAL = 60000; // final should be 1800000 =
												// every 30 minutes;
	final int GPS_UPDATE_DISTANCE_INTERVAL = 0;
	final int NOTIFICATION = R.string.local_service_started;

	protected LocationManager mLocationManager;
	private static MediaPlayer mMediaPlayer;// = new MediaPlayer();
	private NotificationManager mNM;
	private MyLocationListener mMyListener = new MyLocationListener();
	private SmsManager mSmsManager = SmsManager.getDefault();
	private SharedPreferences preferences;

	private Boolean mReceiverRegistered = false;
	private int mCommand;
	private String mMessage;
	private String mAddress;
	private Boolean mIsEmail; // did the command come from phone or email?
	private Boolean mAcPluggedIn = true; // start assuming plugged in
	private String mBatteryLevel;

	// Binding Section
	public class LocalBinder extends Binder {

		SMSLocatorService getService() {
			return SMSLocatorService.this;
		}

		void stopPlayer() {
			SMSLocatorService.this.stopMediaPlayer();
		}

		void sendResponse(String message) {
			Log.v("SMsLocatorService: sendResponse()", "mAddress: "
					+ SMSLocatorService.this.mAddress + " mIsEmail: "
					+ SMSLocatorService.this.mIsEmail);
			SMSLocatorService.this.mSmsManager.sendTextMessage(
					SMSLocatorService.this.mAddress, null, message, null, null);
		}
	}

	private final IBinder mBinder = new LocalBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	// END Binding Section
	private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 0 = battery, 1 = ac, 2 = usb
			int plugged = intent.getIntExtra("plugged", 0);
			int scale = intent.getIntExtra("scale", 0);
			int level = intent.getIntExtra("level", 0);
			String message = "";

			// battery percentage remaining
			int percent = scale / 100;
			level = level / percent;
			SMSLocatorService.this.mBatteryLevel = String.valueOf(level) + "%";
			message += "Your battery is at " + SMSLocatorService.this.mBatteryLevel;
			// if plugged is any other than 0, phone is plugged in
			if (plugged == 0) {
				SMSLocatorService.this.mAcPluggedIn = false;
				message += ". The phone is NOT plugged in.";
			} else {
				SMSLocatorService.this.mAcPluggedIn = true;
				message += ". The phone IS charging.";
			}

			if (mAcPluggedIn) {
				// send sms informing phone was just plugged in
				Log.v("mPluggedIn", "Phone is plugged in");
			}
			// send sms with battery level remaining
			Log.v("mBatteryLevel", mBatteryLevel);
			sendResponse(message);
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
		mMediaPlayer = MediaPlayer.create(this, R.raw.alarm);
		mMediaPlayer.setLooping(true);

		Log.d(TAG,
				"onStratCommand: " + Integer.toString(mMediaPlayer.hashCode()));

		Log.v("myService", "has started");

		switch (mCommand) {
		case SmsReceiver.TURN_ON_RINGER:
			// turn on ringer
			turnOnRinger();
			playRinger();
			startStopRinger();
			break;
		case SmsReceiver.TURN_ON_LOCATION:
			// make phone ring
			enableGPS(true);
			turnonLocationService();
			break;
		case SmsReceiver.TURN_ON_SYSTEMS_CHECK:
			registerBatteryReceiver();
			break;
		default:
			;

		}
		return START_STICKY;
	}

	@Override
	public void onCreate() {
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		preferences = getSharedPreferences("preferences",
				Context.MODE_WORLD_READABLE);
		mCommand = preferences.getInt("command", 5);
		mMessage = preferences.getString("message", null);
		mAddress = preferences.getString("sender_phone", null);
		mIsEmail = preferences.getBoolean("isEmail", false);
		if (mIsEmail) {
			Log.v("Orig Email", mAddress);
		} else {
			Log.v("SmsLocatorService", "Orig Phone Address" + mAddress);
		}
		Log.d(TAG, "onCreate");
	}

	@Override
	public void onDestroy() {
		mNM.cancel(NOTIFICATION);
		if (mLocationManager != null) {
			mLocationManager.removeUpdates(mMyListener);
		}
		if (mReceiverRegistered) {
			unRegisterBatteryReceiver();
		}
		Log.d(TAG, "LocationManager Unregistered");
		if (mMediaPlayer != null) {
			Log.d(TAG, "OnDestroy: Media Player Not Null");
		} else
			Log.d(TAG, "OnDestroy: Media Player Null");
	}

	private void turnonLocationService() {

		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		showNotification();
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				GPS_UPDATE_TIME_INTERVAL, GPS_UPDATE_DISTANCE_INTERVAL,
				mMyListener);
	}

	private void turnOnRinger() {
		Log.d(TAG, "turnOnRinger");
		AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int maxStreamVolume = mAudioManager
				.getStreamMaxVolume(AudioManager.STREAM_RING);
		mAudioManager.setRingerMode(AudioManager.VIBRATE_SETTING_OFF);
		mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		mAudioManager.setStreamVolume(AudioManager.STREAM_RING,
				maxStreamVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		showNotification();
	}

	private void stopMediaPlayer() {
		Log.d(TAG, "stopMediaPlayer()");
		Log.d(TAG,
				"stopMediaPlayer(): "
						+ Integer.toString(mMediaPlayer.hashCode()));
		if (mMediaPlayer != null) {
			Log.d(TAG, "stopMediaPlayer(): Media Player Not Null");
			try {
				mMediaPlayer.stop();
				mMediaPlayer.setLooping(false);
				mMediaPlayer.release();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}

		} else
			Log.d(TAG, "stopMediaPlayer(): Media Player Null");

	}

	private void playRinger() {
		Log.d(TAG, "playRinger()");

		mMediaPlayer.start();

		Log.d(TAG, "MediaPlayer Start");
	}

	private void startStopRinger() {
		Intent broadcast = new Intent();
		broadcast.putExtra("message", mMessage);
		Log.v("SMSLocatorService message", mMessage);
		broadcast.setAction("StopRinger");
		sendBroadcast(broadcast);
		Log.d(TAG, "StopRinger Broadcast Sent");
	}

	private void enableGPS(boolean enable) {
		// Uses exploit. CAUTION: make sure this holds up in future versions
		String provider = Settings.Secure.getString(getContentResolver(),
				Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

		if (provider.contains("gps") == enable) {
			return; // the GPS is already in the requested state
		}

		final Intent poke = new Intent();
		poke.setClassName("com.android.settings",
				"com.android.settings.widget.SettingsAppWidgetProvider");
		poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
		poke.setData(Uri.parse("3"));
		sendBroadcast(poke);
	}

	private void registerBatteryReceiver() {
		this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));
		mReceiverRegistered = true;

	}

	private void unRegisterBatteryReceiver() {
		this.unregisterReceiver(mBatInfoReceiver);
	}

	private void showNotification() {
		// In this sample, we'll use the same text for the ticker and the
		// expanded notification
		CharSequence text = getText(R.string.local_service_started);

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(
				android.R.drawable.ic_dialog_alert, text,
				System.currentTimeMillis());

		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, LauncherActivity.class), 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this,
				getText(R.string.local_service_label), text, contentIntent);

		// Send the notification.
		mNM.notify(NOTIFICATION, notification);
	}

	private String GetJson(Location location) {
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();

		HttpGet httpGet = new HttpGet(

		"http://maps.googleapis.com/maps/api/geocode/json?latlng="
				+ location.getLatitude() + "," + location.getLongitude()
				+ "&sensor=true");
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
				Log.e(ParseException.class.toString(),
						"Failed to download file");
				builder.append("");
			}
		} catch (ClientProtocolException e) {
			Log.e("ClientProtocolException", "wrong protocol");
			e.printStackTrace();
			builder.append("");
		} catch (IOException e) {
			Log.e("IOException", "something went wrong try block: line 180");
			e.printStackTrace();
			builder.append(""); // incase nothing comes back the return
								// statement wont fail
		}
		return builder.toString();
	}

	private class MyLocationListener implements LocationListener {

		public void onLocationChanged(Location location) {
			// RAW GPS Coordinates (default response)
			String message = String.format(
					"New Location \n Longitude: %1$s \n Latitude: %2$s",
					location.getLongitude(), location.getLatitude());
			// Attempting Readable Address Fetch From Google API
			String response = GetJson(location);
			Log.v("Json Response", response);
			if (!response.equals("")) {
				try {
					JSONObject jsonObject = new JSONObject(response);
					JSONArray resultObj = jsonObject.getJSONArray("results");
					message = "You're In:\n"
							+ resultObj.getJSONObject(0).getString(
									"formatted_address");
				} catch (JSONException e) {
					e.printStackTrace();
					Log.v("JSON ERROR",
							"GetJson() Returned Improper Formatted Response");
				}
			} else {
				Log.e("JSON Error", "Failed to get JSON file");
			}
			Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG)
					.show();
			sendResponse(message);
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
	}// END MyLocationListener

	void sendResponse(String message) {
		Log.v("sendResponse()", "mAddress: " + mAddress + " mIsEmail: "
				+ mIsEmail);
		SMSLocatorService.this.mSmsManager.sendTextMessage(mAddress, null,
				message, null, null);
	}

}