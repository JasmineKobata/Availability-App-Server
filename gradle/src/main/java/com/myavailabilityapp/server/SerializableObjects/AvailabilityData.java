package com.myavailabilityapp.server.SerializableObjects;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;


public class AvailabilityData implements Serializable {
    public int clientId;
    public int eventId;
    HashSet<String> checkedDates;

    public AvailabilityData(int clientId, int eventId, HashSet<String> checkedDates) {
        this.clientId = clientId;
        this.eventId = eventId;
        this.checkedDates = new HashSet<String>();
        this.checkedDates = checkedDates;
    }

    public AvailabilityData(Statement stmt, int clientId, int eventId) throws SQLException {
        this.checkedDates = new HashSet<String>();
        String sql = "SELECT checkedDate FROM availability WHERE clientId = '" + clientId + "' AND eventId = '" + eventId + "';";
        ResultSet rs = stmt.executeQuery(sql);
        this.clientId = clientId;
        this.eventId = eventId;

        while (rs.next()) {
            this.checkedDates.add(rs.getString("checkedDate"));
        }
        System.out.println("Availability query executed");
    }

    public void deleteAvailability(Statement stmt) throws SQLException {
        String sql = "DELETE FROM availability WHERE eventId = '" + eventId + "';";
        stmt.executeQuery(sql);

        System.out.println("Availability deleted");
    }

    public static void deleteAvailability(Statement stmt, int id) throws SQLException {
        String sql = "DELETE FROM availability WHERE eventId = '" + id + "';";
        stmt.executeQuery(sql);

        System.out.println("Availability deleted");
    }

    public void updateAvailabilityData(String date) {
        if (!checkedDates.contains(date)) {
            checkedDates.add(date);
        }
        else {
            checkedDates.remove(date);
        }
    }

    public void updateAvailabilityDB(Statement stmt) throws SQLException {
        AvailabilityData existingAvailability = new AvailabilityData(stmt, clientId, eventId);
        for (String date : getCheckedDates()) {
            if (!existingAvailability.containsDate(date)) {
                String sql = "INSERT INTO availability"
                        + "(clientId, eventId, checkedDate) "
                        + "VALUES('" + clientId
                        + "', '" + eventId
                        + "', '" + date + "');";
                stmt.executeUpdate(sql);
                System.out.println("Event inserted");
            }
        }

        for (String date : existingAvailability.getCheckedDates()) {
            if (!containsDate(date)) {
                String sql = "DELETE FROM availability WHERE clientId = '" + clientId
                        + "' AND eventId = '" + eventId + "' AND checkedDate = '" + date + "';";
                stmt.executeQuery(sql);
            }
        }
    }

    public boolean containsDate(String date) {
        return checkedDates.contains(date);
    }

    public HashSet<String> getCheckedDates() {
        return checkedDates;
    }

    public static boolean availabilityExists(Statement stmt, int clientId, int eventId, String date) {
        String sql = "SELECT checkedDate FROM availability WHERE clientId = '" + clientId
                + "' AND eventId = '" + eventId + "' AND checkedDate = '" + date + "';";

        try {
            ResultSet rs = stmt.executeQuery(sql);
            System.out.println("Availability query executed");
            return rs.absolute(1);
        }
        catch (SQLException sq) {
            sq.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
