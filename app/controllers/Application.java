package controllers;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import model.Location;
import model.Place;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import algo.EmotionsManager;
import algo.RecommendationManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Application extends Controller {

	public Result recommandations() {
		JsonNode data = request().body().asJson();
		int userId = data.findPath("USER_ID").asInt();
		Long time = data.findPath("TIME").asLong();
		String lng = data.findPath("LNG").asText();
		String lat = data.findPath("LAT").asText();
		JsonNode types = data.findPath("TYPES");
		
		return recommandations(userId, time, lng, lat, types);		
	}
	
	public Result getRecommandations(Integer userId, Long time, String longitude, String latitude) {
		
		JsonNode data = request().body().asJson();
		System.out.println(data);
		return recommandations(userId,time,longitude,latitude,null);
	}
	

	public Result recommandations(Integer userId, Long time, String longitude, String latitude, JsonNode types) {

		Location currentLocation = new Location(latitude, longitude);

		if (types == null) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				types = mapper.readTree("[{\"type\":\"Cafe\",\"count\":7}]");
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		
		List<Place> recommendedPlaces = 
				RecommendationManager.getRecommendedPlaces(currentLocation, types, userId, cal);
		JsonNode result = Json.toJson(recommendedPlaces);
		return ok(result);
	}
	
	/**
	 * gets a JSON ARRAY of places with it's google type id.
	 * @return
	 */
	
	public Result emotions(){
		JsonNode data = request().body().asJson();
		EmotionsManager.sendEmotions(data);
		return ok();
	}
	
	public Result user(){
		JsonNode data = request().body().asJson();
		//TODO: create user
		return ok();
	}

	public Result index() {
		final JsonNode jsonResponse = Json.toJson("Your new application is ready.");
		return ok(jsonResponse);
	}
}
