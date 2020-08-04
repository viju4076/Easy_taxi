/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.starter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;


public class MainActivity extends AppCompatActivity {
  Switch aSwitch;
  public void redirect()
  {
      if(ParseUser.getCurrentUser().get("riderOrdriver").equals("rider"))
      {
          Intent intent=new Intent(MainActivity.this,RiderActivity.class);
          finish();
          startActivity(intent);

      }
      else
      {
        Intent intent=new Intent(MainActivity.this,NearbyRequests.class);
        finish();
        startActivity(intent);

      }
  }

  public  void login(View view) throws ParseException {String userType;
    if(aSwitch.isChecked())
    {
      Log.i("result","Login as a driver");
      userType="driver";
    }
    else
    {
      Log.i("result","Login as a rider");
      userType="rider";
    }

    ParseUser.getCurrentUser().put("riderOrdriver",userType);
ParseUser.getCurrentUser().save();
 //   Log.i("username",ParseUser.getCurrentUser().getUsername());
redirect();

  }


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    aSwitch= (Switch) findViewById(R.id.switch1);

    getSupportActionBar().hide();
 ParseUser.logOut();
    if(ParseUser.getCurrentUser()==null)
    {
      ParseAnonymousUtils.logIn(new LogInCallback() {
        @Override
        public void done(ParseUser user, ParseException e) {
          if(e==null)
          {
            Log.i("Info","Anonymous Login Successful");
            ParseUser.getCurrentUser().put("riderOrDriver","rider");
            try {
              ParseUser.getCurrentUser().save();
            } catch (ParseException ex) {
              ex.printStackTrace();
            }
          }
          else
          {
            Log.i("Info","Anonyomous Login failed");
          }
        }
      });
    }
    else
    {
      Log.i("resutl","Login");
      if(ParseUser.getCurrentUser().get("riderOrdriver")!=null)
      {
        Log.i("Info","Redirecting as ="+ParseUser.getCurrentUser().get("riderOrdriver"));
        redirect();
      }
      else
      {
        Log.i("Info","Redirecting as ="+"Could not");

      }
    }

    ParseAnalytics.trackAppOpenedInBackground(getIntent());
  }

}