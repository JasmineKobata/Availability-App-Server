package com.myavailabilityapp.server;

import com.sun.net.httpserver.HttpServer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Server {

    public static void main(String[] args) {
        //MariaDB connection
        Database db = Database.getInstance();

        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        HttpServer server = null;
        try {
            Properties prop = new Properties();
            String configFilePath = "src/main/resources/properties2.config";
            FileInputStream propsInput = new FileInputStream(configFilePath);
            prop.load(propsInput);
            server = HttpServer.create(new InetSocketAddress(prop.getProperty("SERVER_IP")
                    , Integer.parseInt(prop.getProperty("SERVER_PORT"))), 0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        server.createContext("/client", HandlerFactory.handleClient(db));
        server.createContext("/group", HandlerFactory.handleGroup(db));
        server.createContext("/event", HandlerFactory.handleEvent(db));
        server.createContext("/availability", HandlerFactory.handleAvailability(db));
        server.setExecutor(threadPoolExecutor);
        server.start();

    }
}
