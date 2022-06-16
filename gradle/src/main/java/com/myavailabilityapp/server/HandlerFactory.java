package com.myavailabilityapp.server;

import com.myavailabilityapp.server.SerializableObjects.AvailabilityData;
import com.myavailabilityapp.server.SerializableObjects.Client;

import com.myavailabilityapp.server.SerializableObjects.Event;
import com.myavailabilityapp.server.SerializableObjects.Group;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.text.StringEscapeUtils;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;

public class HandlerFactory {
    public static HttpHandler handleClient(Database db) {
        return httpExchange -> {
            try {f
                if (pathToList(httpExchange).length < 3) {
                    sendResponse(httpExchange, 500, ErrorUtils.pathErrString);
                    return;
                }

                switch (pathToList(httpExchange)[2]) {
                    case "getMin": {
                        int clientId = Integer.valueOf(getParameters(httpExchange).get("clientId"));
                        Client client = Client.getMinimalClient(db.getStatement(), clientId);
                        String json = JsonFactory.classToJson(client);
                        sendResponse(httpExchange, 200, json);
                        break;  }
                    case "create": {
                        String clientName = getParameters(httpExchange).get("clientName");
                        Client client = new Client(db, clientName);
                        String json = JsonFactory.classToJson(client);
                        client.insertClient(db.getStatement());
                        sendResponse(httpExchange, 200, json);
                        break;  }
                    case "name": {
                        String clientName = getParameters(httpExchange).get("clientName");
                        int clientId = Integer.valueOf(getParameters(httpExchange).get("clientId"));
                        Client.changeClientName(db.getStatement(), clientId, clientName);
                        Client client = new Client(db.getStatement(), clientId);
                        String json = JsonFactory.classToJson(client);
                        sendResponse(httpExchange, 200, json);
                        break;  }
                    case "leave": {
                        int groupId = Integer.valueOf(getParameters(httpExchange).get("groupId"));
                        int clientId = Integer.valueOf(getParameters(httpExchange).get("clientId"));
                        Client.leaveGroup(db.getStatement(), clientId, groupId);
                        Group group = new Group(db.getStatement(), groupId);
                        if (group.getClientIds().isEmpty()) {
                            group.deleteGroup(db.getStatement());
                        }
                        Client client = new Client(db.getStatement(), clientId);
                        String json = JsonFactory.classToJson(client);
                        sendResponse(httpExchange, 200, json);
                        break;  }
                    case "delete": {
                        int clientId = Integer.valueOf(getParameters(httpExchange).get("clientId"));
                        Client client = new Client(db.getStatement(), clientId);
                        client.deleteClient(db.getStatement());
                        sendResponse(httpExchange, 200, clientId + " deleted");
                        break;  }
                    default:
                        sendResponse(httpExchange, 500, "Error: Path '" + pathToStr(httpExchange) + "' not found ");
                        break;
                }
            }
            catch (SQLException sq) {
                sendResponse(httpExchange, 500, ErrorUtils.nameErrString);
            }
            catch (NullPointerException | ArrayIndexOutOfBoundsException np) { //client name is not found
                sendResponse(httpExchange, 500, "Error: Client name not recieved");
            }
            catch (Exception e) {
                sendResponse(httpExchange, 500, e.toString());
            }
        };
    }

