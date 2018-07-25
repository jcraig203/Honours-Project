package eu.pendual.gcuevents.activity;

/**
 * Created by James on 28/03/2018.
 */

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.util.ArrayList;


/**
 * Created by James Craig S1428641
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import eu.pendual.gcuevents.R;
import eu.pendual.gcuevents.containers.Event;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.MyViewHolder> {

    private ArrayList<Event> eventList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, datetime;
        public RelativeLayout relativeLayout;
        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            datetime = (TextView) view.findViewById(R.id.datetime);
            // description = (TextView) view.findViewById(R.id.description);
            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.incidentLayout);
            // location = (TextView) view.findViewById(R.id.location);
        }
    }


    public EventsAdapter(ArrayList<Event> eventList) {
        this.eventList = eventList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_list_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final Event event = eventList.get(position);
        holder.title.setText(event.getEventTitle());
        holder.datetime.setText(event.getPickedDate() + ", " + event.getEventTime());
        // holder.description.setText(incidents.getDescription());

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println(event.getEventTitle());

                String[] incidentStrings = new String[8];
                incidentStrings[0] = event.getEventTitle();
                incidentStrings[1] = event.getEventDescription();
                incidentStrings[2] = event.getEventLocation();
                incidentStrings[3] = event.getPickedDate();
                incidentStrings[4] = event.getEventTime();
                incidentStrings[5] = event.getUuid();
                incidentStrings[6] = event.getEventLat();
                incidentStrings[7] = event.getEventLon();

                Intent intent = new Intent(view.getContext(), DisplayInfoActivity.class);
                Bundle bundle = new Bundle();
                intent.putExtra("incidentStringArray", incidentStrings);
                intent.putExtras(bundle);
                view.getContext().startActivity(intent);
            }
        });
        // holder.location.setText(incidents.getLocation());
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }
}

