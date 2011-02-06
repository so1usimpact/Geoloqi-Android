package com.geoloqi.android1;

import java.util.Timer;
import java.util.TimerTask;

import com.geoloqi.android1.Geoloqi.LQUpdateUI;
import com.geoloqi.android1.Geoloqi.MyTimerTask;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class GeoloqiService extends Service implements LocationListener {
	private static final String TAG = "GeoloqiService";
	MediaPlayer player;
	LocationManager locationManager;
	LQLocationData db;
	int distanceFilter = 5;
	float trackingLimit = 10.0f;
	Timer sendingTimer;
	private Handler handler = new Handler();
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		Toast.makeText(this, "My Service Created", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onCreate");
		
		player = MediaPlayer.create(this, R.raw.braincandy);
		player.setLooping(false); // Set looping
		
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		db = new LQLocationData(this);
		
		sendingTimer = new Timer();
		sendingTimer.schedule(new LQSendingTimerTask(), 0, 10000);
	}

	@Override
	public void onDestroy() {
		Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onDestroy");
		player.stop();
		Log.d(TAG, "Points: " + db.numberOfUnsentPoints());
	}
	
	@Override
	public void onStart(Intent intent, int startid) {
		Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onStart");
		player.start();
		
		String bestProvider = locationManager.getBestProvider(new Criteria(), true);
		locationManager.requestLocationUpdates(bestProvider, distanceFilter, trackingLimit, this);
		
		Log.d(TAG, "Provider: " + bestProvider);
	}

	public void onLocationChanged(Location location) {
		Log.d(TAG, location.toString());
		db.addLocation(location, distanceFilter, (int)trackingLimit, 0);
	}

	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	

	class LQFlushQueue extends AsyncTask<Void, Void, Void> {

		// Doesn't have access to the UI thread
		protected Void doInBackground(Void... v) {
			Log.d(TAG, "Flushing queue...");
			
			return null;
		}

		protected void onProgressUpdate() {

		}

		// Runs with the return value of doInBackground
		protected void onPostExecute(Void v) {
			Log.d(TAG, "Flush queue completed");
			
		}		
	}
	
	public class LQSendingTimerTask extends TimerTask {
		private Runnable runnable = new Runnable() {
			public void run() {
				new LQFlushQueue().execute();
			}
		};

		public void run() {
			handler.post(runnable);
		}
	}
	
}