    public static HttpHandler handleGroup(Database db) {
        return httpExchange -> {
            try {
                if (pathToList(httpExchange).length < 3) {
                    sendResponse(httpExchange, 500, ErrorUtils.pathErrString);
                    return;
                }

                switch (pathToList(httpExchange)[2]) {
                    case "get": {
                        int groupId = Integer.valueOf(getParameters(httpExchange).get("groupId"));
                        Group group = new Group(db.getStatement(), groupId);
                        String json = JsonFactory.classToJson(group);
                        sendResponse(httpExchange, 200, json);
                        break;
                    }
                    case "create": {
                        String groupName = getParameters(httpExchange).get("groupName");
                        int clientId = Integer.valueOf(getParameters(httpExchange).get("clientId"));
                        Group group = new Group(db, groupName, clientId);
                        String json = JsonFactory.classToJson(group);
                        group.insertGroup(db.getStatement());
                        sendResponse(httpExchange, 200, json);
                        break;
                    }
                    case "join": {
                        int groupId = Integer.valueOf(getParameters(httpExchange).get("groupId"));
                        int clientId = Integer.valueOf(getParameters(httpExchange).get("clientId"));
                        if (db.idExists("groups", "groupId", groupId)) {
                            Group group = new Group(db.getStatement(), groupId);
                            String json = JsonFactory.classToJson(group);
                            group.insertToFromGroup(db.getStatement(), clientId);
                            sendResponse(httpExchange, 200, json);
                        }
                        else {
                            sendResponse(httpExchange, 500, ErrorUtils.idErrString);
                        }
                        break;
                    }
                    case "clients": {
                        int groupId = Integer.valueOf(getParameters(httpExchange).get("groupId"));
                        int clientId = Integer.valueOf(getParameters(httpExchange).get("clientId"));
                        Group group = new Group(db.getStatement(), groupId);
                        List<Client> clientList = new ArrayList<Client>();
                        Client client = Client.getMinimalClient(db.getStatement(), clientId);
                        clientList.add(client);
                        for (int id : group.getClientIds()) {
                            if (id != client.getClientId()) {
                                clientList.add(Client.getMinimalClient(db.getStatement(), id));
                            }
                        }

                        String json = JsonFactory.classToJson(clientList);
                        sendResponse(httpExchange, 200, json);
                        break;  }
                    case "name": {
                        String groupName = getParameters(httpExchange).get("groupName");
                        int groupId = Integer.valueOf(getParameters(httpExchange).get("groupId"));
                        Group.changeGroupName(db.getStatement(), groupId, groupName);
                        Group group = new Group(db.getStatement(), groupId);
                        String json = JsonFactory.classToJson(group);
                        sendResponse(httpExchange, 200, json);
                        break;
                    }
                    case "delete": {
                        int groupId = Integer.valueOf(getParameters(httpExchange).get("groupId"));
                        Group group = new Group(db.getStatement(), groupId);
                        group.deleteGroup(db.getStatement());
                        sendResponse(httpExchange, 200, groupId + " deleted\n");
                        break;
                    }
                    default:
                        sendResponse(httpExchange, 500, "Error: Path '" + pathToStr(httpExchange) + "' not found ");
                        break;
                }
            } catch (SQLException sq) {
                sendResponse(httpExchange, 500, ErrorUtils.nameErrString);
            } catch (NullPointerException | ArrayIndexOutOfBoundsException np) { //client name is not found
                sendResponse(httpExchange, 500, "Error: Group name not recieved");
            } catch (Exception e) {
                sendResponse(httpExchange, 500, e.toString());
            }
        };
    }

    public static HttpHandler handleEvent(Database db) {
        return httpExchange -> {
            try {
                if (pathToList(httpExchange).length < 3) {
                    sendResponse(httpExchange, 500, ErrorUtils.pathErrString);
                    return;
                }

                switch (pathToList(httpExchange)[2]) {
                    case "get": {
                        int groupId = Integer.valueOf(getParameters(httpExchange).get("groupId"));
                        int eventId = Integer.valueOf(getParameters(httpExchange).get("eventId"));
                        Event event = new Event(db.getStatement(), eventId, groupId);
                        String json = JsonFactory.classToJson(event);
                        sendResponse(httpExchange, 200, json);
                        break;
                    }
                    case "create": {
                        String eventName = getParameters(httpExchange).get("eventName");
                        int groupId = Integer.valueOf(getParameters(httpExchange).get("groupId"));
                        Vector2f dateRange = new Vector2f();
                        dateRange.first = getParameters(httpExchange).get("startDate");
                        dateRange.second = getParameters(httpExchange).get("endDate");
                        Event event = new Event(db, eventName, groupId, dateRange);
                        String json = JsonFactory.classToJson(event);
                        event.insertEvent(db.getStatement());
                        sendResponse(httpExchange, 200, json);
                        break;
                    }
                    case "name": {
                        String eventName = getParameters(httpExchange).get("eventName");
                        int eventId = Integer.valueOf(getParameters(httpExchange).get("eventId"));
                        int groupId = Integer.valueOf(getParameters(httpExchange).get("groupId"));
                        Event.changeEventName(db.getStatement(), eventId, eventName);
                        Event event = new Event(db.getStatement(), eventId, groupId);
                        String json = JsonFactory.classToJson(event);
                        sendResponse(httpExchange, 200, json);
                        break;
                    }
                    case "dates": {
                        int eventId = Integer.valueOf(getParameters(httpExchange).get("eventId"));
                        int groupId = Integer.valueOf(getParameters(httpExchange).get("groupId"));
                        Vector2f dateRange = new Vector2f();
                        dateRange.first = getParameters(httpExchange).get("startDate");
                        dateRange.second = getParameters(httpExchange).get("endDate");
                        Event.editDateRange(db.getStatement(), eventId, groupId, dateRange);
                        Event event = new Event(db.getStatement(), eventId, groupId);
                        String json = JsonFactory.classToJson(event);
                        sendResponse(httpExchange, 200, json);
                        break;
                    }
                    case "availability": {
                        int groupId = Integer.valueOf(getParameters(httpExchange).get("groupId"));
                        int eventId = Integer.valueOf(getParameters(httpExchange).get("eventId"));
                        Group group = new Group(db.getStatement(), groupId);
                        HashMap<Integer, AvailabilityData> availList = new HashMap<Integer, AvailabilityData>();
                        for (int id : group.getClientIds()) {
                            AvailabilityData availData = new AvailabilityData(db.getStatement(), id, eventId);
                            availList.put(id, availData);
                        }
                        String json = JsonFactory.classToJson(availList);
                        sendResponse(httpExchange, 200, json);
                        break;  }
                    case "delete": {
                        int eventId = Integer.valueOf(getParameters(httpExchange).get("eventId"));
                        int groupId = Integer.valueOf(getParameters(httpExchange).get("groupId"));
                        Event event = new Event(db.getStatement(), eventId, groupId);
                        event.deleteEvent(db.getStatement());
                        sendResponse(httpExchange, 200, eventId + " deleted");
                        break;
                    }
                    default:
                        sendResponse(httpExchange, 500, "Error: Path '" + pathToStr(httpExchange) + "' not found ");
                        break;
                }
            } catch (SQLException sq) {
                sendResponse(httpExchange, 500, ErrorUtils.nameErrString);
            } catch (NullPointerException | ArrayIndexOutOfBoundsException np) { //client name is not found
                sendResponse(httpExchange, 500, "Error: Event name not recieved");
            } catch (Exception e) {
                sendResponse(httpExchange, 500, e.toString());
            }
        };
    }

