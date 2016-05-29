package model;

import java.util.HashMap;
import java.util.Map;

public class TypeDictionary {
	private static Map<String, String> typeN;
	
	public static Map<String, String> getInstance(){
		if(typeN == null){
			initMap();
		}
		return typeN;
	}
	
	private static void initMap(){
		typeN = new HashMap<String, String>();
		
		typeN.put("Amusement Park Ride", "amusement_park");
		typeN.put("Zoo & Aquarium", "zoo");
		typeN.put("Art Gallery", "art_gallery");
		typeN.put("Bakery", "bakery");
		typeN.put("Bar", "bar");
		typeN.put("Beauty Salon", "beauty_salon");
		typeN.put("Bowling Alley", "bowling_alley");
		typeN.put("Cafe", "cafe");
		typeN.put("Campground", "campground");
		typeN.put("Casino", "casino");
		typeN.put("Gym", "gym");
		typeN.put("Library", "library");
		typeN.put("Rental Shop", "movie_rental");
		typeN.put("Movie Theatre", "movie_theater");
		typeN.put("Museum", "museum");
		typeN.put("Night Club", "night_club");
		typeN.put("Park", "park");
		typeN.put("Restaurant", "restaurant");
		typeN.put("Shopping Mall", "shopping_mall");
		typeN.put("Spa, Beauty & Personal Care", "spa");
		typeN.put("Sports Venue & Stadium", "stadium");
		typeN.put("Petting Zoo", "zoo");
	}
}