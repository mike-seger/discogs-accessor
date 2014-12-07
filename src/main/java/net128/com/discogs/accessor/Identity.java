package net128.com.discogs.accessor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Identity {
	public String username;
	public String resource_url;
}