    public static HttpHandler handleAvailability(Database db) {
        return httpExchange -> {
            try {
                if (pathToList(httpExchange).length < 3) {
                    sendResponse(httpExchange, 500, ErrorUtils.pathErrString);
                    return;
                }

                switch (pathToList(httpExchange)[2]) {
                    case "get": {
                        int clientId = Integer.valueOf(getParameters(httpExchange).get("clientId"));
                        int eventId = Integer.valueOf(getParameters(httpExchange).get("eventId"));
                        AvailabilityData availData = new AvailabilityData(db.getStatement(), clientId, eventId);
                        String json = JsonFactory.classToJson(availData);
                        sendResponse(httpExchange, 200, json);
                        break;
                    }
                    case "update": {
                        int clientId = Integer.valueOf(getParameters(httpExchange).get("clientId"));
                        int eventId = Integer.valueOf(getParameters(httpExchange).get("eventId"));
                        String[] datesTemp = JsonFactory.classFromJson(
                                getParameters(httpExchange).get("checkedDates")
                                , String[].class);
                        HashSet<String> checkedDates = new HashSet<>(Arrays.asList(datesTemp));
                        AvailabilityData ad = new AvailabilityData(clientId, eventId, checkedDates);
                        ad.updateAvailabilityDB(db.getStatement());
                        sendResponse(httpExchange, 200, "Availability updated");
                        break;
                    }
                    default:
                        sendResponse(httpExchange, 500, "Error: Path '" + pathToStr(httpExchange) + "' not found ");
                        break;
                }
            } catch (SQLException sq) {
                sendResponse(httpExchange, 500, ErrorUtils.nameErrString);
            } catch (NullPointerException | ArrayIndexOutOfBoundsException np) { //client name is not found
                sendResponse(httpExchange, 500, "Error: Availability not recieved");
            } catch (Exception e) {
                sendResponse(httpExchange, 500, ErrorUtils.dbErrString);
            }
        };
    }

    public static void sendResponse(HttpExchange httpExchange, int statusCode, String msg) throws IOException {
        OutputStream outputStream = httpExchange.getResponseBody();

        httpExchange.sendResponseHeaders(statusCode, msg.length());
        outputStream.write(msg.getBytes());
        outputStream.flush();
        outputStream.close();
    }

    public static String streamToString(InputStream inputStream) throws IOException {
        StringBuilder textBuilder = new StringBuilder();
        Reader reader = new BufferedReader(new InputStreamReader(
                inputStream, Charset.forName(StandardCharsets.UTF_8.name())));
        int c = 0;
        while ((c = reader.read()) != -1) {
            textBuilder.append((char) c);
        }
        return textBuilder.toString();
    }

    private static Map<String, String> getParameters(HttpExchange httpExchange) throws UnsupportedEncodingException {
//        System.out.println("URI.query: " + httpExchange.getRequestURI().getQuery());
        String[] params = URLDecoder.decode(httpExchange.
                getRequestURI()
                .getQuery(), "UTF-8")
                .split("&");
        Map<String, String> paramMap = new HashMap<>();

        for (String str : params) {
            String[] keyVal = str.split("=");
            paramMap.put(keyVal[0], keyVal[1]);
        }

        return paramMap;
    }

    private static String pathToStr(HttpExchange httpExchange) {
        String params = httpExchange.
                getRequestURI()
                .getPath();
        return params;
    }

    private static String[] pathToList(HttpExchange httpExchange) {
        String[] params = httpExchange.
                getRequestURI()
                .getPath()
                .split("/");
        return params;
    }
}
