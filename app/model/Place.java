package model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import services.FacebookPlace;
import services.LoggedInFacebookClient;
import services.PlaceDetailsService;
import algo.RatingManager;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.restfb.Connection;
import com.restfb.FacebookClient;
import com.restfb.Parameter;

public class Place {
	private String place_id;
	private String icon;
	private String name;
	private Location location = new Location();
	private float googleRating;
	private List<Type> types = new ArrayList<Type>();
	private double distance;
	private Type chosenType;

	private String address;
	private String phoneNumber;
	private String url;
	private String[] openHoursText;
	private JsonNode openHours;
	private String photo;

	// local
	private Double rate;

	// facebook
	private int checkins;

	public List<Type> getTypes() {
		return types;
	}

	public String getPlaceId() {
		return place_id;
	}

	public void setPlaceId(String PlaceId) {
		this.place_id = PlaceId;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public Double getLatitude() {
		return Double.parseDouble(location.lat);
	}

	public void setLatitude(Double latitude) {
		this.location.lat = latitude.toString();
	}

	public Double getLongitude() {
		return Double.parseDouble(location.lng);
	}

	public void setLongitude(Double longitude) {
		this.location.lng = longitude.toString();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setTypes(List<Type> types) {
		this.types = types;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Double getRate() {
		return rate;
	}

	public void setRate(Double rate) {
		this.rate = rate;
	}

	public int getCheckins() {
		return checkins;
	}

	public void setCheckins(int checkins) {
		this.checkins = checkins;
	}

	public float getGoogleRating() {
		return googleRating;
	}

	public void setGoogleRating(float googlRating) {
		this.googleRating = googlRating;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public Type getChosenType() {
		return chosenType;
	}

	public void setChosenType(Type chosenType) {
		this.chosenType = chosenType;
	}

	public String getWebsite() {
		return url;
	}

	public void setWebsite(String website) {
		this.url = website;
	}

	public String[] getOpenHoursText() {
		return openHoursText;
	}

	public void setOpenHoursText(String[] openHoursText) {
		this.openHoursText = openHoursText;
	}

	public JsonNode getOpenHours() {
		return openHours;
	}

	public void setOpenHours(JsonNode openHours) {
		this.openHours = openHours;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public Place() {
	}

	public void fetchFullData() {
		PlaceDetailsService pds = new PlaceDetailsService(this.place_id);
		JsonNode details = pds.getDetails();

		this.setAddress(details.findPath("formatted_address").asText());
		this.setPhoneNumber(details.findPath("formatted_phone_number").asText());
		this.setUrl(details.findPath("website").asText());

		JsonNode openingHours = details.findPath("opening_hours");
		if (openingHours != null) {
			this.setOpenHours(openingHours.findPath("periods"));
			JsonNode tempWeekDay = openingHours.findPath("weekday_text");
			int length = tempWeekDay.size();
			String[] h = new String[length];

			if (length > 0) {
				for (int i = 0; i < length; i++) {
					h[i] = tempWeekDay.get(i).asText();
				}
			}
			this.setOpenHoursText(h);
		}

		JsonNode photos = details.findPath("photos").get(0);
		if (photos != null) {
			this.setPhoto(photos.findPath("photo_reference").asText());
		}
	}

	public Place(JsonObject placeJson, Type chosenType) {
		try {
			this.chosenType = chosenType;
			JsonObject geometry = (JsonObject) placeJson.get("geometry");
			JsonObject location = (JsonObject) geometry.get("location");
			this.setLatitude(location.get("lat").getAsDouble());
			this.setLongitude(location.get("lng").getAsDouble());
			this.setIcon(placeJson.get("icon").getAsString());
			this.setName(placeJson.get("name").getAsString());

			this.setPlaceId(placeJson.get("place_id").getAsString());
			JsonElement rating = placeJson.get("rating");
			if (rating != null) {
				this.setGoogleRating(rating.getAsFloat());
			}

			JsonArray typesArray = placeJson.getAsJsonArray("types");

			for (int j = 0; j < typesArray.size(); j++) {
				Type t = MySqlDriver.getGoogleType(typesArray.get(j).getAsString());
				if (t != null) {
					this.getTypes().add(t);
				}
			}

			this.fetchFacebookData();

			this.rate = RatingManager.getInstance().getRate(this);

		} catch (JsonParseException ex) {
			Logger.getLogger(Place.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public boolean isOpen(Calendar chosenDate) {
		DateFormat format = new SimpleDateFormat("HHmm");
		int day = chosenDate.get(Calendar.DAY_OF_WEEK);

		if (this.openHours != null && !this.openHours.isNull() && this.openHours.isArray()) {
			for (JsonNode jsonNode : this.openHours) {
				try {
					JsonNode open = jsonNode.findPath("open");
					int openDay = open.findPath("day").asInt() + 1;
					if (day == openDay) {
						JsonNode openTime = open.findPath("time");
						Calendar openCalendar = Calendar.getInstance();
						Date openTimeDate = format.parse(openTime.asText());
						openCalendar.setTime(openTimeDate);
						openCalendar.set(chosenDate.get(Calendar.YEAR), chosenDate.get(Calendar.MONTH),
								chosenDate.get(Calendar.DATE));

						JsonNode close = jsonNode.findPath("close");
						int closeDay = close.findPath("day").asInt() + 1;
						JsonNode closeTime = close.findPath("time");
						Calendar closeCalendar = Calendar.getInstance();
						Date closeTimeDate = format.parse(closeTime.asText());
						closeCalendar.setTime(closeTimeDate);
						closeCalendar.set(chosenDate.get(Calendar.YEAR), chosenDate.get(Calendar.MONTH),
								chosenDate.get(Calendar.DATE) + (closeDay - openDay));

						if (openCalendar.before(chosenDate) && closeCalendar.after(chosenDate)) {
							return true;
						}
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}

			return false;

		} else {
			return true;
		}
	}

	public void fetchFacebookData() {
		FacebookClient facebookClient = new LoggedInFacebookClient();
		Connection<FacebookPlace> placeSearch = facebookClient.fetchConnection("search", FacebookPlace.class,
				Parameter.with("q", this.getName()), Parameter.with("type", "place"),
				Parameter.with("fields", "id,likes,name,checkins,location,category,category_list"));
		// TODO: remove chars like ', ", /, \ and such....
		int size = placeSearch.getData().size();
		int checkins = 0;
		if (size <= 0) {

		} else if (size <= 2) {
			for (FacebookPlace place : placeSearch.getData()) {
				// System.out.println(place.getCheckins());
				if (place.getCheckins() != null) {
					checkins += place.getCheckins().intValue();
				}
			}
		} else {
			for (int i = 0; i < 3; i++) {
				// TODO: check locations?
				// System.out.println(placeSearch.getData().get(i).getCheckins());
				if (placeSearch.getData().get(i).getCheckins() != null) {
					checkins += placeSearch.getData().get(i).getCheckins().intValue();
				}
			}
		}

		this.setCheckins(checkins);

	}

	@Override
	public boolean equals(Object obj) {
		return this.place_id.equals(((Place) obj).place_id);
	}

	@Override
	public String toString() {
		return "Place{" + this.name + ":" + this.rate + " - distance=" + this.distance + " ,id=" + this.place_id + "}";
	}

}