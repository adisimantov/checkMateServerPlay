package controllers;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import algo.RecommendationManager;
import model.Location;
import model.Place;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller {
	
	public Result recommandations(Integer userId, Long time, String longitude, String latitude, String js) {
        
		Location currentLocation = new Location(latitude, longitude);
		
		JsonNode types = request().body().asJson();
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
		
		//return ok(types);
		//JsonArray jaTypes = new JsonParser().parse(types).getAsJsonArray();
		
		List<Place> recommendedPlaces = RecommendationManager.getRecommendedPlaces(currentLocation, types, userId);

		JsonNode result = Json.toJson(recommendedPlaces);
		
		return ok(result);
	}
    
	public Result index() {
        final JsonNode jsonResponse = Json.toJson("Your new application is ready.");
        return ok(jsonResponse);
    }
}
