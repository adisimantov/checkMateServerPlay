package controllers;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import model.Location;
import model.MySqlDriver;
import model.Place;

import org.joda.time.LocalDate;
import org.joda.time.Years;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.LoggedInFacebookClient;
import services.PlaceFacebookClient;
import services.PlacesService;
import algo.EmotionsManager;
import algo.RecommendationManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.FacebookType;
import com.restfb.types.User;

public class Application extends Controller {
	static {
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		Runnable task = new SimilarityThread();    
		executor.scheduleAtFixedRate(task, 0, 30, TimeUnit.MINUTES);
	}
	
	public Result login() {
		JsonNode data = request().body().asJson();
		String userId = data.findPath("USER_ID").asText();
		String token = data.findPath("TOKEN").asText();
		boolean result = true;
		
		if (MySqlDriver.getUser(userId) == null) {
			FacebookClient facebookClient = new LoggedInFacebookClient();
			User getUser = 
					facebookClient.fetchObject(userId , User.class,Parameter.with("fields", "id,gender,birthday"));
			
			String gender = null;
			if (getUser.getGender() != null) {
				if ("female".equals(getUser.getGender())) {
					gender = "f";
				} else if ("male".equals(getUser.getGender())){
					gender = "m";
				}
			}
			
			Integer age = null;
			if (getUser.getBirthdayAsDate() != null) {
				LocalDate now = new LocalDate();
				LocalDate birthday = new LocalDate(getUser.getBirthdayAsDate());
				age = Years.yearsBetween(birthday, now).getYears();
			}
			result = MySqlDriver.setUser(userId, token, age, gender);
		}
		
		if (result) {
			return ok();
		} else {
			return internalServerError();
		}
	}
	
	public Result saveCheckins() throws ParseException {
		JsonNode data = request().body().asJson();
		proccessCheckins(data);
		
		return ok();
	}
	
