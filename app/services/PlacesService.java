package services;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import model.Location;
import model.Place;

public class PlacesService {
	
	private int radius;
	private Location location;

	public PlacesService(Location location, int radius) {
		this.radius = radius;
		this.location = location;
	}

	private static final String HEBREW_LANG = "iw";
	private static final String API_SERVER_KEY = "AIzaSyB9y8JYZK-7D2Cag6oFEK5km8DOg5SAn6w";
	private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
	private static final String TYPE_SEARCH = "/nearbysearch";
	private static final String OUT_JSON = "/json";

	public ArrayList<Place> getPlaces(String type) {

		try {
			String json = getPlacesJson(this.location, this.radius, type, null);

			ArrayList<Place> arrayList = new ArrayList<Place>();

			while (json != null) {
				JsonObject object = new JsonParser().parse(json).getAsJsonObject();
				JsonArray array = object.get("results").getAsJsonArray();
				String nextPage = null;
				if (object.get("next_page_token") != null) {
					nextPage = object.get("next_page_token").getAsString();
				}
				for (int i = 0; i < array.size(); i++) {
					try {
						Place place = new Place((JsonObject) array.get(i));
						arrayList.add(place);
					} catch (Exception e) {
						System.out.println(e.getMessage());
						e.printStackTrace();
					}
				}
				if (nextPage != null) {
					json = getPlacesJson(this.location, this.radius, type, nextPage);
				} else {
					json = null;
				}
			}

			return arrayList;
		}

		catch (Exception ex) {
			Logger.getLogger(PlacesService.class.getName()).log(Level.SEVERE, null, ex);
		}

		return null;
	}

	private String getPlacesJson(Location currLoc, int radius, String type, String nextPage) {
		HttpURLConnection conn = null;
		StringBuilder jsonResults = new StringBuilder();
		try {
			URL url = makeUrl(currLoc, radius, type, nextPage);
			conn = (HttpURLConnection) url.openConnection();
			InputStreamReader in = new InputStreamReader(conn.getInputStream(), "UTF-8");

			int read;
			char[] buff = new char[1024];
			while ((read = in.read(buff)) != -1) {
				jsonResults.append(buff, 0, read);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}

		return jsonResults.toString();
	}

	private URL makeUrl(Location currLoc, int radius, String type, String nextPage) throws MalformedURLException {
		StringBuilder sb = new StringBuilder(PLACES_API_BASE);
		sb.append(TYPE_SEARCH);
		sb.append(OUT_JSON);
		sb.append("?key=" + API_SERVER_KEY);
		sb.append("&language=" + HEBREW_LANG);
		sb.append("&location=" + currLoc.lat + "," + currLoc.lng);
		sb.append("&radius=" + String.valueOf(radius));
		sb.append("&type=" + type);
		if (nextPage != null) {
			sb.append("&pagetoken=" + nextPage);
		}
		// sb.append("&opennow");

		URL url = new URL(sb.toString());
		return url;
	}

}