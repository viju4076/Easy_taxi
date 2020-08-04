package com.parse.starter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


public class NearbyRequests extends AppCompatActivity {
LocationManager locationManager;
LocationListener locationListener;
Location lastknowlocation;
boolean clickable=true;

public void update()
{
    if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        lastknowlocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        // update();
    }

        if (lastknowlocation != null) {
            ParseUser.getCurrentUser().put("location",new ParseGeoPoint(lastknowlocation.getLatitude(),lastknowlocation.getLongitude()));
            ParseUser.getCurrentUser().saveInBackground();
            final ListView list=findViewById(R.id.list);
            final ArrayList<String> distance=new ArrayList<String>();
            distance.clear();
            final ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_expandable_list_item_1,distance);
            final ArrayList<String> username=new ArrayList<String>();
                 ParseGeoPoint location1=new ParseGeoPoint(lastknowlocation.getLatitude(),lastknowlocation.getLongitude());

            ParseQuery<ParseObject> query=new ParseQuery<ParseObject>("Request");
            query.whereDoesNotExist("driverUsername");
            query.whereNear("location",location1);

            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e==null)
                    {
                    Log.i("objects",objects.size()+"");
                        if(objects.size()>0)
                        {
                            clickable=true;
                            for(ParseObject object:objects) {
                                ParseGeoPoint parseGeoPoint = object.getParseGeoPoint("location");
                                distance.add(parseGeoPoint.distanceInMilesTo(new ParseGeoPoint(lastknowlocation.getLatitude(), lastknowlocation.getLongitude())) + " miles");
                            username.add((String)object.get("username"));
                            }
                            list.setAdapter(arrayAdapter);
                        }
                        else
                        {
                            distance.clear();
                            username.clear();
                         clickable=false;
                            distance.add("No new requests found.......");
                            arrayAdapter.notifyDataSetChanged();

                       list.setAdapter(arrayAdapter);
                         }


                    }
                    else
                    {
                        Log.i("Exception","occured");
                    }
                }
            });
                        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                  @Override
                  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if(clickable) {
                        Intent intent = new Intent(getApplicationContext(), DriverActivity.class);

                        intent.putExtra("username", username.get(position));

                        startActivity(intent);
                    }

                  }
              });









        }
        else
        {
            Toast.makeText(getApplicationContext(),"could not find your loaction",Toast.LENGTH_SHORT).show();
        }


    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_requests);
        setTitle("Near By Requests");
         locationManager= (LocationManager) this.getSystemService(LOCATION_SERVICE);
         locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                update();
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
     update();

}
}