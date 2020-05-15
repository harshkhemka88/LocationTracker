package com.example.locationtracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

// Trial comment to see how GitHub works

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    // STEP 9
    // Note: Step 8 is in the text tab of activity_maps.xml
    // Declare variables on xml, Firebase variables, and location variables
    EditText etLatitude, etLongitude;
    Button btnUpdate;
    FirebaseFirestore fStore;
    DocumentReference documentReference;
    CollectionReference collectionReference;
    LocationListener locationListener; // location variable
    LocationManager locationManager; // location variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // STEP 5
        // To always get user permission for location -- not checking, always asking
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION}, PackageManager.PERMISSION_GRANTED);
        // We will ask for location permission in the code above
        // Step 6 is in activity_maps.xml

        // STEP 10
        // Initialize all xml and Firebase variables
        etLatitude = findViewById(R.id.etLatitude);
        etLongitude = findViewById(R.id.etLongitude);
        btnUpdate = findViewById(R.id.btnUpdate);
        fStore = FirebaseFirestore.getInstance();

        // STEP 11
        // Also initialize collectionReference to create a new collection called "locations" -- why?
        // We need them to later receive our location points from firebase
        collectionReference = fStore.collection("locations");
        documentReference = collectionReference.document("myLocations");

        // STEP 12
        // Create a listener for value change
        // addSnapshotListener -- choose with activity and EventListener -- automatically creates onEvent method`
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                // STEP 28
                // Our text boxes send data to firebase in step 27, and here we receive the sent location from firebase
                // We receive location points in variables
                String dbLat = documentSnapshot.getString("latitude");
                String dbLong = documentSnapshot.getString("longitude");

                // STEP 29
                // Now we want to show the data we got above, on the map
                // First create a lat long variable to store the data above
                LatLng latLng = new LatLng(Double.parseDouble(dbLat), Double.parseDouble(dbLong));
                // Double.parseDouble is used as LatLng takes double value and our values are string

                // STEP 30/30
                // Put this data on our map and move the camera to that location
                mMap.addMarker(new MarkerOptions().position(latLng).title(dbLat + ", " + dbLong));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5));
                // Now when we run the app, it is working fine
            }
        });

        // STEP 13
        // setup on click listener for button
        // Step 14 is in onMapReady method
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // STEP 24
                // Whenever the user presses this button, we want to first extract the string in lat and long edit texts
                // And the document where we want to put the data
                String lat = etLatitude.getText().toString();
                String lng = etLongitude.getText().toString();
                documentReference = fStore.collection("locations").document("myLocations");

                // STEP 25
                // After getting the values, we need to add them to Firebase using HashMap
                final Map<String, Object> location = new HashMap<>();

                // STEP 26
                // Add values in key-value format
                location.put("latitude", lat);
                location.put("longitude", lng);

                // STEP 27
                // Now put this to our document
                documentReference.set(location);
                // Now go to step 12 above to receive data from firebase -- step 28
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // STEP 14
        // Comment out the below 3 lines of code
        // LatLng sydney = new LatLng(-34, 151);
        // mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        // mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        // STEP 15
        // We want to show the location of the user when the map is ready
        // For that we use locationListener -- new LocationListener() -- automatically creates onLocationChanged, onStatusChanged, onProviderEnabled, onProviderDisabled methods
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                // STEP 16
                // Use-try catch in case there is an error in maps
                try
                {
                    // STEP 18
                    // Whenever location is changed on map, we want to set the edit text to the latitude and longitude on the map
                    // location.getLatitude() is double -- set text needs string -- so we have converted double to string
                    etLatitude.setText(Double.toString(location.getLatitude())); // location is the parameter of this method
                    etLongitude.setText(Double.toString(location.getLongitude()));
                    // Step 19 is at the end of this file

                }

                catch (Exception e)
                {
                    // STEP 17
                    // Print error message
                    Toast.makeText(MapsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
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
        };

        // STEP 19
        // After setting up the location listener, we want to set up the location manager
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // We get the location service

        // STEP 20
        // We get location service and request for location updates
        // requestLocationUpdates -- choose the one with 4 options -- manager, minTime, minDistance, and listener
        // locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER, 1000, 50, locationListener);

        // STEP 21
        // Step 20 throws an error that we need to ask for user permission to access location
        // so do that by using an if statement
       if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                                                != PackageManager.PERMISSION_GRANTED)
       {
           // STEP 22
           // Ask for permission
           ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
       }

       // STEP 23
        // Now do step 20
        locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER, 1000, 50, locationListener);
       // step 24 is in on click update button
    }
}
