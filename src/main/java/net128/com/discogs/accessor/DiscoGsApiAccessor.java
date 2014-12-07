package net128.com.discogs.accessor;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Properties;

import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DiscoGsApiAccessor {
	private final String configPropertiesFile="config.properties";
	private final String secretPropertiesFile="secret.properties";
	public static void main(String[] args) throws FileNotFoundException, IOException {
		new DiscoGsApiAccessor().run(args);
	}
	
	public void run(String[] args) throws FileNotFoundException, IOException {
		Properties configProps = new Properties();
		configProps.load(new FileInputStream(configPropertiesFile));
		configProps.load(new FileInputStream(secretPropertiesFile));

		System.setProperty("DiscoGS-User-Agent", configProps.getProperty("useragent.string"));

		// this requires user interaction if no access token is found in config.properties
		OAuthService authService = new ServiceBuilder()
			.provider(DiscoGSApi10a.class)
			.apiKey(configProps.getProperty("consumer.key"))
			.apiSecret(configProps.getProperty("consumer.secret"))
			.debug()
			.build();
		Token accessToken=getAccessToken(authService , configProps);
		
		Identity identity=getIdentity(authService, accessToken);
		System.out.println(identity.username);
		System.out.println(identity.resource_url);
	}

	private Identity getIdentity(final OAuthService authService, Token accessToken) throws JsonParseException, JsonMappingException, IOException {
		// ask for your identity
		OAuthRequest request = new OAuthRequest(Verb.GET, "http://api.discogs.com/oauth/identity");
		authService.signRequest(accessToken, request);
		Response response = request.send();
		String json=response.getBody();
		System.out.println(json);
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(json, Identity.class);
	}
	
	private Token getAccessToken(OAuthService authService , Properties configProps) throws IOException {
		// will be empty on the first run, as token is not yet provided
		String configToken = configProps.getProperty("accesstoken.key", "");
		String configSecret = configProps.getProperty("accesstoken.secret", "");

		Token accessToken;
		if (("".equals(configToken)) || ("".equals(configSecret))) {
			Token request_token = authService.getRequestToken();
			String authUrl = authService.getAuthorizationUrl(request_token);
			String code = getDiscoGSAuthCodeFromCommandline(authUrl);
			Verifier v = new Verifier(code);
			accessToken = authService.getAccessToken(request_token, v);

			// save access token to config.properties
			configProps.setProperty("accesstoken.key", accessToken.getToken());
			configProps.setProperty("accesstoken.secret", accessToken.getSecret());
			PrintWriter writer = new PrintWriter(secretPropertiesFile);
			configProps.store(writer, "modified by DiscoGSOAuthExample");
		} else {
			accessToken = new Token(configToken, configSecret);
		}
		return accessToken;
	}

	public static String getDiscoGSAuthCodeFromCommandline(String authUrl) {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String code = null;
		System.out.println("");
		System.out.println(">> Use a browser and navigate to: ");
		System.out.println(">> " + authUrl);
		System.out.println(">> to authorize this application for usage of your discogs-account");
		System.out.println("");
		System.out.println(">> Enter the authorization code and press return:");
		System.out.print(">> ");
		try {
			code = br.readLine();
		} catch (IOException ioe) {
			System.out.println("IO error while reading your authorization code");
		}
		return code;
	}

}