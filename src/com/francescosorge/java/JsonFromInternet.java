package com.francescosorge.java;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

class JsonFromInternet {
    private String url;
    private JsonObject json;

    JsonFromInternet(String pUrl) {
        this.url = pUrl;

        // Connect to the URL using java's native library
        try {
            URL url = new URL(pUrl);
            URLConnection request = url.openConnection();
            request.connect();

            // Convert to a JSON object to print data
            JsonParser jp = new JsonParser(); //from gson
            JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); // Convert the input stream to a json element
            this.json = root.getAsJsonObject(); //May be an array, may be an object.
        } catch (Exception e) {
            System.out.println("An unknown error has just occurred. Error: " + e.toString());
        }
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return this.json.toString();
    }

    JsonObject getJson() {
        return json;
    }
}
