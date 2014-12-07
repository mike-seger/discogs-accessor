package net128.com.discogs.accessor;

import org.scribe.builder.api.DefaultApi10a;
import org.scribe.model.Token;
 
public class DiscoGSApi10a extends DefaultApi10a {
	private final String AUTHORIZE_URL = "http://www.discogs.com/oauth/authorize?oauth_token=%s";
	private final String REQUEST_TOKEN_URL = "http://api.discogs.com/oauth/request_token";
	private final String ACCESS_TOKEN_URL = "http://api.discogs.com/oauth/access_token";
    
	public String getAccessTokenEndpoint(){
		return this. ACCESS_TOKEN_URL;
	}
	
	public String getAuthorizationUrl(Token request_token){
		return String.format(this.AUTHORIZE_URL, request_token.getToken());
	}
	
	public String getRequestTokenEndpoint(){
		return this.REQUEST_TOKEN_URL;
	}
}