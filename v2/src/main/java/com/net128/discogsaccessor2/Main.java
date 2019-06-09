package com.net128.discogsaccessor2;

import nl.hypothermic.javacogs.Javacogs;
import nl.hypothermic.javacogs.SearchBuilder;
import nl.hypothermic.javacogs.authentication.KeySecretAuthenticationMethod;
import nl.hypothermic.javacogs.entities.*;
import nl.hypothermic.javacogs.handlers.Handler;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Main {
    private final String secretPropertiesFile = "../secret.properties";
    public static void main(String [] args) throws IOException {
        new Main().run(args);
    }
    void run(String [] args) throws IOException {
        Properties configProps = new Properties();
        try (InputStream is=new FileInputStream(secretPropertiesFile)) {
            configProps.load(is);
        }
        Javacogs.getInstance().setAuthenticationMethod(
                new KeySecretAuthenticationMethod(
                    configProps.getProperty("consumer.key"),
                    configProps.getProperty("consumer.secret"))
        );

        Javacogs.getInstance().getHandler(Handler.DATABASE).getEntitiesBySearch(
                new SearchBuilder().setQuery("Flip de Vogelaar"),
            response -> {
                if (response.hasSucceeded()) {
                    int i=0;
                    for (SearchResult result : response.getValue()) {
                        try {
                            Javacogs.getInstance().getHandler(Handler.DATABASE).getEntityFromSearchResult(result,
                            response1 -> {

                                Entity e = response1.getValue();

                                // Cast the Entity into a superclass
                                if(e!=null) {
                                    System.out.print(e.getClass().getName() + ": ");
                                    if (e instanceof ArtistGroup) {
                                        System.out.print(((ArtistGroup) e).toString());
                                    } else if (e instanceof ArtistMember) {
                                        System.out.print(((ArtistMember) e).toString());
                                    } else if (e instanceof Label) {
                                        System.out.print(((Label) e).toString());
                                    } else if (e instanceof Master) {
                                        System.out.print(((Master) e).toString());
                                    } else if (e instanceof Release) {
                                        System.out.print(((Release) e).toString());
                                    }
                                    System.out.println();
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.print("RES: "+i++);
                    }
                } else {
                    System.out.println("Response failed");
                }
            });
    }
}
