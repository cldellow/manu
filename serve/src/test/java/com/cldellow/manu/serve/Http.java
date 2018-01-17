package com.cldellow.manu.serve;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class Http {
    public static HttpResponse get(String theUrl) throws Exception {
        URL url = new URL(theUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setReadTimeout(15 * 1000);
        connection.connect();

        BufferedReader reader;

        try {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        } catch (FileNotFoundException fnfe) {
            return new HttpResponse(connection.getResponseCode(), "");
        }

        try {
            StringBuilder sb = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null)
                sb.append(line + "\n");

            return new HttpResponse(connection.getResponseCode(), sb.toString());
        } finally {
            connection.disconnect();
            reader.close();
        }
    }

    public static HttpResponse post(String theUrl, String body) throws Exception {
        URL url = new URL(theUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setReadTimeout(15 * 1000);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        OutputStream os = connection.getOutputStream();
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        bw.write(body);
        bw.flush();
        bw.close();

        connection.connect();

        BufferedReader reader;

        try {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        } catch (FileNotFoundException fnfe) {
            return new HttpResponse(connection.getResponseCode(), "");
        }

        try {
            StringBuilder sb = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null)
                sb.append(line + "\n");

            return new HttpResponse(connection.getResponseCode(), sb.toString());
        } finally {
            connection.disconnect();
            reader.close();
        }
    }
}
