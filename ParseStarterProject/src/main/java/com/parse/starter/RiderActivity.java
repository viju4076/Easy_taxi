package com.parse.starter;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

public class RiderActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
   Button callUberButton;
   boolean requestActive=false;
   Handler handler=new Handler();
   TextView infoText;
   boolean driverActive=false;
   public void logout(View view)
   {
       ParseUser.logOut();
       Intent intent=new Intent(getApplicationContext(),MainActivity.class);
       finish();
       startActivity(intent);
   }
public void checkForUpdates()
{
    final ParseQuery<ParseObject> query=new ParseQuery<ParseObject>("Request");
    query.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
    query.whereExists("driverUsername");
    query.findInBackground(new FindCallback<ParseObject>() {
        @Override
        public void done(List<ParseObject> objects, ParseException e) {
            if(e==null)
            {
                if(objects.size()>0)
                {
                    driverActive=true;
                  ParseQuery<ParseUser> query1=ParseUser.getQuery();
                  query1.whereEqualTo("username",objects.get(0).getString("driverUsername"));
                  query1.findInBackground(new FindCallback<ParseUser>() {
                      @Override
                      public void done(List<ParseUser> objects, ParseException e) {
                          if(e==null)
                          {
                              ParseGeoPoint driverLocation=objects.get(0).getParseGeoPoint("location");
                              if(ContextCompat.checkSelfPermission(RiderActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
                                  Location lastknowlocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                               if(lastknowlocation!=null)
                               {
                                   ParseGeoPoint userLocation=new ParseGeoPoint(lastknowlocation.getLatitude(),lastknowlocation.getLongitude());
                                   double distance=driverLocation.distanceInMilesTo(userLocation);
                                   distance=(double)(Math.round(distance*100))/100;
                                   if(distance<0.01)
                                   {
                                       infoText.setText("Your Driver is here!");

                                       ParseQuery<ParseObject> query2=ParseQuery.getQuery("Request");
                                       query2.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
                                       query2.findInBackground(new FindCallback<ParseObject>() {
                                           @Override
                                           public void done(List<ParseObject> objects, ParseException e) {
                                               if(e==null)
                                               {
                                                   if(objects.size()>0)
                                                   {
                                                       for(ParseObject object:objects)
                                                       {
                                                           object.deleteInBackground();
                                                       }
                                                   }
                                               }
                                           }
                                       });
                                       handler.postDelayed(new Runnable() {
                                           @Override
                                           public void run() {
                                              infoText.setText("");
                                               callUberButton.setVisibility(View.VISIBLE);
                                               callUberButton.setText("Call an Uber");
                                               requestActive=false;
                                               driverActive=false;

                                           }
                                       },5000);

                                   }
                                   else {


                                       infoText.setText("Your Driver is " + distance + "miles away from your location");
                                       LatLng Userlocation = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
                                       ArrayList<Marker> markers = new ArrayList<Marker>();
                                       mMap.clear();
                                       LatLng driverlocation = new LatLng(driverLocation.getLatitude(), driverLocation.getLongitude());
                                       markers.add(mMap.addMarker(new MarkerOptions().position(Userlocation).title("Your Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))));
                                       markers.add(mMap.addMarker(new MarkerOptions().position(driverlocation).title("Riders Location")));
                                       LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                       for (Marker marker : markers) {
                                           builder.include(marker.getPosition());
                                       }
                                       LatLngBounds latLngBounds = builder.build();
                                       CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(latLngBounds, 60);
                                       mMap.animateCamera(cu);
                                       callUberButton.setVisibility(View.INVISIBLE);



                                   }





                               }

                              }
                              }
                      }
                  });



                }
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        checkForUpdates();
                    }
                },2000);

            }
        }
    });

}
   public void callUber(View view)
   {
       if(requestActive)
       {
           ParseQuery<ParseObject> parseQuery =new ParseQuery<ParseObject>("Request");
           parseQuery.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
           parseQuery.findInBackground(new FindCallback<ParseObject>() {
               @Override
               public void done(List<ParseObject> objects, ParseException e) {
                   if(e==null)
                   {
                       if(objects.size()>0)
                       {
                           for(ParseObject parseObject:objects)
                           {
                               parseObject.deleteInBackground();
                           }

                           requestActive=false;
                           callUberButton.setText("Call an Uber");
                       }
                   }
               }
           });


       }
       else
       {
           if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
               locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
               Location lastknowlocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

               if(lastknowlocation!=null) {
                   final ParseObject request = new ParseObject("Request");
                   request.put("username", ParseUser.getCurrentUser().getUsername());
                   ParseGeoPoint parseGeoPoint=new ParseGeoPoint(lastknowlocation.getLatitude(),lastknowlocation.getLongitude());
                   request.put("location",parseGeoPoint);
                   request.saveInBackground(new SaveCallback() {
                       @Override
                       public void done(ParseException e) {
                           if(e==null)
                           {
                               requestActive=true;
                               callUberButton.setText("Cancel Uber");
                            checkForUpdates();
                           }
                       }
                   });
               }
               else
               {
                   Toast.makeText(this,"Could not find location. Please try again later",Toast.LENGTH_SHORT).show();
               }
           }
       }


   }
    public void updateMap(Location location)
    {
        if(driverActive==false) {
            LatLng userlocation = new LatLng(location.getLatitude(), location.getLongitude());

            mMap.clear();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userlocation, 15));
            mMap.addMarker(new MarkerOptions().position(userlocation).title("Your Location"));
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if(requestCode==1)
    {
        if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
        {
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
            {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
            Location lastknowlocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            updateMap(lastknowlocation);
            }

        }
    }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider);
        callUberButton=findViewById(R.id.button);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
         infoText=findViewById(R.id.infoText);
        ParseQuery<ParseObject> parseQuery =new ParseQuery<ParseObject>("Request");
        parseQuery.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e==null)
                {
                    if(objects.size()>0)
                    {
                        requestActive=true;
                        callUberButton.setText("Cancel Uber");
                        checkForUpdates();
                    }
                }
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

     locationManager=(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
     locationListener=new LocationListener() {
         @Override
         public void onLocationChanged(Location location) {
         updateMap(location);

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
        // Add a marker in Sydney and move the camera



        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
       {
           ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
       }
       else
       {
           locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
        Location lastknowlocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

           if(lastknowlocation!=null)
       {
           updateMap(lastknowlocation);
       }

       }

        }
}