	public Result checkins() throws ParseException {
		String checkinsStr = "{\"CHECKINS\":[{\"created_time\":\"2016-03-21T17:35:34+0000\",\"place\":{\"name\":\"Exitroom Ramat-Gan\",\"category_list\":[{\"id\":\"133436743388217\",\"name\":\"Arts & Entertainment\"}],\"location\":{\"city\":\"Ramat Gan\",\"country\":\"Israel\",\"latitude\":32.087622764,\"longitude\":34.813273992,\"street\":\"Jabotinsky 83\"},\"id\":\"409390919243331\"},\"id\":\"1225267250835626\"},{\"created_time\":\"2015-01-26T14:35:32+0000\",\"place\":{\"name\":\"מרכז הירידים תל אביב - גני התערוכה\",\"category_list\":[{\"id\":\"133436743388217\",\"name\":\"Arts & Entertainment\"},{\"id\":\"198503866828628\",\"name\":\"Organization\"}],\"location\":{\"city\":\"Tel Aviv\",\"country\":\"Israel\",\"latitude\":32.0892296,\"longitude\":34.7797318,\"street\":\"שדרות רוקח\",\"zip\":\"69020\"},\"id\":\"202280733146165\"},\"id\":\"987429847952702\"},{\"created_time\":\"2013-09-04T10:12:01+0000\",\"place\":{\"name\":\"Abu Shukri\",\"category_list\":[{\"id\":\"273819889375819\",\"name\":\"Restaurant\"}],\"location\":{\"city\":\"Jerusalem\",\"country\":\"Israel\",\"latitude\":31.77960222016,\"longitude\":35.232370265627},\"id\":\"163213423733444\"},\"id\":\"689949581034065\"},{\"created_time\":\"2013-05-02T10:30:16+0000\",\"place\":{\"name\":\"Google Tel-Aviv\",\"category_list\":[{\"id\":\"152142351517013\",\"name\":\"Corporate Office\"}],\"location\":{\"city\":\"Tel Aviv\",\"country\":\"Israel\",\"latitude\":32.06370225,\"located_in\":\"337084306340818\",\"longitude\":34.779732417394,\"zip\":\"25900\"},\"id\":\"168772183146117\"},\"id\":\"625782667450757\"},{\"created_time\":\"2012-11-13T18:11:23+0000\",\"place\":{\"name\":\"קפה ג'ו\",\"category_list\":[{\"id\":\"197871390225897\",\"name\":\"Cafe\"},{\"id\":\"128673187201735\",\"name\":\"Coffee Shop\"}],\"location\":{\"city\":\"Qiryat Ono\",\"country\":\"Israel\",\"latitude\":32.056603824644,\"longitude\":34.866729453496},\"id\":\"181134078600241\"},\"id\":\"539675386061486\"}]}";
		//String checkinsStr = "{\"CHECKINS\":[{\"created_time\":\"2016-06-13T12:46:34+0000\",\"place\":{\"name\":\"סטימצקי\",\"category_list\":[{\"id\":\"197048876974331\",\"name\":\"Book Store\"}],\"location\":{\"latitude\":32.08019926169,\"longitude\":34.773794801758},\"id\":\"113221342097861\"},\"id\":\"595096060651571\"},{\"created_time\":\"2015-11-29T17:02:51+0000\",\"place\":{\"name\":\"הדירה של איגור ונועם\",\"category_list\":[{\"id\":\"197289820310880\",\"name\":\"Home\"}],\"id\":\"1719006521666335\"},\"id\":\"516428145185030\"},{\"created_time\":\"2015-10-16T09:00:00+0000\",\"place\":{\"name\":\"האחוזה-בית חנן\",\"category_list\":[{\"id\":\"211155112228091\",\"name\":\"Event Venue\"}],\"location\":{\"city\":\"Beit Hanan\",\"country\":\"Israel\",\"latitude\":31.936398879174,\"longitude\":34.76756768244},\"id\":\"124877367670089\"},\"id\":\"513773542117157\"},{\"created_time\":\"2015-06-03T19:50:54+0000\",\"place\":{\"name\":\"בלאק בורגר פתח תקווה\",\"category_list\":[{\"id\":\"2500\",\"name\":\"Local Business\"}],\"location\":{\"city\":\"Petah Tiqwa\",\"country\":\"Israel\",\"latitude\":32.088653040706,\"longitude\":34.856071557608,\"street\":\"הסיבים 18\",\"zip\":\"49517\"},\"id\":\"118019081608029\"},\"id\":\"452277721600073\"},{\"created_time\":\"2015-04-05T18:23:47+0000\",\"place\":{\"name\":\"תערוכת משחקי הכס ישראל - Game of Thrones Exhibition Israel בשיתוף Yes ו-hbo\",\"category_list\":[{\"id\":\"197384240287028\",\"name\":\"Art Gallery\"}],\"location\":{\"city\":\"Tel Aviv\",\"country\":\"Israel\",\"latitude\":32.098316967529,\"located_in\":\"134885479889002\",\"longitude\":34.774314050376,\"zip\":\"63508\"},\"id\":\"1572346236347712\"},\"id\":\"429395950554917\"},{\"created_time\":\"2014-12-05T02:23:45+0000\",\"place\":{\"name\":\"בית!\",\"category_list\":[{\"id\":\"2500\",\"name\":\"Local Business\"}],\"location\":{\"city\":\"Oranit\",\"country\":\"Israel\",\"latitude\":32.131309613757,\"longitude\":34.987772544629},\"id\":\"186426761391840\"},\"id\":\"376837892477390\"},{\"created_time\":\"2014-11-30T13:14:28+0000\",\"place\":{\"name\":\"Ben Gurion Airport\",\"category_list\":[{\"id\":\"128966563840349\",\"name\":\"Airport\"}],\"location\":{\"city\":\"Lod\",\"country\":\"Israel\",\"latitude\":32.009444444444,\"longitude\":34.882777777778,\"street\":\"Ben Gurion International Airport\",\"zip\":\"70150\"},\"id\":\"142460865765439\"},\"id\":\"375202632640916\"},{\"created_time\":\"2014-04-02T18:53:54+0000\",\"place\":{\"name\":\"אורנית\",\"category_list\":[{\"id\":\"224455390913969\",\"name\":\"City\"}],\"location\":{\"city\":\"Oranit\",\"country\":\"Israel\",\"latitude\":32.12821,\"longitude\":34.996,\"street\":\"אורנית\",\"zip\":\"44813\"},\"id\":\"240522982678867\"},\"id\":\"266598296834684\"}]}";
		JsonNode data = Json.parse(checkinsStr);
		proccessCheckins(data);

		return ok();
	}
	
