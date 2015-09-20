package com.example.frederic.tabtemplate;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;

import static android.support.v4.content.PermissionChecker.checkSelfPermission;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TabFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TabFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TabFragment extends Fragment
        implements LocationListener,
                    GoogleApiClient.ConnectionCallbacks,
                    GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "OFFER_FRAGMENT";  // debug tag for this fragment
    private OnFragmentInteractionListener mListener;
    private static Context mContext;
    // Map Ui and corresponding map
    private View fragment_view;
    private MapView mapView;
    private GoogleMap map;
    // Google api client for last known location
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    // Location manager for actual current location
    private LocationManager locationManager;
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;
    private static final int PREQCODE_FINE_LOCATION = 100;
    private boolean boolFineLocationConnected = false;
    // necessary permissions for this fragment - used for 6.0's runtime permissions api
    private static final String[] INITIAL_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TabFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TabFragment newInstance(Context context) {
        TabFragment fragment = new TabFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        mContext = context;
        return fragment;
    }

    public TabFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // google api client for getting last known location (used at the very start of the fragment)
        buildGoogleApiClient();
        mGoogleApiClient.connect();
        // location manager setup for getting access to gps and current location
        locationManager = (LocationManager) mContext.getSystemService(mContext.LOCATION_SERVICE);
        if (checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // TODO: Consider calling
            //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            requestPermissions(INITIAL_PERMISSIONS, PREQCODE_FINE_LOCATION);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragment_view = inflater.inflate(R.layout.fragment_map, container, false);
        // Get MapView from XML layout and call its onCreate
        mapView = (MapView) fragment_view.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        // Attach a googleMap to our mapview
        map = mapView.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setMyLocationEnabled(true);
        map.getUiSettings().setAllGesturesEnabled(false);
        // MapsInitializer does... well that
        MapsInitializer.initialize(this.getActivity()); // does not throw exc anymore but returns err code
        // Fallback location and zoom for the mapview if mLocation isn't yet set
        CameraUpdate cameraUpdate;
        if (mLastLocation == null) {
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(43.1, -86), 10);
            map.moveCamera(cameraUpdate);
        }
        // Inflate the layout for this fragment
        return fragment_view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                            int[] grantResults)
    {
        if (requestCode == PREQCODE_FINE_LOCATION
             && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        // google api client to retrieve the last known location of the device
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addApi(LocationServices.API)
                .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) this)
                .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) this)
                .build();
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "onLocationChanged");
        boolFineLocationConnected = true;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        moveMapWithOffset(latLng, 0.2);
        locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    /**
     * GoogleApiConnection callback
     * The connection is used to get the last known location - has nothing to do with the actual
     * gps access for getting the current location (onLocationChanged)
     * @param bundle
     */
    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "GoogleApiOnConnected");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (!boolFineLocationConnected) {
            // get last known location
            LatLng lastKnownLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            moveMapWithOffset(lastKnownLatLng, 0.2);
        }

    }

    /**
     * GoogleApiConnection callback
     * @param i
     */
    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiConnection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "GoogleApiConnection failed: " + connectionResult.toString());
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    private void moveMapWithOffset(LatLng pos, double center_offset) {
        // get maps height
        LinearLayout map_container = (LinearLayout) fragment_view.findViewById(R.id.map_container);
        int map_container_height = map_container.getHeight();
        // get last known location
        CameraUpdate camUpdate = CameraUpdateFactory.newLatLngZoom(pos, 16);
        map.moveCamera(camUpdate);
        // get point half a screen above that
        Point screenPosition = map.getProjection().toScreenLocation(pos);
        Point pointWithOffset;
        if (center_offset != 0) {
            int divisor = (int) Math.round(Math.pow(center_offset, -1));
            pointWithOffset = new Point(screenPosition.x,
                    screenPosition.y - (map_container_height / divisor));
        } else {
            pointWithOffset = screenPosition;
        }
        LatLng latLngWithOffset = map.getProjection().fromScreenLocation(pointWithOffset);
        camUpdate = CameraUpdateFactory.newLatLngZoom(latLngWithOffset, 16);
        map.moveCamera(camUpdate);
    }

}
