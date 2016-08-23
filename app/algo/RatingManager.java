package algo;

import model.Location;
import model.MySqlDriver;
import model.Place;

public class RatingManager {

	private Location location;
	private String userId;
	private static RatingManager instance;
	
	private RatingManager() {
	}
	
	public static RatingManager getInstance(){
		if (instance == null){
			instance = new RatingManager();
		}
		
		return instance;
	}
	
	public void init(Location location, String userId){
		this.location = location;
		this.userId = userId;
	}

	public double getRate(Place place) {

		double rate = 1;
		// addition features.
		rate += (place.getGoogleRating() * RatingCons.GOOGLE_RATING);
		rate += (place.getCheckins() * RatingCons.CHECKINS);
		
		// Multiply features
		double distance = DistanceCalculator.distance(place.getLocation(),this.location);
		place.setDistance(distance); 
		rate *= (1/Math.pow(distance, 1.3));
		rate *= (1/Math.pow(3, MySqlDriver.getDislikeCountByPlace(place.getPlaceId(), userId)));
		//rate *= 0.3 *  MySqlDriver.getInstance().getDislikeCountByGoogType(place.get(), userId);  
		return rate;
	}

}
