package com.myavailabilityapp.server.SerializableObjects;

import com.myavailabilityapp.server.Database;
import com.myavailabilityapp.server.ErrorUtils;
import com.myavailabilityapp.server.Vector2f;

import javax.xml.crypto.Data;
import java.awt.*;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class Event implements Serializable {
    int groupId;
    int eventId;
    String eventName;
    Vector2f<String> dateRange;

    public Event() {
        eventName = new String();
        dateRange = new Vector2f<String>();
    }

    public Event(Database db, String occasionName, int groupId, Vector2f<String> dateRange) {
        eventId = generateOccasionId(db);
        this.eventName = occasionName;
        this.groupId = groupId;
        this.dateRange = dateRange;
    }

    public Event(Statement stmt, int eventId, int groupId) throws SQLException {
        dateRange = new Vector2f<String>();
        String sql = "SELECT eventName, startDate, endDate FROM event WHERE eventId = '" + eventId
                + "' AND groupId = '" + groupId + "';";
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            this.groupId = groupId;
            this.eventId = eventId;
            eventName = rs.getString("eventName");

            dateRange.first = rs.getString("startDate");
            dateRange.second = rs.getString("endDate");
            System.out.println("Event query executed");
        }
    }

    public void deleteEvent(Statement stmt) throws SQLException {
        AvailabilityData.deleteAvailability(stmt, eventId);
        String sql = "DELETE FROM event WHERE eventId = '" + eventId
                + "' AND groupId = '" + groupId + "';";
        stmt.executeQuery(sql);
        System.out.println("Group deleted");
    }

    public void insertEvent(Statement stmt) throws SQLException {
        String sql = "INSERT INTO event "
                + "(eventId, groupId, eventName, startDate, endDate) "
                + "VALUES('" + eventId
                + "', '" + groupId
                + "', '" + eventName
                + "', '" + dateRange.first
                + "', '" + dateRange.second + "');";
        stmt.executeUpdate(sql);
        System.out.println("client inserted");
    }

    public int generateOccasionId(Database db) {
        Random r = new Random();
        int id = r.nextInt(99999999);

        while (db.idExists("event", "eventId", id)) {
            id = (id + 1) % 99999999;
        }
        return id;
    }

    public int getEventId() {
        return eventId;
    }

    public int getGroupId() {
        return groupId;
    }

    public String getEventName() {
        return eventName;
    }

    public Vector2f<String> getDateRange() {
        return dateRange;
    }

    public static void changeEventName(Statement stmt, int eventId, String eventName) throws SQLException {
        String sql = "UPDATE event "
                + "SET eventName = '" + eventName
                + "' WHERE eventId = '" + eventId + "';";
        stmt.executeUpdate(sql);
        System.out.println("Event name updated");
    }

    public static void editDateRange(Statement stmt, int eventId, int groupId, Vector2f newDateRange) throws SQLException {
        String sql = "UPDATE event "
                + "SET startDate = '" + newDateRange.first
                + "', endDate = '" + newDateRange.second
                + "' WHERE eventId = '" + eventId
                + "' AND groupId = '" + groupId + "';";
        stmt.executeUpdate(sql);
        System.out.println("Date Range updated");
    }
}
