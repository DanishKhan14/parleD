package com.example.gkhandel.parlegtest.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * the task object that contains the task details
 */
public class Task {
    private static final int ID_LENGTH = 40;
    private static final String NAME_KEY = "name";
    private static final String ID_KEY = "taskId";
    private static final String DESC_KEY = "desc";
    private static final String LONG_KEY = "longitude";
    private static final String LAT_KEY = "latitude";
    private static final String RAD_KEY = "radius";
    private static final String ST_TM_KEY = "startTime";
    private static final String END_TM_KEY = "endTime";
    private static final String PR_KEY = "priority";
    private static final String ST_KEY = "status";
    public static final String ST_CANCELED = "CANCELED";
    public static final String ST_NOT_DONE = "NOT YET DONE";
    public static final String ST_PROGRESS =  "IN PROGRESS";
    public static final String ST_DONE = "DONE";

    private final String taskId;
    private final String name;
    private final String desc;
    private final Double longitude;
    private final Double latitude;
    private final Double radius;
    private final Date startTime; // in mm-dd-yyyy format
    private final Date endTime; // in mm-dd-yyyy format
    private final Integer priority;
    private String status;
    private Boolean notified = false;

    public void setNotified(Boolean notified) {
        this.notified = notified;
    }

    public Boolean getNotified() {
        return notified;
    }

    public Task(String name, String desc, Double longitude, Double latitude, Double radius, Date startTime, Date endTime, Integer priority, String status) throws IllegalArgumentException{
        this.taskId = genId();
        this.name = name;
        this.desc = desc;
        this.longitude = longitude;
        this.latitude = latitude;
        this.radius = radius;
        this.startTime = startTime;
        this.endTime = endTime;
        this.priority = priority;
        if(!status.equals(ST_CANCELED) && !status.equals(ST_NOT_DONE) && !status.equals(ST_PROGRESS) && !status.equals(ST_DONE)) {
            throw new IllegalArgumentException("Illegal status type");
        }
        this.status = status;
    }

    /**
     * @return a randomly generated string of length ID_LENGTH
     */
    private String genId() {
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < ID_LENGTH; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * @return the taskId
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the description
     */
    public String getDesc() {
        return desc;
    }

    /**
     * @return the longitude
     */
    public Double getLongitude() {
        return longitude;
    }

    /**
     * @return the latitude
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * @return the radius
     */
    public Double getRadius() {
        return radius;
    }

    /**
     * @return the starTime
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * @return the endTime
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * @return the priority
     */
    public Integer getPriority() {
        return priority;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @return the json object
     * @throws JSONException on conversion error
     */
    public JSONObject getJSONObject() throws JSONException {

        JSONObject jsonObject = new JSONObject();

        jsonObject.put(ID_KEY, taskId);
        jsonObject.put(NAME_KEY, name);
        jsonObject.put(DESC_KEY, desc);
        jsonObject.put(LONG_KEY, longitude);
        jsonObject.put(LAT_KEY, latitude);
        jsonObject.put(RAD_KEY, radius);
        jsonObject.put(ST_TM_KEY, startTime);
        jsonObject.put(END_TM_KEY, endTime);
        jsonObject.put(PR_KEY, priority);
        jsonObject.put(ST_KEY, status);

        return jsonObject;
    }

    /**
     * @param jsonObject the object in json format
     * @throws JSONException on conversion error from json
     * @throws ParseException on conversion error from date
     */
    public Task(JSONObject jsonObject) throws JSONException, ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("mm-dd-yyyy");
        taskId = jsonObject.get(ID_KEY).toString();
        name = jsonObject.get(NAME_KEY).toString();
        desc  = jsonObject.get(DESC_KEY).toString();
        longitude =  Double.parseDouble(jsonObject.get(LONG_KEY).toString());
        latitude = Double.parseDouble(jsonObject.get(LAT_KEY).toString());
        radius = Double.parseDouble(jsonObject.get(RAD_KEY).toString());
        //startTime = formatter.parse(jsonObject.get(ST_TM_KEY).toString());
        //endTime = formatter.parse(jsonObject.get(END_TM_KEY).toString());
        startTime = new Date();
        endTime = new Date();
        priority = Integer.parseInt(jsonObject.get(PR_KEY).toString());
        status = jsonObject.get(ST_KEY).toString();
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Task)) {
            return false;
        }
        if (this == other) {
            return true;
        }

        Task otherTask = (Task) other;
        return taskId.equals(otherTask.taskId);
    }
}
