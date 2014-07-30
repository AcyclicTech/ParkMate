package com.acyclictech.android.wheredipark;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends Activity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, LocationListener,
		android.location.LocationListener {

	private GoogleMap map;
	private LocationClient mLocationClient;
	private Location mCurrentLocation;
	private LocationRequest mLocationRequest;
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

		LocationManager locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, this);
		mLocationClient = new LocationClient(this, this, this);
		mLocationRequest = LocationRequest.create();

		mLocationRequest
				.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
		// Set the update interval to 5 seconds
		mLocationRequest.setInterval(UPDATE_INTERVAL);
		// Set the fastest update interval to 1 second
		mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

//		if (savedInstanceState == null) {
//			MapFragment mMapFragment = MapFragment.newInstance();
//			FragmentTransaction fragmentTransaction = getFragmentManager()
//					.beginTransaction();
//			fragmentTransaction.add(R.id.map, mMapFragment);
//			fragmentTransaction.commit();
//		}

		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();

		int status = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getApplicationContext());
		if (status != ConnectionResult.SUCCESS) {
			Dialog d = GooglePlayServicesUtil.getErrorDialog(status, this,
					CONNECTION_FAILURE_RESOLUTION_REQUEST);
			d.show();
		}

//		LayoutInflater inflater = (LayoutInflater) getApplicationContext()
//				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		View view = inflater.inflate(R.layout.activity_map, null, false);
//
		// MapView mapView = (MapView) view.findViewById(R.id.mapView);
		// mapView.onCreate(savedInstanceState);
		// map = mapView.getMap();
		map.setMyLocationEnabled(true);

		map.setOnMapClickListener(new OnMapClickListener() {

			@Override
			public void onMapClick(LatLng latLng) {
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
						map.addMarker(new MarkerOptions().position(latLng).title(
								addressName.toString()));
					}
				} catch (IOException e1) {
				}

			}
		});
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
			return true;
		}
		return super.onOptionsItemSelected(item);
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
