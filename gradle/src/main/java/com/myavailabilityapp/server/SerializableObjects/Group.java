package com.myavailabilityapp.server.SerializableObjects;

import com.myavailabilityapp.server.Database;
import com.myavailabilityapp.server.JsonFactory;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class Group implements Serializable {
    int groupId;
    String groupName;
    HashSet<Integer> clientIds;
    HashSet<Integer> eventIds;

    public Group() {
        groupName = new String();
        clientIds = new HashSet<Integer>();
        eventIds = new HashSet<Integer>();
    }

    //Create group & insert into client
    public Group(Database db, String groupName, Client client) {
        this.groupId = generateGroupId(db);
        client.addGroupId(this.groupId);
        this.groupName = groupName;
        clientIds = new HashSet<Integer>();
        clientIds.add(client.getClientId());
        eventIds = new HashSet<Integer>();
    }

    //Create group without inserting into client
    public Group(Database db, String groupName, int clientId) {
        this.groupId = generateGroupId(db);
        this.groupName = groupName;
        clientIds = new HashSet<Integer>();
        clientIds.add(clientId);
        eventIds = new HashSet<Integer>();
    }

    public Group(Statement stmt, int groupId) {
        clientIds = new HashSet<Integer>();
        eventIds = new HashSet<Integer>();
        try {
            String sql = "SELECT groupName FROM groups WHERE groupId = '" + groupId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                this.groupId = groupId;
                groupName = rs.getString("groupName");

                sql = "SELECT eventId FROM event WHERE groupId = '" + groupId + "';";
                ResultSet rs2 = stmt.executeQuery(sql);
                while (rs2.next()) {
                    eventIds.add(rs2.getInt("eventId"));
                }
                sql = "SELECT clientId FROM groupToFromClient WHERE groupId = '" + groupId + "';";
                ResultSet rs3 = stmt.executeQuery(sql);
                while (rs3.next()) {
                    clientIds.add(rs3.getInt("clientId"));
                }
                System.out.println("Group query executed");
            }
        }
        catch (SQLException sq) {
            sq.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteGroup(Statement stmt) throws SQLException {
        String sql = "SELECT eventId FROM event WHERE groupId = '" + groupId + "';";
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            AvailabilityData.deleteAvailability(stmt, rs.getInt("eventId"));
        }

        sql = "DELETE FROM groups WHERE groupId = '" + groupId + "';";
        stmt.executeQuery(sql);
        sql = "DELETE FROM groupToFromClient WHERE groupId = '" + groupId + "';";
        stmt.executeQuery(sql);
        sql = "DELETE FROM event WHERE groupId = '" + groupId + "';";
        stmt.executeQuery(sql);

        System.out.println("Group deleted");
    }

    public void insertGroup(Statement stmt) throws SQLException {
        String sql = "INSERT INTO groups"
                + "(groupId, groupName) "
                + "VALUES('" + groupId
                + "', '" + groupName + "');";
        stmt.executeUpdate(sql);
        System.out.println("group inserted");

        for (int id : clientIds) {
            insertToFromGroup(stmt, id);
        }
    }

    public void insertToFromGroup(Statement stmt, int id) throws SQLException {
        String sql = "INSERT INTO groupToFromClient "
                + "(groupId, clientId) "
                + "VALUES('" + groupId
                + "', '" + id + "');";
        stmt.executeUpdate(sql);
        System.out.println("group <-> client inserted");
    }

    public void removeEventId(int id) {
        eventIds.remove(id);
    }

    public void removeClientId(int id) { clientIds.remove(id); }

    public String getGroupName() {
        return groupName;
    }

    public static void changeGroupName(Statement stmt, int groupId, String groupName) throws SQLException {
        String sql = "UPDATE groups "
                + "SET groupName = '" + groupName
                + "' WHERE groupId = '" + groupId + "';";
        stmt.executeUpdate(sql);

        System.out.println("Group name updated");
    }

    public int getGroupId() {
        return groupId;
    }

    public int generateGroupId(Database db) {
        Random r = new Random();
        int id = r.nextInt(99999999);

        while (db.idExists("groups", "groupId", id)) {
            id = (id + 1) % 99999999;
        }
        return id;
    }

    public HashSet<Integer> getClientIds() {
        return clientIds;
    }

    public HashSet<Integer> getEventIds() {
        return eventIds;
    }

    public boolean containsEventId(int id) {
        return eventIds.contains(id);
    }

    public void addClientId(int id) {
        clientIds.add(id);
    }

    public void addEventId(int id) {
        eventIds.add(id);
    }
}
