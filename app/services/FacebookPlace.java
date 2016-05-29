package services;

import com.restfb.Facebook;
import com.restfb.types.Place;

public class FacebookPlace extends Place {

	@Facebook("checkins")
	private Integer checkins;

	public Integer getCheckins() {
		return checkins;
	}

	public void setCheckins(Integer checkins) {
		this.checkins = checkins;
	} 
	
}