	public void proccessCheckins(JsonNode data) throws ParseException {
		String user_id = data.findPath("USER_ID").asText();
		if (user_id == "") {
			user_id = "1217366534959031";
		}
		
		ArrayNode checkins = (ArrayNode) data.findPath("CHECKINS");
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ");
		PlacesService service = new PlacesService();
		for (JsonNode checkin : checkins) {
			String create_time = checkin.get("created_time").asText();
			Timestamp date_create_time = new Timestamp(format.parse(create_time).getTime());
			String checkin_id = checkin.get("id").asText();
			JsonNode place = checkin.findPath("place");
			
			String place_id = place.get("id").asText();
			String name = place.findPath("name").asText();
			JsonNode location = place.findPath("location");
			JsonNode categories = place.findPath("category_list");

			String city = location.findPath("city").asText();
			String country = location.findPath("country").asText();
			String street = location.findPath("street").asText();
			double latitude = location.findPath("latitude").asDouble();
			double longitude = location.findPath("longitude").asDouble();
			String zip = location.findPath("zip").asText();
			
			List<FacebookType> types = new ArrayList<FacebookType>();
			FacebookType type;
			for (JsonNode category : categories) {
				String type_id = category.get("id").asText();
				String type_name = category.get("name").asText();
				type = new FacebookType();
				type.setId(type_id);
				type.setType(type_name);
				types.add(type);
			}
			
			FacebookClient facebookClient = new PlaceFacebookClient();
			com.restfb.types.Page getPlace = null;
			Long likes = null;
			Integer checkin_count = null;
			String main_category = null,price_range = null;
			
			try {
				getPlace = 
						facebookClient.fetchObject(place_id , com.restfb.types.Page.class,Parameter.with("fields", "id,name,category,checkins,fan_count,price_range"));
				likes = getPlace.getFanCount();
				checkin_count = getPlace.getCheckins();
				main_category = getPlace.getCategory();
				price_range = getPlace.getPriceRange();
			} catch (Exception e) {
				System.out.println("Can't find page with place id " + place_id);
			}
			
			String goog_place_id = service.getPlaceByName(name + " " + street + " " + city + " " + country);
			
			try {
				MySqlDriver.saveCheckin(user_id,checkin_id,date_create_time,
										place_id,name,latitude,longitude,
										street,city,country,zip,main_category,checkin_count,likes,price_range,goog_place_id,types);
			} catch (Exception e) {
				System.out.println("Can't save checkin  - " + e.getMessage());
			}

		}
	}
	
	public Result recommandations() {
		JsonNode data = request().body().asJson();
		String userId = data.findPath("USER_ID").asText();
		Long time = data.findPath("TIME").asLong();
		String lng = data.findPath("LNG").asText();
		String lat = data.findPath("LAT").asText();
		JsonNode types = data.findPath("TYPES");
		
		return recommandations(userId, time, lng, lat, types);		
	}
	
	public Result getRecommandations(String userId, Long time, String longitude, String latitude) {
		
		JsonNode data = request().body().asJson();
		System.out.println(data);
		return recommandations(userId,time,longitude,latitude,null);
	}
	

	public Result recommandations(String userId, Long time, String longitude, String latitude, JsonNode types) {

		Location currentLocation = new Location(latitude, longitude);

		if (types == null) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				types = mapper.readTree("[{\"type\":\"Cafe\",\"count\":7}]");
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			} catch (IOException e) {
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
	
	public Result similarity(){
		try {
			MySqlDriver.calcSimilarities();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ok();
	}

	public Result index() {
		final JsonNode jsonResponse = Json.toJson("Your new application is ready.");
		return ok(jsonResponse);
	}
}
