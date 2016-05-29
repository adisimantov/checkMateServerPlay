package algo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import model.Location;
import model.MySqlDriver;
import model.Place;
import model.Type;
import services.PlacesService;

public class RecommendationManager {

	private static final int RADIUS = 3000;
	private static final double OTHER_PERCENTS = 0.05;
	private static final int TOTAL_RECOMMENDATION_AMOUNT = 30;

	public static List<Place> getRecommendedPlaces(Location location, JsonNode facebookTypes, int userId) {

		// convertListJson(facebookTypes);

		Map<Type, Integer> checkinTypes = new HashMap<Type, Integer>();
		List<Place> currTypePlaces;
		Map<Type, List<Place>> allPlaces = new HashMap<Type, List<Place>>();
		List<String> noGoogleTypeFbList = new ArrayList<String>();

		// set the places service with the current location and the radius
		PlacesService service = new PlacesService(location, RADIUS);

		// init the rating manager with the current location and user
		RatingManager.getInstance().init(location, userId);

		if (facebookTypes.isArray()) {
			for (JsonNode typeJson : facebookTypes) {

				int count = typeJson.get("count").asInt();
				String typeName = typeJson.get("type").asText();

				// get the google type by the facebook type name
				Type googleType = MySqlDriver.getGoogleType(typeName);

				// if there is a google type linked to the current facebook type
				if (googleType != null) {
					if (!checkinTypes.containsKey(googleType)) {

						// add the type with its amount
						checkinTypes.put(googleType, count);

						// get places list from google by the type.
						currTypePlaces = service.getPlaces(googleType.getName());
						// if there are any places, sort them by the rating
						// (desc)
						if ((currTypePlaces != null) && (!currTypePlaces.isEmpty())) {
							Collections.sort(currTypePlaces, new Comparator<Place>() {
								@Override
								public int compare(Place place1, Place place2) {
									return (place2.getRate().compareTo(place1.getRate()));
								}
							});
							allPlaces.put(googleType, currTypePlaces);
						}
					} else {
						checkinTypes.put(googleType, checkinTypes.get(googleType) + count);
					}
					// if there is no google type, get the interest linked to
					// the facebook type.
				} else {
					noGoogleTypeFbList.add(typeName);
				}
			}
		}

		// TODO: add the other random type lists
		allPlaces.put(Type.other, new ArrayList<Place>());

		Map<Type, Integer> finalAmounts = calcTypeAmount(checkinTypes);

		List<Place> finalPlaces = new ArrayList<Place>();

		for (Type type : finalAmounts.keySet()) {
			finalPlaces.addAll(getTop(allPlaces.get(type), finalAmounts.get(type)));
		}

		// TODO: extract the comparator
		Collections.sort(finalPlaces, new Comparator<Place>() {
			@Override
			public int compare(Place place1, Place place2) {
				return (place2.getRate().compareTo(place1.getRate()));
			}
		});

		return finalPlaces;
	}

	private static List<Place> getTop(List<Place> places, int amount) {

		List<Place> top = new ArrayList<Place>();
		if (places != null) {
			for (Place place : places) {
				if (place.getAllGoogleData()) {
					top.add(place);
					if (--amount <= 0) {
						break;
					}
				}
			}
		}

		return top;
	}

	private static Map<Type, Integer> calcTypeAmount(Map<Type, Integer> typeCount) {

		int total = TOTAL_RECOMMENDATION_AMOUNT;

		// remove the "other" category.
		total = (int) (total * (1 - OTHER_PERCENTS));

		// get the total sum of the type counts
		int sum = typeCount.values().stream().mapToInt(Integer::intValue).sum();

		// create a new map with the amounts based on the final amount according
		// to the
		// percents of the types, rounding down.
		Map<Type, Integer> m = new HashMap<Type, Integer>();
		for (Map.Entry<Type, Integer> entry : typeCount.entrySet()) {
			m.put(entry.getKey(), total * (entry.getValue() / sum));
		}

		// get the final sum of the types and set the "other" type as the rest
		int finalSum = m.values().stream().mapToInt(Integer::intValue).sum();
		m.put(Type.other, TOTAL_RECOMMENDATION_AMOUNT - finalSum);

		return m;
	}

	private static Map<Type, Integer> convertListJson(JsonArray facebookTypes) {
		Map<Type, Integer> facebookTypesMap = new HashMap<Type, Integer>();
		for (int i = 0; i < facebookTypes.size(); i++) {
			JsonObject typeJson = facebookTypes.get(i).getAsJsonObject();
			int count = typeJson.get("count").getAsInt();
			String typeName = typeJson.get("type").getAsString();
			Type type = MySqlDriver.getGoogleType(typeName);
			facebookTypesMap.put(type, count);
		}
		return facebookTypesMap;
	}
}
