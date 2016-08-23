package algo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import model.Interest;
import model.Location;
import model.MySqlDriver;
import model.Place;
import model.Type;
import services.PlacesService;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class RecommendationManager {

	private static final int RADIUS = 3000;
	private static final double OTHER_PERCENTS = 0.10;
	private static final int TOTAL_RECOMMENDATION_AMOUNT = 30;

	public static List<Place> getRecommendedPlaces(Location location, JsonNode facebookTypes, int userId,
			Calendar time) {
		
		// convertListJson(facebookTypes);
		Map<Type, Integer> checkinTypes = new HashMap<Type, Integer>();
		List<Place> currTypePlaces;
		Map<Type, List<Place>> allPlaces = new HashMap<Type, List<Place>>();
		List<String> allFbTypeNames = new ArrayList<String>();
		List<Integer> allGoogleTypeName = new ArrayList<Integer>();

		// set the places service with the current location and the radius
		PlacesService service = new PlacesService(location, RADIUS);

		// init the rating manager with the current location and user
		RatingManager.getInstance().init(location, userId);

		if (facebookTypes.isArray()) {
			for (int i = 0; i < facebookTypes.size(); i++) {
				JsonNode typeJson = facebookTypes.get(i);
				int count = typeJson.get("count").asInt();
				String typeName = typeJson.get("type").asText();

				// get the google type by the facebook type name
				Type googleType = MySqlDriver.getGoogleTypeFromFacebook(typeName);

				allFbTypeNames.add(typeName);

				// if there is a google type linked to the current facebook type
				if (googleType != null) {
					allGoogleTypeName.add(googleType.getId());

					if (!checkinTypes.containsKey(googleType)) {

						// add the type with its amount
						checkinTypes.put(googleType, count);

						// get places list from google by the type.
						currTypePlaces = service.getPlaces(googleType);
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
				}
			}
		}

		List<Type> otherTypes = MySqlDriver.getGoogleTypesByInterestWithRate(allFbTypeNames, allGoogleTypeName, userId);
		List<Type> chosenOther = new ArrayList<Type>();
		Type randomType;
		for (int i = 0; i < 3 && !otherTypes.isEmpty(); i++) {
			randomType = getRandomType(otherTypes);
			chosenOther.add(randomType);
			otherTypes.remove(randomType);
		}

		List<Place> otherPlaces = new ArrayList<Place>();
		List<Place> curr;

		for (Type googType : chosenOther) {
			curr = service.getPlaces(googType);
			otherPlaces.addAll(curr);
		}

		if ((otherPlaces != null) && (!otherPlaces.isEmpty())) {
			Collections.sort(otherPlaces, new Comparator<Place>() {
				@Override
				public int compare(Place place1, Place place2) {
					return (place2.getRate().compareTo(place1.getRate()));
				}
			});
		}

		allPlaces.put(Type.other, otherPlaces);
		List<Place> finalPlaces = new ArrayList<Place>();

		Map<Type, Integer> finalAmounts = calcTypeAmount(checkinTypes);

		for (Type type : finalAmounts.keySet()) {
			finalPlaces.addAll(getTop(allPlaces.get(type), finalPlaces,finalAmounts.get(type), time));
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

	private static Type getRandomType(List<Type> googleTypes) {

		// Compute the total weight of all items together
		double totalWeight = 0.0d;
		for (Type i : googleTypes) {
			totalWeight += i.getRate();
		}

		// Now choose a random item
		int randomIndex = -1;
		double random = Math.random() * totalWeight;
		for (int i = 0; i < googleTypes.size(); i++) {
			random -= googleTypes.get(i).getRate();
			if (random <= 0.0d) {
				randomIndex = i;
				break;
			}
		}

		return ((randomIndex == -1) ? null : googleTypes.get(randomIndex));
	}

	private static Map<Interest, Integer> sortByComparator(Map<Interest, Integer> unsortMap) {

		// Convert Map to List
		List<Map.Entry<Interest, Integer>> list = new LinkedList<Map.Entry<Interest, Integer>>(unsortMap.entrySet());

		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<Interest, Integer>>() {
			public int compare(Map.Entry<Interest, Integer> o1, Map.Entry<Interest, Integer> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		// Convert sorted map back to a Map
		Map<Interest, Integer> sortedMap = new LinkedHashMap<Interest, Integer>();
		for (Iterator<Map.Entry<Interest, Integer>> it = list.iterator(); it.hasNext();) {
			Map.Entry<Interest, Integer> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

	private static List<Place> getTop(List<Place> places,List<Place> finalPlaces ,int amount, Calendar time) {

		List<Place> top = new ArrayList<Place>();
		if (places != null) {
			for (Place place : places) {
				if (!finalPlaces.contains(place)) {
					place.fetchFullData();
					if (place.isOpen(time)) {
						top.add(place);
						if (--amount <= 0) {
							break;
						}
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
			Type type = MySqlDriver.getGoogleTypeFromFacebook(typeName);
			facebookTypesMap.put(type, count);
		}
		return facebookTypesMap;
	}
}
