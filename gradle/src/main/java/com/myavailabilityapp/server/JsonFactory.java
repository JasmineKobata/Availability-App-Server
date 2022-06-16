package com.myavailabilityapp.server;

import com.google.gson.Gson;
import org.json.JSONObject;

public class JsonFactory {
    static JSONObject json = new JSONObject();

    public static String classToJson(Object obj) {
//        file.writeString(json.toJson(obj), false);
        Gson gson = new Gson();
        return gson.toJson(obj);
    }

    public static <T> T classFromJson(String json, Class<T> type) {
//        return (T) json.fromJson(type, file);
        Gson gson = new Gson();
        return gson.fromJson(json, type);
    }

    public static <T> T classFromJsonString(String s, Class<T> type) {
        Gson gson = new Gson();
        return (T) gson.fromJson(s, type);
    }
}
