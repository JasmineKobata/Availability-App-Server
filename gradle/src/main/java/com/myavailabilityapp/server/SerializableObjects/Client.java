package com.myavailabilityapp.server.SerializableObjects;

import com.myavailabilityapp.server.Database;
import com.myavailabilityapp.server.ErrorUtils;
import com.myavailabilityapp.server.JsonFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Random;

public class Client {
    int clientId;
    String clientName;
    HashSet<Integer> groupIds;

    public Client() {
        clientName = new String();
        groupIds = new HashSet<Integer>();
    }

    public Client(Database db, String clientName) {
        this.clientId = generateClientId(db);
        this.clientName = clientName;
        this.groupIds = new HashSet<Integer>();
    }

    public Client(Statement stmt, int clientId) throws SQLException {
        this.groupIds = new HashSet<Integer>();
        String sql = "SELECT clientName FROM client WHERE clientId = '" + clientId + "';";
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            this.clientId = clientId;
            this.clientName = rs.getString("clientName");

            sql = "SELECT groupId FROM groupToFromClient WHERE clientId = '" + clientId + "';";
            ResultSet rs2 = stmt.executeQuery(sql);
            while (rs2.next()) {
                groupIds.add(rs2.getInt("groupId"));
            }
            System.out.println("Client query executed");
        }
    }

    public static Client getMinimalClient(Statement stmt, int clientId) throws SQLException {
        Client client = new Client();
        client.clientId = clientId;
        client.clientName = Client.getClientName(stmt, clientId);
        return client;
    }

    public void insertClient(Statement stmt) throws SQLException {
        String sql = "INSERT INTO client "
                + "(clientId, clientName) "
                + "VALUES('" + clientId
                + "', '" + clientName+ "');";
        stmt.executeUpdate(sql);
        System.out.println("client inserted");
    }

    public static void leaveGroup(Statement stmt, int clientId, int groupId) throws SQLException {
        String sql = "DELETE FROM groupToFromClient WHERE groupId = '" + groupId
                + "' AND clientId = '" + clientId + "';";
        //add if no people left in group, delete group
        stmt.executeUpdate(sql);
        sql = "SELECT eventId FROM event WHERE groupId = '" + groupId + "';";
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            sql = "DELETE FROM availability WHERE eventId = '" + rs.getInt("eventId")
                    + "' AND clientId = '" + clientId + "';";
            stmt.executeUpdate(sql);
        }
        System.out.println("Group left");
    }

    public void addGroupId(int id) {
        groupIds.add(id);
    }

    public void removeGroupId(int id) {
        groupIds.remove(id);
    }

    public static void changeClientName(Statement stmt, int clientId, String clientName) throws SQLException {
        String sql = "UPDATE client "
                + "SET clientName = '" + clientName
                + "' WHERE clientId = '" + clientId + "';";
        stmt.executeUpdate(sql);
        System.out.println("client name updated");
    }

    public int getClientId() {
        return clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public static String getClientName(Statement stmt, int clientId) throws SQLException {
        String sql = "SELECT clientName FROM client WHERE clientId = '" + clientId + "';";
        ResultSet rs = stmt.executeQuery(sql);
        String name = new String();
        while (rs.next()) {
            name = rs.getString("clientName");
            System.out.println("Client query executed");
        }
        return name;
    }

    public HashSet<Integer> getGroupIds() {
        return groupIds;
    }

    public boolean containsGroupId(int id) {
        return groupIds.contains(id);
    }

    private int generateClientId(Database db) {
        Random r = new Random();
        int id = r.nextInt(99999999);
        //edit later to check all existing group ids, not just local
        while (db.idExists("client", "clientId", id)) {
            id = (id + 1) % 99999999;
        }
        return id;
    }

    public void deleteClient(Statement stmt) throws SQLException {
        String sql = "SELECT groupId FROM groupToFromClient WHERE clientId = '" + clientId + "';";
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            int groupId = rs.getInt("groupId");
            sql = "DELETE FROM groupToFromClient WHERE clientId = '" + clientId + "' AND groupId = '" + groupId + "';";
            stmt.executeQuery(sql);
            Group group = new Group(stmt, groupId);

            if (group.getClientIds().isEmpty()) {
                group.deleteGroup(stmt);
            }
        }
        sql = "DELETE FROM client WHERE clientId = '" + clientId + "';";
        stmt.executeQuery(sql);
        sql = "DELETE FROM availability WHERE clientId = '" + clientId + "';";
        stmt.executeQuery(sql);
    }
}
