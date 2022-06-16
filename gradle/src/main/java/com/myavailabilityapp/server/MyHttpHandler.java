package com.myavailabilityapp.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.text.StringEscapeUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

public class MyHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Map requestParamValue = null;
        if("GET".equals(httpExchange.getRequestMethod())) {
            requestParamValue = getParameters(httpExchange);
        }

        handleResponse(httpExchange,requestParamValue);
    }

    private Map<String, String> getParameters(HttpExchange httpExchange) {
        String[] params = httpExchange.
                getRequestURI()
                .toString()
                .split("\\?")[1]
                .split("&");
        Map<String, String> paramMap = new HashMap<>();

        for (String str : params) {
            String[] keyVal = str.split("=");
            paramMap.put(keyVal[0], keyVal[1]);
        }

        return paramMap;
    }

    private void handleResponse(HttpExchange httpExchange, Map requestParamValue)  throws  IOException {
        OutputStream outputStream = httpExchange.getResponseBody();
        StringBuilder htmlBuilder = new StringBuilder();

        htmlBuilder.append(requestParamValue);
        String htmlResponse = StringEscapeUtils.escapeHtml4(htmlBuilder.toString());

        httpExchange.sendResponseHeaders(200, htmlResponse.length());
        outputStream.write(htmlResponse.getBytes());
        outputStream.flush();
        outputStream.close();
    }
}