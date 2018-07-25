package eu.pendual.gcuevents.activity;

/**
 * Created by James Craig S1428641
 */

import android.content.Intent;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import com.uber.sdk.android.core.UberSdk;
import com.uber.sdk.android.rides.RideParameters;
import com.uber.sdk.rides.client.SessionConfiguration;
import com.uber.sdk.android.rides.RideRequestButton;


import eu.pendual.gcuevents.R;
import eu.pendual.gcuevents.app.AppConfig;
import eu.pendual.gcuevents.app.AppController;
import eu.pendual.gcuevents.helper.SQLiteHandler;

public class DisplayInfoActivity extends AppCompatActivity {

    String title;
    String description;
    String location;
    String date;
    String time;
    String uuidEvent;
    String uuidUser;
    String latitude;
    String longitude;
    TextView titleTV;
    TextView descriptionTV;
    TextView datetimeTV;
    TextView locationTV;
    Button btnAttendEvent;
    Button btnAttending;
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private SQLiteHandler db;
    RideRequestButton requestButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_info);
        getDisplayInformation();
        displayInfo();
        btnAttendEvent = (Button) findViewById(R.id.btnAttendEvent);
        btnAttending = (Button) findViewById(R.id.btnAttending);
        btnAttending.setVisibility(View.INVISIBLE);
        requestButton = (RideRequestButton) findViewById(R.id.requestButton);

        SessionConfiguration config = new SessionConfiguration.Builder()
                // mandatory
                .setClientId("<BsGVC_oHumlPz0byB2lk-wwuXAkO_6tl>")
                // required for enhanced button features
                .setServerToken("<4DxHyBSjYE4cTu6eOqBCUQUVwtcm_ItkYLwcQ4Zk>")
                // required for implicit grant authentication
                .setRedirectUri("<http://127.0.0.1>")
                // optional: set sandbox as operating environment
                .setEnvironment(SessionConfiguration.Environment.SANDBOX)
                .build();

        UberSdk.initialize(config);

        db = new SQLiteHandler(getApplicationContext());
        final HashMap<String, String> user = db.getUserDetails();
        checkAttending(user.get("uid"), uuidEvent);
        btnAttendEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uuidUser = user.get("uid");
                registerUser(uuidUser, uuidEvent);
            }
        });



        RideParameters rideParams = new RideParameters.Builder()
                // Required for price estimates; lat (Double), lng (Double), nickname (String), formatted address (String) of dropoff location
                .setDropoffLocation(
                        Double.parseDouble(latitude), Double.parseDouble(longitude), title, location)
                .build();
                // set parameters for the RideRequestButton instance
                requestButton.setRideParameters(rideParams);

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getDisplayInformation(){
        Intent i = getIntent();
        Bundle extras=i.getExtras();
        String[] incidentStrings = new String[5];

        if(extras != null)  //this line is necessary for getting any value
        {
            incidentStrings = i.getStringArrayExtra("incidentStringArray");
        }

        title = incidentStrings[0];
        description = incidentStrings[1];
        location = incidentStrings[2];
        date = incidentStrings[3];
        time = incidentStrings[4];
        uuidEvent = incidentStrings[5];
        latitude = incidentStrings[6];
        longitude = incidentStrings[7];
    }

    private void displayInfo() {
        titleTV = (TextView) findViewById(R.id.titleTV);
        descriptionTV = (TextView) findViewById(R.id.descriptionTV);
        datetimeTV = (TextView) findViewById(R.id.datetimeTV);
        locationTV = (TextView) findViewById(R.id.locationTV);

        titleTV.setText(title);
        descriptionTV.setText(description);
        datetimeTV.setText(date + " " + time);
        locationTV.setText(location);
    }

    private void registerUser(final String uuidUser, final String uuidEvent){
        // Tag used to cancel the request
        String tag_string_req = "req_attend";


        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_ATTEND, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // User successfully stored in MySQL
                        // Now store the user in sqlite
                        Toast.makeText(getApplicationContext(), "You've been signed up!", Toast.LENGTH_LONG).show();
                        btnAttending.setVisibility(View.VISIBLE);
                        btnAttendEvent.setVisibility(View.GONE);
                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("uuidUser", uuidUser);
                params.put("uuidEvent", uuidEvent);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void checkAttending(final String uuidUser, final String uuidEvent){
        // Tag used to cancel the request
        String tag_string_req = "req_attend";


        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_ATTENDCHECK, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());

                Boolean error =  Boolean.valueOf(response);
                if (!error) {

                } else {
                    btnAttending.setVisibility(View.VISIBLE);
                    btnAttendEvent.setVisibility(View.GONE);
                    // Error occurred in registration. Get the error
                    // message
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("uuidUser", uuidUser);
                params.put("uuidEvent", uuidEvent);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }
}

