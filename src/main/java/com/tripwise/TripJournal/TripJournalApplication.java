package com.tripwise.TripJournal;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TripJournalApplication {

    public static void main(String[] args) {
        SpringApplication.run(TripJournalApplication.class, args);
    }

    static {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        String[] envVars = {
                "PORT", "SPRING_APPLICATION_NAME",
                "SPRING_DATA_MONGODB_URI", "OAUTH2_ISSUER_URI",
                "JWT_AUDIENCE", "JWT_ALG", "MEDIA_BASE_URL",
                "LOG_ROOT_LEVEL", "LOG_APP_LEVEL", "OPENWEATHER_API_KEY",
                "SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI"
        };

        for (String key : envVars) {

            String value = dotenv.get(key);

            if (value != null) {
                System.setProperty(key, value); // Makes it accessible via System.getProperty
                System.out.println("✅ " + key + " loaded and set.");
            } else {
                System.out.println("⚠️" + key + " not found in env variables. Skipping System property.");
            }
        }
    }


}
