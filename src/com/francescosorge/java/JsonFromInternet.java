package com.francescosorge.java;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

class JsonFromInternet {
    private String url;
    private JsonObject json;

    public JsonFromInternet() {
    }

    public JsonFromInternet(String pUrl) throws MalformedURLException {
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
        } catch (MalformedURLException e) {
            throw new MalformedURLException();
        } catch (Exception e) {
            System.out.println("An unknown error has just occurred. Error: " + e.toString());
        }
    }

    @Override
    public String toString() {
        return this.json.toString();
    }

    public String getValue(String field) {
        try {
            return this.json.get(field).getAsString();
        }catch(Exception e) {
            return "An unknown error has just occurred. Error: " + e.toString();
        }
    }

    public boolean hasKey(String field) {
        return this.json.has(field);
    }
}
