package com.ifewalter.android.textonmotion.location;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class ShortenLocationUrl {

	private static final String GOOG_URL = "https://www.googleapis.com/urlshortener/v1/url?shortUrl=http://goo.gl/fbsS&key=AIzaSyBZqYe3ighAE7V8C2DelaSU2fd2oZ_K6zU";

	private static final String MAPS_URL = "http://maps.google.com/maps?q=loc:";
	private static String shortUrl, longUrl;

	public static String shorten(float latitude, float longitude) {

		longUrl = MAPS_URL + String.valueOf(latitude) + ","
				+ String.valueOf(longitude);

		try {
			URLConnection conn = new URL(GOOG_URL).openConnection();
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "application/json");

			OutputStreamWriter wr = new OutputStreamWriter(
					conn.getOutputStream());
			wr.write("{\"longUrl\":\"" + longUrl + "\"}");
			wr.flush();

			// Get the response
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			String line;

			while ((line = rd.readLine()) != null) {
				if (line.indexOf("id") > -1) {
					// I'm sure there's a more elegant way of parsing
					// the JSON response, but this is quick/dirty =)
					shortUrl = line.substring(8, line.length() - 2);
					break;
				}
			}

			wr.close();
			rd.close();
		} catch (MalformedURLException ex) {
			// shortUrl = ex.toString();
			shortUrl = longUrl;
		} catch (IOException ex) {
			// shortUrl = ex.toString();
			shortUrl = longUrl;
		}

		return shortUrl;
	}

}
