package com.tehkonnos.lighthouse.ui.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.GeoPoint;
import com.tehkonnos.lighthouse.MainActivity;
import com.tehkonnos.lighthouse.R;
import com.tehkonnos.lighthouse.ui.Map.SavePerson.SavePerson;
import com.tehkonnos.lighthouse.ui.firestore.DatabaseData;
import com.tehkonnos.lighthouse.ui.firestore.Feed;
import com.tehkonnos.lighthouse.ui.firestore.SavedMarker;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowLongClickListener, GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;


    private static final String TAG = MapsFragment.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;
    private LocationCallback locationCallback;


    private static List<SavedMarker> myMarker = new ArrayList<>();
    private static List<Feed> feedList = new ArrayList<>();


    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    Dialog myDialog;


    //private String lastKnownMap = "mMap";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_maps, container, false);

        FloatingActionButton fab = root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SavePerson.class);
                try {
                    intent.putExtra("latitude", lastKnownLocation.getLatitude());
                    intent.putExtra("longitude", lastKnownLocation.getLongitude());
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("Error send intent: ", "" + e.getMessage() + " with error code: " + e.hashCode());
                }
            }
        });

        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            CameraPosition cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        DatabaseData databaseData = MainActivity.getDatabaseData();

        databaseData.getMutableFeed().observe(getViewLifecycleOwner(), new Observer<List<Feed>>() {
            @Override
            public void onChanged(List<Feed> feeds) {
                feedList = feeds;
            }
        });

        databaseData.getMutableMarker().observe(getViewLifecycleOwner(), new Observer<List<SavedMarker>>() {
            @Override
            public void onChanged(List<SavedMarker> savedMarkers) {
                myMarker = savedMarkers;
            }
        });

        myDialog = new Dialog(requireContext());

        return root;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, lastKnownLocation);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnInfoWindowLongClickListener(this);

        //Change style if Dark Mode is enabled
        setMapStyle();

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        //Load Markers from Feed
        loadMarkers();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
    }

    public void ShowPopup(final int id) {
        myDialog.setContentView(R.layout.custompopup);

        TextView location = myDialog.findViewById(R.id.popup1location);
        location.setText(findAddress(feedList.get(id).getGeoPoint()));

        TextView description = myDialog.findViewById(R.id.popup1Desc);
        description.setText(myMarker.get(id).getDescription());

        TextView needs = myDialog.findViewById(R.id.popup1Needs);
        needs.setText(myMarker.get(id).getNeeds());

        TextView comments = myDialog.findViewById(R.id.popup1Comments);
        String comment = myMarker.get(id).getComments();
        if (comment.equals("")) comments.setText("Δεν υπάρχει κάποιο σχόλιο");
        else comments.setText(comment);

        Button btn = myDialog.findViewById(R.id.popup1btnMap);
        btn.setVisibility(View.INVISIBLE);

        TextView txtclose;
        txtclose = myDialog.findViewById(R.id.txtclose);
        txtclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });

        Objects.requireNonNull(myDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        }
        updateLocationUI();
    }


    private void getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
                if (getArguments() != null) {
                    stopLocationUpdates();
                    GeoPoint geo = feedList.get(getArguments().getInt("ID")).getGeoPoint();
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(geo.getLatitude(), geo.getLongitude()), 17));
                } else {
                    startLocationUpdates();
                }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }


    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", Objects.requireNonNull(e.getMessage()));
        }
    }

    private void loadMarkers() {
        try{
            for (int i = 0; i < feedList.size(); i++) {
                String title = myMarker.get(i).getDescription();
                LatLng latLng = new LatLng(feedList.get(i).getGeoPoint().getLatitude(), feedList.get(i).getGeoPoint().getLongitude());
                String locality = findAddress(feedList.get(i).getGeoPoint());
                mMap.addMarker(new MarkerOptions().position(latLng).title(title).snippet(locality));
                //Set Marker Color taken by category
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private String findAddress(GeoPoint geopoint) {
        Geocoder geocoder = new Geocoder(getContext());
        try {
            List<Address> addressList = geocoder.getFromLocation(geopoint.getLatitude(), geopoint.getLongitude(), 1);
            String locality = addressList.get(0).getAddressLine(0);
            if (locality == null) locality = "Άγνωστη τοποθεσία";

            return locality;
        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage());
            return "Άγνωστη τοποθεσία";
        }
    }

    @Override
    public void onInfoWindowLongClick(Marker marker) {
        System.out.println("Marker ID Long: " + marker.getId());
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        String idstr = marker.getId().replace("m", "");
        int id = Integer.parseInt(idstr);
        ShowPopup(id);
    }

    private void setMapStyle() {
        int nightModeFlags =
                requireContext().getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.maps_night));
        }
    }

    private void stopLocationUpdates() {
        try {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }catch(Exception e){
            Log.e("MapsFragment stopLocation: ",""+e.getMessage());
        }
    }


    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        long UPDATE_INTERVAL = 10 * 1000; /* 10 secs */
        long FASTEST_INTERVAL = 2 * 1000; /* 2 sec */
        final LocationRequest locationRequest = new LocationRequest();

        locationRequest.setPriority(LocationRequest.PRIORITY_NO_POWER);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(requireActivity());
        settingsClient.checkLocationSettings(locationSettingsRequest);
        try {
            if(locationPermissionGranted) {
                fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                        locationCallback = new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                lastKnownLocation = locationResult.getLastLocation();
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), 15));
                            }
                        },
                        Looper.getMainLooper());
            }else{
                mMap.moveCamera(CameraUpdateFactory
                        .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();
        startLocationUpdates();
    }
}
