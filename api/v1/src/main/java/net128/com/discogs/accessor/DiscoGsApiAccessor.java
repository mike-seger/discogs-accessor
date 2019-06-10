package net128.com.discogs.accessor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.*;
import org.scribe.oauth.OAuthService;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

public class DiscoGsApiAccessor {
	private final String configPropertiesLocation = "/config.properties";
	private final String secretPropertiesFile = "../../secret.properties";
	private final String prompt="Please enter search query (Artist;Title): ";
	
	private boolean debug=true;

	public static void main(String[] args) throws IOException {
		new DiscoGsApiAccessor().run(args);
	}

	void run(String[] args) throws IOException {
		if(args.length>0 && "d".equals(args[0])) {
			debug=false;
		}
		Properties configProps = new Properties();
		try (InputStream is=getClass().getResourceAsStream(configPropertiesLocation)) {
			configProps.load(is);
		}
		try (InputStream is=new FileInputStream(secretPropertiesFile)) {
			configProps.load(is);
		}
		System.setProperty("DiscoGS-User-Agent", configProps.getProperty("useragent.string"));

		// this requires user interaction if no access token is found in
		// config.properties
		OAuthService authService = new ServiceBuilder().provider(DiscoGSApi10a.class).apiKey(configProps.getProperty("consumer.key"))
			.apiSecret(configProps.getProperty("consumer.secret")).debug().build();
		Token accessToken = getAccessToken(authService, configProps);

		Identity identity = getIdentity(authService, accessToken);
		debugMessage(identity.toString());

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String query;
		debugMessage(prompt);
		while((query=br.readLine()) != null) {
			try {
				String [] tokens=query.split(" - ");
				if(tokens.length==2) {
					List<SearchResult> searchResults = getSearchResult(authService, accessToken,tokens[0], tokens[1]);
					String firstYear=findFirstYear(searchResults);
					System.out.println(">>>>> "+firstYear+"\t"+query);
				} else {
					System.err.println("Invalid search!");
				}
				debugMessage(prompt);
				Thread.sleep(1000);
			} catch(Exception e) {
				System.err.println(e.getMessage());
			}
		}
	}

	private List<SearchResult> getSearchResult(final OAuthService authService, Token accessToken, String artist, String title)
			throws IOException {
		String theArtist=URLEncoder.encode(artist, StandardCharsets.UTF_8.name());
		//String theTitle=URLEncoder.encode("track:\""+title+"\"", StandardCharsets.UTF_8.name());
		String theTitle=URLEncoder.encode(title.replaceAll("[^A-Za-z]", " ").replaceAll("  *", " ").trim(), StandardCharsets.UTF_8.name());
		String url="http://api.discogs.com/database/search?type=release&per_page=10&artist="+theArtist+"&q="+theTitle;
		OAuthRequest request = new OAuthRequest(Verb.GET, url);
		authService.signRequest(accessToken, request);
		Response response = request.send();
		String json = response.getBody();
		debugMessage(json);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(json);
		node = node.get("results");
		TypeReference<List<SearchResult>> typeRef = new TypeReference<List<SearchResult>>() {};
		return mapper.readValue(node.traverse(), typeRef);
	}
	
	private String findFirstYear(List<SearchResult> searchResults) {
		String firstYear=null;
		for(SearchResult searchResult : searchResults) {
			if(searchResult.year!=null) {
				if(firstYear==null || searchResult.year.compareTo(firstYear)<0) {
					firstYear=searchResult.year;
				}
			}
		}
		return firstYear;
	}

	private Identity getIdentity(final OAuthService authService, Token accessToken) throws  IOException {
		OAuthRequest request = new OAuthRequest(Verb.GET, "http://api.discogs.com/oauth/identity");
		authService.signRequest(accessToken, request);
		Response response = request.send();
		String json = response.getBody();
		debugMessage(json);
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(json, Identity.class);
	}

	private Token getAccessToken(OAuthService authService, Properties configProps) throws IOException {
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

	private String getDiscoGSAuthCodeFromCommandline(String authUrl) {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String code = null;
		System.out.println();
		System.out.println(">> Use a browser and navigate to: ");
		System.out.println(">> " + authUrl);
		System.out.println(">> to authorize this application for usage of your discogs-account");
		System.out.println();
		System.out.println(">> Enter the authorization code and press return:");
		System.out.print(">> ");
		try {
			code = br.readLine();
		} catch (IOException ioe) {
			System.out.println("IO error while reading your authorization code");
		}
		return code;
	}

	private void debugMessage(String message) {
		if(debug) System.out.print(message);
	}
}