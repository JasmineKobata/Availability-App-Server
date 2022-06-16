package com.myavailabilityapp.server;

import com.myavailabilityapp.server.SerializableObjects.Client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class Database {
    private static Database instance;
    private Statement stmt;
    long lastStmtTime;

    private Database(Statement stmt) {
        this.stmt = stmt;
    }

    public Statement getStatement() {
        if (System.currentTimeMillis() > lastStmtTime + 1000*60*60) {
            if (stmt != null) {
                try {
                    stmt.getConnection().close();
                } catch (SQLException sq) {
                    sq.printStackTrace();
                }
            }
            Properties prop = new Properties();
            try {
                String configFilePath = "src/main/resources/properties.config";
                FileInputStream propsInput = new FileInputStream(configFilePath);
                prop.load(propsInput);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException ie) {
                ie.printStackTrace();
            }
            String host = prop.getProperty("DB_HOST");
            String port = prop.getProperty("DB_PORT");
            String user = prop.getProperty("DB_USER");
            String pass = prop.getProperty("DB_PASSWORD");
            String endpoint = prop.getProperty("DB_ENDPOINT");
            String url = "jdbc:mariadb://" + host + ":" + port + endpoint;

            try {
                Class.forName("org.mariadb.jdbc.Driver");
                Connection conn = DriverManager.getConnection(url, user, pass);
                System.out.println("Connected database successfully...");
                stmt = conn.createStatement();
            } catch (Exception e) {
                e.printStackTrace();
            }
            lastStmtTime = System.currentTimeMillis();
        }

        return stmt;
    }

    public static Database getInstance() {
        if (instance == null) {
            instance = new Database(createDB());
        }
        return instance;
    }

    public static Statement createDB() {
        Properties prop = new Properties();
        try {
            String configFilePath = "src/main/resources/properties.config";
            FileInputStream propsInput = new FileInputStream(configFilePath);
            prop.load(propsInput);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
        String host = prop.getProperty("DB_HOST");
        String port = prop.getProperty("DB_PORT");
        String user = prop.getProperty("DB_USER");
        String pass = prop.getProperty("DB_PASSWORD");
        String endpoint = prop.getProperty("DB_ENDPOINT");
        String url = "jdbc:mariadb://" + host + ":" + port + endpoint;

        Statement stmt = null;
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, user, pass);
            System.out.println("Connected database successfully...");

            stmt = conn.createStatement();

            String sql = "CREATE TABLE IF NOT EXISTS client ("
                    + "clientId INT PRIMARY KEY,"
                    + "clientName VARCHAR(50));";
            stmt.executeUpdate(sql);
            System.out.println("Created client table in given database...");

            sql = "CREATE TABLE IF NOT EXISTS groups ("
                    + "groupId INT PRIMARY KEY,"
                    + "groupName VARCHAR(50) );";
            stmt.executeUpdate(sql);
            System.out.println("Created group table in given database...");

            sql = "CREATE TABLE IF NOT EXISTS groupToFromClient ("
                    + "groupId INT,"
                    + "clientId INT);";
            stmt.executeUpdate(sql);
            System.out.println("Created groupToFromClient table in given database...");

            sql = "CREATE TABLE IF NOT EXISTS event ("
                    + "eventId INT PRIMARY KEY,"
                    + "groupId INT,"
                    + "eventName VARCHAR(50),"
                    + "startDate CHAR(9),"
                    + "endDate CHAR(9));";
            stmt.executeUpdate(sql);
            System.out.println("Created event table in given database...");

            sql = "CREATE TABLE IF NOT EXISTS availability ("
                    + "clientId INT,"
                    + "eventId INT,"
                    + "checkedDate VARCHAR(15) );";
            stmt.executeUpdate(sql);
            System.out.println("Created availability table in given database...");
        }
        catch (SQLException sq) {
            System.out.println("1 com.myavailabilityapp.server.Database connection failed...");
            sq.printStackTrace();
        }
        catch (Exception e) {
            System.out.println("2 com.myavailabilityapp.server.Database connection failed...");
            e.printStackTrace();
        }
        return stmt;
    }

    public boolean idExists(String tableField, String classIdField, int id) {
        String sql = "SELECT " + classIdField + " FROM " + tableField
                + " WHERE " + classIdField + " = '" + id + "';";

        try {
            ResultSet rs = stmt.executeQuery(sql);
            System.out.println(tableField + " query executed");
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
