package services;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.Location;
import model.Place;
import model.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class PlacesService {
	
	private int radius;
	private Location location;

	public PlacesService() {
	}
	
	public PlacesService(Location location, int radius) {
		this.radius = radius;
		this.location = location;
	}

	private static final String TYPE_SEARCH = "/nearbysearch";
	private static final String TEXT_SEARCH = "/textsearch";
	private static final String DETAILS_SEARCH = "/details";

	public ArrayList<Place> getPlaces(Type type) {

		try {
			String json = getPlacesJson(this.location, this.radius, type.getName(), null);

			ArrayList<Place> arrayList = new ArrayList<Place>();

//			while (json != null) {
				JsonObject object = new JsonParser().parse(json).getAsJsonObject();
				JsonArray array = object.get("results").getAsJsonArray();
				String nextPage = null;
				if (object.get("next_page_token") != null) {
					nextPage = object.get("next_page_token").getAsString();
				}
				for (int i = 0; i < array.size(); i++) {
					try {
						Place place = new Place((JsonObject) array.get(i), type);
						arrayList.add(place);
					} catch (Exception e) {
						System.out.println(e.getMessage());
						e.printStackTrace();
					}
				}
//				if (nextPage != null) {
//					json = getPlacesJson(this.location, this.radius, type.getName(), nextPage);
//				} else {
//					json = null;
//				}
//			}

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
			InputStreamReader in = new InputStreamReader(conn.getInputStream(), GoogleServicesCons.UTF8);

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

	private String getPlaceJsonByName(String text) {
		HttpURLConnection conn = null;
		StringBuilder jsonResults = new StringBuilder();
		try {
			URL url = makeTextSearchUrl(text);
			conn = (HttpURLConnection) url.openConnection();
			InputStreamReader in = new InputStreamReader(conn.getInputStream(), GoogleServicesCons.UTF8);

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
	
	private String getPlaceJsonById(String place_id) {
		HttpURLConnection conn = null;
		StringBuilder jsonResults = new StringBuilder();
		try {
			URL url = makePlaceDetailsUrl(place_id);
			conn = (HttpURLConnection) url.openConnection();
			InputStreamReader in = new InputStreamReader(conn.getInputStream(), GoogleServicesCons.UTF8);

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
	
	public Place getPlaceByName(String text) {

		try {
			String json = getPlaceJsonByName(text);

			Place place = null;

			if (json != null) {
				place = new Place();
				JsonObject object = new JsonParser().parse(json).getAsJsonObject();
				JsonArray array = object.get("results").getAsJsonArray();

				try {
					place = new Place((JsonObject) array.get(0), null);
				} catch (Exception e) {
					System.out.println(e.getMessage());
					e.printStackTrace();
				}
			}

			return place;
		}

		catch (Exception ex) {
			Logger.getLogger(PlacesService.class.getName()).log(Level.SEVERE, null, ex);
		}

		return null;
	}
	
	public Place getPlaceById(String place_id) {

		try {
			String json = getPlaceJsonById(place_id);

			Place place = null;

			if (json != null) {
				place = new Place();
				JsonObject object = new JsonParser().parse(json).getAsJsonObject();
				JsonArray array = object.get("results").getAsJsonArray();

				try {
					place = new Place((JsonObject) array.get(0), null);
				} catch (Exception e) {
					System.out.println(e.getMessage());
					e.printStackTrace();
				}
			}

			return place;
		}

		catch (Exception ex) {
			Logger.getLogger(PlacesService.class.getName()).log(Level.SEVERE, null, ex);
		}

		return null;
	}
	
	private URL makeUrl(Location currLoc, int radius, String type, String nextPage) throws MalformedURLException {
		StringBuilder sb = new StringBuilder(GoogleServicesCons.PLACES_API_BASE);
		sb.append(TYPE_SEARCH);
		sb.append(GoogleServicesCons.OUT_JSON);
		sb.append("?key=" + GoogleServicesCons.API_SERVER_KEY);
		sb.append("&language=" + GoogleServicesCons.HEBREW_LANG);
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

	private URL makeTextSearchUrl(String text) throws MalformedURLException {
		StringBuilder sb = new StringBuilder(GoogleServicesCons.PLACES_API_BASE);
		sb.append(TEXT_SEARCH);
		sb.append(GoogleServicesCons.OUT_JSON);
		sb.append("?key=" + GoogleServicesCons.API_SERVER_KEY);
		sb.append("&language=" + GoogleServicesCons.HEBREW_LANG);
		sb.append("&query=" + text);

		URL url = new URL(sb.toString());
		return url;
	}
	
	private URL makePlaceDetailsUrl(String place_id) throws MalformedURLException {
		StringBuilder sb = new StringBuilder(GoogleServicesCons.PLACES_API_BASE);
		sb.append(DETAILS_SEARCH);
		sb.append(GoogleServicesCons.OUT_JSON);
		sb.append("?key=" + GoogleServicesCons.API_SERVER_KEY);
		sb.append("&language=" + GoogleServicesCons.HEBREW_LANG);
		sb.append("&placeid=" + place_id);

		URL url = new URL(sb.toString());
		return url;
	}
}
