package eu.pendual.gcuevents.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.sql.Time;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import eu.pendual.gcuevents.R;
import eu.pendual.gcuevents.app.AppConfig;
import eu.pendual.gcuevents.app.AppController;
import eu.pendual.gcuevents.helper.SQLiteHandler;


/**
 * Created by James on 09/04/2018.
 */

public class NewEventActivity extends Activity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    int PLACE_PICKER_REQUEST = 1;
    private Button btnCreateEvent;
    private Button btnGetAddress;
    private Button btnSetStart;
    private Button btnSetFinish;
    private EditText inputEventTitle;
    private EditText inputEventDescription;
    private TextView inputEventLocation;
    private ProgressDialog pDialog;
    private static final String TAG = NewEventActivity.class.getSimpleName();
    private SQLiteHandler db;

     String eventTitle;
     String eventDescription;
     String eventLocation;
     Double eventLat;
     Double eventLon;
     String pickedDate;
     String eventTime;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newevent);


        //Load items
        btnCreateEvent = (Button) findViewById(R.id.btnGo);
        btnGetAddress = (Button) findViewById(R.id.getAddress);
        btnSetFinish = (Button) findViewById(R.id.setFinish);
        btnSetStart = (Button) findViewById(R.id.setStart);
        inputEventTitle = (EditText) findViewById(R.id.eventTitle);
        inputEventDescription = (EditText) findViewById(R.id.eventDescription);
        inputEventLocation = (TextView) findViewById(R.id.eventLocation);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        //Handling location input
        btnGetAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handlePlacePicker();
            }
        });

        //Handling Date/Time inputs

        //Set Start date of event
        btnSetStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
                int startYear = calendar.get(Calendar.YEAR);
                int startMonth = calendar.get(Calendar.MONTH );
                int startDay = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(NewEventActivity.this, NewEventActivity.this, startYear, startMonth, startDay);
                System.out.println("Opening Datepicker...");
                datePickerDialog.show();
            }
        });

        //Set start time of event
        btnSetFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
                int startHour = calendar.get(Calendar.HOUR_OF_DAY);
                int startMinute = calendar.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(NewEventActivity.this, NewEventActivity.this, startHour, startMinute, android.text.format.DateFormat.is24HourFormat(NewEventActivity.this));
                System.out.println("Opening Timepicker...");
                timePickerDialog.show();
            }
        });

        //Handle Event Create button functions
        btnCreateEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventTitle = inputEventTitle.getText().toString();
                eventDescription = inputEventDescription.getText().toString();
                eventLocation = inputEventLocation.getText().toString();
                if (!eventTitle.isEmpty() && !eventDescription.isEmpty() && !eventLocation.isEmpty() && (eventLat != null) && (eventTime != null) && (pickedDate != null)) {
                    registerUser(eventTitle, eventDescription, eventLocation, eventLat, eventLon, pickedDate, eventTime);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Please make sure you've filled every field!", Toast.LENGTH_LONG)
                            .show();
                }

            }
        });

    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        i1 = i1+1;
        System.out.println(i);
        System.out.println(i1);
        System.out.println(i2);
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.set(Calendar.YEAR, i);
        calendar.set(Calendar.MONTH, i1);
        calendar.set(Calendar.DATE, i2);
        pickedDate = String.format("%02d/%02d/%04d", i2, i1, i);
        System.out.println(pickedDate);
        System.out.println("Date set!");
        System.out.println("=====================================");
        btnSetStart.setText("Event Date: " + i2 + "/" + i1 + "/" + i);
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hours, int mins){
        eventTime = String.format("%02d:%02d", hours, mins);
        btnSetFinish.setText("Event Time: " + eventTime);
    }

    @Override
    public void onBackPressed() {
    finish();
    }

    public void handlePlacePicker(){
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try{
            startActivityForResult(builder.build(NewEventActivity.this),PLACE_PICKER_REQUEST);
        }catch (GooglePlayServicesNotAvailableException | GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        if(requestCode == PLACE_PICKER_REQUEST){
            if (resultCode == RESULT_OK){
                Place place = PlacePicker.getPlace(NewEventActivity.this, data);
                inputEventLocation.setText( place.getAddress().toString());
                eventLocation = place.getAddress().toString();
                LatLng queriedLocation = place.getLatLng();
                eventLat = queriedLocation.latitude;
                eventLon = queriedLocation.longitude;
            }
        }
    }

    /**
     * Function to store user in MySQL database will post params(tag, name,
     * email, password) to register url
     * */
    private void registerUser(final String eventTitle, final String eventDescription, final String eventLocation, final Double eventLat, final Double eventLon, final String pickedDate, final String eventTime) {
        // Tag used to cancel the request
        String tag_string_req = "req_createEvent";

        pDialog.setMessage("Creating event ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_CREATEEVENT, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // Event successfully stored in MySQL
                        Toast.makeText(getApplicationContext(), "Event created successfully!", Toast.LENGTH_LONG).show();

                        // Launch Home activity
                        Intent intent = new Intent(
                                NewEventActivity.this,
                                MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {

                        // Error occurred in event creation. Get the error
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
                Log.e(TAG, "Creation Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", eventTitle);
                params.put("description", eventDescription);
                params.put("location", eventLocation);
                params.put("latitude", eventLat.toString());
                params.put("longitude", eventLon.toString());
                params.put("date", pickedDate);
                params.put("time", eventTime);
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
