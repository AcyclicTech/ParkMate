package com.acyclictech.android.wheredipark;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends Activity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, LocationListener,
		android.location.LocationListener {

	private GoogleMap map;
	private LocationClient mLocationClient;
	private Location mCurrentLocation;
	private LocationRequest mLocationRequest;
	private Marker curMarker;
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	// Milliseconds per second
	private static final int MILLISECONDS_PER_SECOND = 1000;
	// Update frequency in seconds
	public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
	// Update frequency in milliseconds
	private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND
			* UPDATE_INTERVAL_IN_SECONDS;
	// The fastest update frequency, in seconds
	private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
	// A fast frequency ceiling in milliseconds
	private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND
			* FASTEST_INTERVAL_IN_SECONDS;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_map);
		MapsInitializer.initialize(this);

		mLocationClient = new LocationClient(this, this, this);
		mLocationRequest = LocationRequest.create();

		mLocationRequest
				.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
		// Set the update interval to 5 seconds
		mLocationRequest.setInterval(UPDATE_INTERVAL);
		// Set the fastest update interval to 1 second
		mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();

		int status = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getApplicationContext());
		if (status != ConnectionResult.SUCCESS) {
			Dialog d = GooglePlayServicesUtil.getErrorDialog(status, this,
					CONNECTION_FAILURE_RESOLUTION_REQUEST);
			d.show();
		}

		map.setMyLocationEnabled(true);

		map.setOnMapClickListener(new OnMapClickListener() {

			@Override
			public void onMapClick(LatLng latLng) {
				String address = getAddress(latLng);
				map.addMarker(new MarkerOptions().position(latLng).title(address));
				//save new point
				//TODO: add save dialog
				saveCurrent(latLng, address, "note");
			}
		});
		SharedPreferences sp = getPreferences(MODE_PRIVATE);
		if(sp.contains("Current Point")){
			String pointString = sp.getString("Current Point", null);
			try {
				JSONObject obj = new JSONObject(pointString);
				double latD = obj.getDouble("lat");
				double lonD = obj.getDouble("long");
				String note = obj.optString("note");
				String address = obj.getString("address");
				LatLng latLng = new LatLng(latD, lonD);
				curMarker = map.addMarker(new MarkerOptions().position(latLng).title(address));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private String getAddress(LatLng latLng){
		String address = "";
		Geocoder geocoder = new Geocoder(getApplicationContext(),
				Locale.getDefault());

		List<Address> addresses = null;
		try {
			/*
			 * Return 1 address.
			 */
			addresses = geocoder.getFromLocation(latLng.latitude,
					latLng.longitude, 1);
			if(addresses.size() > 0){
				StringBuffer addressName = new StringBuffer();
				Address add = addresses.get(0);
				for(int i = 0; i < add.getMaxAddressLineIndex(); i++){
					addressName.append(add.getAddressLine(i)).append('\n');
				}
				address = addressName.toString();
			}
		} catch (IOException e1) {
		}
		return address;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			//show settings/info
			return true;
		}
		if(id == R.id.save){
			//save current point as favorite
			//saveFavorite();
		}
		if(id == R.id.share){
			//share point with everyone
		}
		if(id == R.id.share_meter){
			//share parking meter point with everyone
		}
		if(id == R.id.clear){
			clearSpot();
		}
		if(id == R.id.search){
			//search for points
		}
		return super.onOptionsItemSelected(item);
	}

	private void saveCurrent(LatLng latLng, String address, String note) {
		SharedPreferences sp = getPreferences(MODE_PRIVATE);
		Editor e = sp.edit();
		JSONObject obj = new JSONObject();
		try {
			obj.put("lat", latLng.latitude);
			obj.put("long", latLng.longitude);
			obj.put("note", note);
			obj.put("address", address);

			e.putString("Current Point", obj.toString(4));
			e.apply();
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private void saveFavorite(LatLng latLng, String address, String note) {
		SharedPreferences sp = getPreferences(MODE_PRIVATE);
		Editor e = sp.edit();
		JSONObject obj = new JSONObject();
		try {
			String favorites = sp.getString("favorites", null);
			if(favorites != null){
				obj = new JSONObject(favorites);
			}
			JSONArray array = obj.getJSONArray("favorites");
			JSONObject newObj = new JSONObject();
			newObj.put("lat", latLng.latitude);
			newObj.put("long", latLng.longitude);
			newObj.put("note", note);
			newObj.put("address", address);

			array.put(newObj);
			obj.put("favorite", array);
			e.putString("Current Point", obj.toString(4));
			e.apply();
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private void shareSpot() {
		// TODO Auto-generated method stub
		
	}
	
	private void shareMeterSpot() {
		// TODO Auto-generated method stub
		
	}
	
	private void clearSpot() {
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setMessage(getString(R.string.confirm));
		b.setPositiveButton(R.string.ok, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				SharedPreferences sp = getPreferences(MODE_PRIVATE);
				Editor e = sp.edit();
				if(sp.contains("Current Point")){
					String pointString = sp.getString("Current Point", null);
					e.remove("Current Point");
					e.apply();
				}
				if(curMarker != null){
					curMarker.remove();
					curMarker = null;
				}
			}
		});
		b.setNegativeButton(R.string.cancel, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		AlertDialog d = b.create();
		d.show();
	}
	
	private void searchSpots() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		mLocationClient.requestLocationUpdates(mLocationRequest, this);
		mCurrentLocation = mLocationClient.getLastLocation();
		CameraUpdateFactory.zoomTo(9);
		if (mCurrentLocation != null) {
			map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(mCurrentLocation
					.getLatitude(), mCurrentLocation.getLongitude())));
		}
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		mLocationClient.removeLocationUpdates(this);

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		mLocationClient.connect();

	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		mLocationClient.disconnect();
		super.onStop();

	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	// public static class PlaceholderFragment extends Fragment {
	//
	// public PlaceholderFragment() {
	// }
	//
	// @Override
	// public View onCreateView(LayoutInflater inflater, ViewGroup container,
	// Bundle savedInstanceState) {
	// View rootView = inflater.inflate(R.layout.fragment_map, container,
	// false);
	// return rootView;
	// }
	// }

	@Override
	public void onLocationChanged(Location loc) {
		// TODO Auto-generated method stub
		// update map center location
		mCurrentLocation = loc;
		map.animateCamera(CameraUpdateFactory
				.newLatLng(new LatLng(mCurrentLocation.getLatitude(),
						mCurrentLocation.getLongitude())));
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}
}
