package eu.pendual.gcuevents.activity;

import eu.pendual.gcuevents.R;
import eu.pendual.gcuevents.helper.SQLiteHandler;
import eu.pendual.gcuevents.helper.SessionManager;

import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView txtName;
    private TextView txtEmail;
    private Button btnLogout;
    private Button btnNewEvent;
    private Button btnViewEvents;
    private Button btnViewEventsUser;

    private SQLiteHandler db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtName = (TextView) findViewById(R.id.name);
        txtEmail = (TextView) findViewById(R.id.email);
        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnNewEvent = (Button) findViewById(R.id.btnCreateEvent);
        btnViewEvents = (Button) findViewById(R.id.btnViewEvents);
        btnViewEventsUser = (Button) findViewById(R.id.btnViewEventsUser);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();

        String name = user.get("name");
        String email = user.get("email");

        // Displaying the user details on the screen
        txtName.setText(name);
        txtEmail.setText(email);

        // Logout button click event
        btnLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        // Button to create a new event
        btnNewEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NewEventActivity.class);
                startActivity(intent);
            }
        });

        //Button to view events
        btnViewEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ViewEventActivity.class);
                startActivity(intent);
            }
        });

        btnViewEventsUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ViewEventActivityUser.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
