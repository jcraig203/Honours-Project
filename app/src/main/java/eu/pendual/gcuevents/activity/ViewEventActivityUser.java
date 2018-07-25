package eu.pendual.gcuevents.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import eu.pendual.gcuevents.R;
import eu.pendual.gcuevents.app.AppConfig;
import eu.pendual.gcuevents.app.AppController;
import eu.pendual.gcuevents.containers.Event;
import eu.pendual.gcuevents.helper.SQLiteHandler;

public class ViewEventActivityUser extends Activity implements DatePickerDialog.OnDateSetListener {
    ArrayList<Event> eventList = new ArrayList<Event>();
    RecyclerView recyclerView;
    eu.pendual.gcuevents.activity.EventsAdapter eAdapter;
    private static final String TAG = RegisterActivity.class.getSimpleName();
    ToggleButton searchButton;
    Button byTitleButton;
    Button byDateButton;
    String pickedDate;
    Button viewAllButton;
    String m_Text;
    private SQLiteHandler db;
    HashMap<String, String> user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewevents);
        searchButton = (ToggleButton) findViewById(R.id.searchButton);
        byTitleButton = (Button) findViewById(R.id.byTitleButton);
        byTitleButton.setVisibility(View.GONE);
        byDateButton = (Button) findViewById(R.id.byDateButton);
        byDateButton.setVisibility(View.GONE);
        viewAllButton = (Button) findViewById(R.id.viewAllButton);
        viewAllButton.setVisibility(View.GONE);
        m_Text = "";
        db = new SQLiteHandler(getApplicationContext());
        user = db.getUserDetails();

        new getAllEvents().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


        //toggle search buttons on/off
        searchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    byDateButton.setVisibility(View.VISIBLE);
                    byTitleButton.setVisibility(View.VISIBLE);
                    viewAllButton.setVisibility(View.VISIBLE);
                } else {
                    byDateButton.setVisibility(View.GONE);
                    byTitleButton.setVisibility(View.GONE);
                    viewAllButton.setVisibility(View.GONE);
                }
            }
        });

        //Search by title
        byTitleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewEventActivityUser.this);
                builder.setTitle("Title");

                // Set up the input
                final EditText input = new EditText(ViewEventActivityUser.this);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_Text = input.getText().toString();
                        new getEventsByTitle().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        //Search by date
        byDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
                int startYear = calendar.get(Calendar.YEAR);
                int startMonth = calendar.get(Calendar.MONTH);
                int startDay = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(ViewEventActivityUser.this, ViewEventActivityUser.this, startYear, startMonth, startDay);
                System.out.println("Opening Datepicker...");
                datePickerDialog.show();
            }
        });

        //reset/search all
        viewAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new getAllEvents().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        i1 = i1 + 1;
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
        new getEventsByDate().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private class getAllEvents extends AsyncTask {

        ProgressDialog progDailog = new ProgressDialog(ViewEventActivityUser.this);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            System.out.println("Loading page...");
            progDailog.setMessage("Loading...");
            progDailog.setIndeterminate(false);
            progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDailog.setCancelable(true);
            progDailog.show();

        }

        @Override
        protected Object doInBackground(Object[] objects) {

            // Tag used to cancel the request
            String tag_string_req = "req_getallevents";
            eventList.clear();;

            StringRequest strReq = new StringRequest(Request.Method.POST,
                    AppConfig.URL_GETALLEVENTSUSER, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {


                    try {
                        JSONObject jObj = new JSONObject(response);
                        //boolean error = jObj.getBoolean("error");
                        boolean error = false;
                        // Check for error node in json
                        if (!error) {

                            JSONArray spi = jObj.getJSONArray("details");

                            for (int i = 0; i < spi.length(); i++) {

                                JSONObject c = spi.getJSONObject(i);

                                String uuid = c.getString("uuid");
                                String eventTitle = c.getString("name");
                                String eventDescription = c.getString("description");
                                String eventLocation = c.getString("location");
                                String eventLat = c.getString("latitude");
                                String eventLon = c.getString("longitude");
                                String eventDate = c.getString("date");
                                String eventTime = c.getString("time");

                                System.out.println(uuid + eventTitle + eventDescription + eventLocation + eventDate);

                                Date date = null;
                                DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                                try {
                                    date = formatter.parse(eventDate);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                Calendar cal = Calendar.getInstance();
                                cal.add(Calendar.DATE, -1);
                                Date yesterday = cal.getTime();

                                System.out.println(eventTitle + date + yesterday);

                                if(date.toInstant().isAfter(yesterday.toInstant())) {

                                    eventList.add(new Event(uuid, eventTitle, eventDescription, eventLocation, eventLat, eventLon, eventDate, eventTime));

                                }
                            }
                            recyclerView = findViewById(R.id.plannedRecycler);
                            System.out.println("recyclerView instantiated.");
                            RecyclerView.LayoutManager iLayoutManager = new LinearLayoutManager(getApplicationContext());
                            recyclerView.setLayoutManager(iLayoutManager);
                            recyclerView.setItemAnimator(new DefaultItemAnimator());
                            recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.VERTICAL));
                            System.out.println("recyclerView prepared");
                            eAdapter = new EventsAdapter(eventList);
                            System.out.println("Adapter set");
                            recyclerView.setAdapter(eAdapter);
                            System.out.println("recyclerView loaded.");
                            System.out.println(eAdapter.getItemCount());
                        } else {
                            // Error in login. Get the error message
                            String errorMsg = jObj.getString("error_msg");
                            Toast.makeText(getApplicationContext(),
                                    errorMsg, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        // JSON error
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Login Error: " + error.getMessage());
                    Toast.makeText(getApplicationContext(),
                            error.getMessage(), Toast.LENGTH_LONG).show();
                }
            }){

                @Override
                protected Map<String, String> getParams() {
                    // Posting parameters to url
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("uuidUser", user.get("uid"));

                    return params;
                }

            };

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

            System.out.println(eventList.size());
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            progDailog.dismiss();
        }
    }

    private class getEventsByDate extends AsyncTask {

        ProgressDialog progDailog = new ProgressDialog(ViewEventActivityUser.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            System.out.println("Loading page...");
            progDailog.setMessage("Loading...");
            progDailog.setIndeterminate(false);
            progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDailog.setCancelable(true);
            progDailog.show();

        }

        @Override
        protected Object doInBackground(Object[] objects) {

            // Tag used to cancel the request
            String tag_string_req = "req_getallevents";
            eventList.clear();

            StringRequest strReq = new StringRequest(Request.Method.POST,
                    AppConfig.URL_SEARCHDATEUSER, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {


                    try {
                        JSONObject jObj = new JSONObject(response);
                        // boolean error = jObj.getBoolean("error");
                        boolean error = false;
                        // Check for error node in json
                        if (!error) {

                            JSONArray spi = jObj.getJSONArray("details");

                            for (int i = 0; i < spi.length(); i++) {

                                JSONObject c = spi.getJSONObject(i);

                                String uuid = c.getString("uuid");
                                String eventTitle = c.getString("name");
                                String eventDescription = c.getString("description");
                                String eventLocation = c.getString("location");
                                String eventLat = c.getString("latitude");
                                String eventLon = c.getString("longitude");
                                String eventDate = c.getString("date");
                                String eventTime = c.getString("time");

                                System.out.println(eventTitle + eventDate);

                                eventList.add(new Event(uuid, eventTitle, eventDescription, eventLocation, eventLat, eventLon, eventDate, eventTime));


                            }
                            recyclerView = findViewById(R.id.plannedRecycler);
                            System.out.println("recyclerView instantiated.");
                            RecyclerView.LayoutManager iLayoutManager = new LinearLayoutManager(getApplicationContext());
                            recyclerView.setLayoutManager(iLayoutManager);
                            recyclerView.setItemAnimator(new DefaultItemAnimator());
                            recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.VERTICAL));
                            System.out.println("recyclerView prepared");
                            eAdapter = new EventsAdapter(eventList);
                            System.out.println("Adapter set");
                            recyclerView.setAdapter(eAdapter);
                            System.out.println("recyclerView loaded.");
                            System.out.println(eAdapter.getItemCount());
                        } else {
                            // Error in login. Get the error message
                            String errorMsg = jObj.getString("error_msg");
                            Toast.makeText(getApplicationContext(),
                                    errorMsg, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        // JSON error
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Login Error: " + error.getMessage());
                    Toast.makeText(getApplicationContext(),
                            error.getMessage(), Toast.LENGTH_LONG).show();
                }
            }){

                @Override
                protected Map<String, String> getParams() {
                    // Posting parameters to url
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("date", pickedDate);
                    params.put("uuidUser", user.get("uid"));

                    return params;
                }

            };

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

            System.out.println(eventList.size());
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            progDailog.dismiss();
        }
    }
    private class getEventsByTitle extends AsyncTask {

        ProgressDialog progDailog = new ProgressDialog(ViewEventActivityUser.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            System.out.println("Loading page...");
            progDailog.setMessage("Loading...");
            progDailog.setIndeterminate(false);
            progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDailog.setCancelable(true);
            progDailog.show();

        }

        @Override
        protected Object doInBackground(Object[] objects) {

            // Tag used to cancel the request
            String tag_string_req = "req_getallevents";
            eventList.clear();

            StringRequest strReq = new StringRequest(Request.Method.POST,
                    AppConfig.URL_SEARCHTITLEUSER, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {


                    try {
                        JSONObject jObj = new JSONObject(response);
                        // boolean error = jObj.getBoolean("error");
                        boolean error = false;
                        // Check for error node in json
                        if (!error) {

                            JSONArray spi = jObj.getJSONArray("details");

                            for (int i = 0; i < spi.length(); i++) {

                                JSONObject c = spi.getJSONObject(i);

                                String uuid = c.getString("uuid");
                                String eventTitle = c.getString("name");
                                String eventDescription = c.getString("description");
                                String eventLocation = c.getString("location");
                                String eventLat = c.getString("latitude");
                                String eventLon = c.getString("longitude");
                                String eventDate = c.getString("date");
                                String eventTime = c.getString("time");

                                Date date = null;
                                DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                                try {
                                    date = formatter.parse(eventDate);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                Calendar cal = Calendar.getInstance();
                                cal.add(Calendar.DATE, -1);
                                Date yesterday = cal.getTime();

                                System.out.println(eventTitle + date + yesterday);

                                if(date.toInstant().isAfter(yesterday.toInstant())) {

                                    eventList.add(new Event(uuid, eventTitle, eventDescription, eventLocation, eventLat, eventLon, eventDate, eventTime));

                                }

                            }
                            recyclerView = findViewById(R.id.plannedRecycler);
                            System.out.println("recyclerView instantiated.");
                            RecyclerView.LayoutManager iLayoutManager = new LinearLayoutManager(getApplicationContext());
                            recyclerView.setLayoutManager(iLayoutManager);
                            recyclerView.setItemAnimator(new DefaultItemAnimator());
                            recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.VERTICAL));
                            System.out.println("recyclerView prepared");
                            eAdapter = new EventsAdapter(eventList);
                            System.out.println("Adapter set");
                            recyclerView.setAdapter(eAdapter);
                            System.out.println("recyclerView loaded.");
                            System.out.println(eAdapter.getItemCount());
                        } else {
                            // Error in login. Get the error message
                            String errorMsg = jObj.getString("error_msg");
                            Toast.makeText(getApplicationContext(),
                                    errorMsg, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        // JSON error
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Login Error: " + error.getMessage());
                    Toast.makeText(getApplicationContext(),
                            error.getMessage(), Toast.LENGTH_LONG).show();
                }
            }){

                @Override
                protected Map<String, String> getParams() {
                    // Posting parameters to url
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("title", m_Text);
                    params.put("uuidUser", user.get("uid"));

                    return params;
                }

            };

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

            System.out.println(eventList.size());
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            progDailog.dismiss();
        }
    }
}
