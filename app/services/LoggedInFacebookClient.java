package services;

import com.restfb.DefaultFacebookClient;

public class LoggedInFacebookClient extends DefaultFacebookClient {

	protected final String APP_ID = "979217132144477";
	protected final String APP_SECRET = "396af3128055eb4fed59639d10d5e99c" ;
	
    public LoggedInFacebookClient() {
        AccessToken accessToken = this.obtainAppAccessToken(APP_ID, APP_SECRET);
        this.accessToken = accessToken.getAccessToken();
    }

}