package eu.pendual.gcuevents.containers;

public class Event {
    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    public String getEventLat() {
        return eventLat;
    }

    public void setEventLat(String eventLat) {
        this.eventLat = eventLat;
    }

    public String getEventLon() {
        return eventLon;
    }

    public void setEventLon(String eventLon) {
        this.eventLon = eventLon;
    }

    public String getPickedDate() {
        return pickedDate;
    }

    public void setPickedDate(String pickedDate) {
        this.pickedDate = pickedDate;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    private String uuid;
    private String eventTitle;
    private String eventDescription;
    private String eventLocation;
    private String eventLat;
    private String eventLon;
    private String pickedDate;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    private String eventTime;

    public Event(String uuid, String eventTitle, String eventDescription, String eventLocation, String eventLat, String eventLon, String pickedDate, String eventTime) {
        this.uuid = uuid;
        this.eventTitle = eventTitle;
        this.eventDescription = eventDescription;
        this.eventLocation  = eventLocation;
        this.eventLat = eventLat;
        this.eventLon = eventLon;
        this.pickedDate= pickedDate;
        this.eventTime= eventTime;
    }

    public Event(String eventTitle, String eventDescription, String eventLocation, String eventLat, String eventLon, String pickedDate, String eventTime) {
        this.eventTitle = eventTitle;
        this.eventDescription = eventDescription;
        this.eventLocation  = eventLocation;
        this.eventLat = eventLat;
        this.eventLon = eventLon;
        this.pickedDate= pickedDate;
        this.eventTime= eventTime;
    }
}
