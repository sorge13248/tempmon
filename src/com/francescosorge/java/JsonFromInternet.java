package com.francescosorge.java;

// https://mvnrepository.com/artifact/com.google.code.gson/gson
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

class JsonFromInternet {
    private static final double VERSION = 2.0;
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
        return this.getValueAsString(field);
    }

    public String getValueAsString(String field) {
        try {
            return this.json.get(field).getAsString();
        }catch(Exception e) {
            return "An unknown error has just occurred. Error: " + e.toString();
        }
    }

    public int getValueAsInt(String field) {
        try {
            return this.json.get(field).getAsInt();
        }catch(Exception e) {
            return -1;
        }
    }

    public double getValueAsDouble(String field) {
        try {
            return this.json.get(field).getAsDouble();
        }catch(Exception e) {
            return -1;
        }
    }

    public byte getValueAsByte(String field) {
        try {
            return this.json.get(field).getAsByte();
        }catch(Exception e) {
            return -1;
        }
    }

    public boolean getValueAsBollean(String field) throws Exception {
        try {
            return this.json.get(field).getAsBoolean();
        }catch(Exception e) {
            throw new Exception("Something went wrong while calling getValueAsBoolean on " + field);
        }
    }

    public boolean isValueNull(String field) {
        try {
            this.json.get(field).getAsJsonNull();
            return true;
        }catch(Exception e) {
            return false;
        }
    }

    public boolean hasKey(String field) {
        return this.json.has(field);
    }
}
