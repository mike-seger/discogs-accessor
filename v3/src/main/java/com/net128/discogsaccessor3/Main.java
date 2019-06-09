package com.net128.discogsaccessor3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.blizzed.discogsdb.DiscogsAuthData;
import ru.blizzed.discogsdb.DiscogsDBApi;
import ru.blizzed.discogsdb.DiscogsDBErrorException;
import ru.blizzed.discogsdb.model.Page;
import ru.blizzed.discogsdb.model.search.BaseSearchResult;
import ru.blizzed.discogsdb.params.DiscogsDBParams;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private final String secretPropertiesFile = "../secret.properties";
    public static void main(String [] args) throws IOException, DiscogsDBErrorException {
        new Main().run(args);
    }
    void run(String [] args) throws IOException, DiscogsDBErrorException {
        Properties configProps = new Properties();
        try (InputStream is = new FileInputStream(secretPropertiesFile)) {
            configProps.load(is);
        }
        DiscogsDBApi.initialize(new DiscogsAuthData(
            configProps.getProperty("consumer.key"),
            configProps.getProperty("consumer.secret")));
        Page<BaseSearchResult> page = DiscogsDBApi.searchArtist(
            DiscogsDBParams.QUERY.of("nostrum")
        ).execute();
        logger.info("Got: {} results", page.getContent().size());
        long kornId = page.getContent().get(0).getId();
    }

}
