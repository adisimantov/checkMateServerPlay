package services;

import com.restfb.Version;

public class PlaceFacebookClient extends LoggedInFacebookClient {
	
	public PlaceFacebookClient() {
        super();
        this.apiVersion = Version.LATEST;
	}

}
