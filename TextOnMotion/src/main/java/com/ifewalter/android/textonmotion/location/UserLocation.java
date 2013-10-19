package com.ifewalter.android.textonmotion.location;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.ifewalter.android.textonmotion.Compose;

public class UserLocation extends Service {

	private static final int TWO_MINUTES = 1000 * 60 * 5;
	private static String savedLocationUrl = null;

	public static final String SAVED_LOCATION_NAME = "MyLocation";
	private static final String LONGITUDE_KEY = "longitude";
	private static final String LATITUDE_KEY = "latitude";
	public static final String ACCURACY_KEY = "accuracy";
	private static String PROVIDER_KEY = "provider";
	private static String TIME_STAMP_KEY = "time_stamp";
	public static final String SAVED_LOCATION_URL = "savedurl";

	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;

	private void commitLocation(float newLongitude, float newLatitude,
			float newAccuracy, String newProvider, long newTimeStamp) {

		locationManager.removeUpdates(locationListener);

		savedLocationUrl = ShortenLocationUrl
				.shorten(newLatitude, newLongitude);

		SharedPreferences settings = getSharedPreferences(SAVED_LOCATION_NAME,
				0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putFloat(LONGITUDE_KEY, newLongitude);
		editor.putFloat(LATITUDE_KEY, newLatitude);
		editor.putFloat(ACCURACY_KEY, newAccuracy);
		editor.putString(PROVIDER_KEY, newProvider);
		editor.putLong(TIME_STAMP_KEY, newTimeStamp);
		editor.putString(SAVED_LOCATION_URL, savedLocationUrl);

		// Commit the edits!
		editor.commit();

		stopSelf();
	}

	// use function to compare stored location and last known location
	protected boolean isBetterLocation(Location oldLocation,
			Location currentBestLocation) {

		boolean returnValue = false;

		if (currentBestLocation == null) {
			// A new location is always better than no location
			returnValue = true;
		}
		// Check whether the new location fix is newer or older
		long timeDelta = oldLocation.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;

		// If it's been more than two minutes since the current location, use
		// the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			returnValue = true;
			// If the new location is more than two minutes older, it must be
			// worse
		} else if (isSignificantlyOlder) {
			returnValue = false;
		}
		return returnValue;
	}

	private static LocationListener locationListener;
	private static LocationManager locationManager;

	private void getCurrentLocation() {

		locationManager = (LocationManager) this
				.getSystemService(Compose.LOCATION_SERVICE);

		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				locationManager.removeUpdates(locationListener);
				commitLocation((float) location.getLongitude(),
						(float) location.getLatitude(),
						(float) location.getAccuracy(), location.getProvider(),
						location.getTime());

				UserLocation.locationManager.removeUpdates(this);

			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}
		};

		// request for gps
		System.gc();

		try {

			SharedPreferences locationSettings = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			String getLocationSettingsProvider = locationSettings.getString(
					"location_provider_preference", "Network");
			if (getLocationSettingsProvider.equals("GPS")) {
				boolean statusOfGPS = locationManager
						.isProviderEnabled(LocationManager.GPS_PROVIDER);
				if (statusOfGPS == true) {
					locationManager.requestLocationUpdates(
							LocationManager.GPS_PROVIDER, 0, 0,
							locationListener);
				} else {
					if (locationSettings.getBoolean(
							"gps_status_notification_preference", true) == true) {
						Toast.makeText(
								getApplicationContext(),
								"Please Enable GPS, to get Updates."
										+ " Temporarily Switching to Network Provider",
								Toast.LENGTH_LONG).show();
					}
					locationManager.requestLocationUpdates(
							LocationManager.NETWORK_PROVIDER, 0, 0,
							locationListener);
				}

			} else {
				// request for network
				locationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, 0, 0,
						locationListener);
			}
		} catch (Exception ex) {

		}

	}
	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}
		private long tempInt;
		@Override
		public void handleMessage(Message msg) {
			tempInt = TWO_MINUTES;
			try {
				SharedPreferences locationDelay = PreferenceManager
						.getDefaultSharedPreferences(getApplicationContext());
				String locationDelayValue = locationDelay.getString(
						"location_update_intereval_preference", "3");

				tempInt = Long.parseLong(locationDelayValue);
				tempInt = 1000 * 60 * tempInt;

				Runnable mUpdateTimerTask = new Runnable() {

					@Override
					public void run() {
						getCurrentLocation();

						mServiceHandler.postDelayed(this, tempInt);
					}
				};

				this.post(mUpdateTimerTask);

			} catch (Exception ex) {
			} finally {
				getCurrentLocation();
			}
			// Stop the service using the startId, so that we don't stop
			// the service in the middle of handling another job
			stopSelf(msg.arg1);
		}

	}
	@Override
	public void onCreate() {
		super.onCreate();

		HandlerThread thread = new HandlerThread("ServiceStartArguments",
				Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		// Get the HandlerThread's Looper and use it for our Handler
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);

	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		// For each start request, send a message to start a job and deliver the
		// start ID so we know which request we're stopping when we finish the
		// job
		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		mServiceHandler.sendMessage(msg);

		// If we get killed, after returning from here, restart
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}