package com.parse.starter;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class DriverActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
  LocationManager locationManager;
  LocationListener locationListener;
  String username;
  Location lastknowlocation;
  ParseGeoPoint parseGeoPoint;
public void accept(View view)
{
    Log.i("Acccept","button invoked");
    ParseQuery<ParseObject> query=new ParseQuery<ParseObject>("Request");
    query.whereEqualTo("username",username);
    query.findInBackground(new FindCallback<ParseObject>() {
                               @Override
                               public void done(List<ParseObject> objects, ParseException e) {
                                   if (e == null) {
                                       if (objects.size() > 0) {
                                           for (ParseObject object : objects) {
                                               object.put("driverUsername", ParseUser.getCurrentUser().getUsername());
                                               object.saveInBackground(new SaveCallback() {
                                                   @Override
                                                   public void done(ParseException e) {
                                                       if(e==null)
                                                       {
                                                           Uri gmmIntentUri = Uri.parse("google.navigation:q="+parseGeoPoint.getLatitude()+","+parseGeoPoint.getLongitude());
                                                           Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                                           mapIntent.setPackage("com.google.android.apps.maps");
                                                           finish();
                                                           startActivity(mapIntent);


                                                       }
                                                   }
                                               });
                                           }
                                       }
                                   }
                               }
                           });





}

  public void showpoints()
  {

      if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) {
          locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
          lastknowlocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
      }
      if(lastknowlocation!=null)
      {
          ParseQuery<ParseObject> query=new ParseQuery<ParseObject>("Request");
          query.whereEqualTo("username",username);
          query.findInBackground(new FindCallback<ParseObject>() {
              @Override
              public void done(List<ParseObject> objects, ParseException e) {
                  if(e==null)
                  {
                      if(objects.size()>0)
                      {
                          for(ParseObject object:objects)
                          {
                               parseGeoPoint=object.getParseGeoPoint("location");
                              LatLng userlocation=new LatLng(parseGeoPoint.getLatitude(),parseGeoPoint.getLongitude());
                              ArrayList<Marker> markers=new ArrayList<Marker>();
                              mMap.clear();
                              LatLng driverlocation=new LatLng(lastknowlocation.getLatitude(),lastknowlocation.getLongitude());
                              markers.add(mMap.addMarker(new MarkerOptions().position(userlocation).title("Drivers Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))));
                             markers.add(mMap.addMarker(new MarkerOptions().position(driverlocation).title("Riders Location")));
                              LatLngBounds.Builder builder=new LatLngBounds.Builder();
                              for(Marker marker:markers)
                              {
                                  builder.include(marker.getPosition());
                              }
                              LatLngBounds latLngBounds=builder.build();
                              CameraUpdate cu=CameraUpdateFactory.newLatLngBounds(latLngBounds,60);
                              mMap.animateCamera(cu);

                          }
                      }
                  }
              }
          });
      }
  }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);
        Intent intent=getIntent();
        username=intent.getStringExtra("username");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        locationManager= (LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                showpoints();
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
        showpoints();


        }
}
