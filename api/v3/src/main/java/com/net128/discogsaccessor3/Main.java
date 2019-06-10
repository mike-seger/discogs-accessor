package com.net128.discogsaccessor3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.blizzed.discogsdb.DiscogsAuthData;
import ru.blizzed.discogsdb.DiscogsDBApi;
import ru.blizzed.discogsdb.DiscogsDBErrorException;
import ru.blizzed.discogsdb.model.SearchPage;
import ru.blizzed.discogsdb.params.DiscogsDBParams;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private final String secretPropertiesFile = "../../secret.properties";
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
        SP [] sps= new SP [] {
            new SP("nostrum", "baby"),
            new SP("nostrum", "monastery"),
        };
        for(SP sp : sps) {
            SearchPage searchPage = DiscogsDBApi.search(
                DiscogsDBParams.ARTIST.of(sp.artist),
                DiscogsDBParams.TRACK.of(sp.title)
            ).execute();
            logger.info("Search: {} results", searchPage.getResults().size());
            searchPage.getAllReleases().stream().forEach(r ->
                System.out.println(r.getTitle()+" / "+r.getYear()+ " / "+r.getLabels()));
        }
    }

    class SP {
        String artist;
        String title;

        public SP(String artist, String title) {
            this.artist = artist;
            this.title = title;
        }
    }
}
