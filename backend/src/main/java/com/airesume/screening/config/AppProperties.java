package com.airesume.screening.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Jwt jwt = new Jwt();
    private final Upload upload = new Upload();
    private final Selenium selenium = new Selenium();
    private final OpenAi openai = new OpenAi();
    private final Gemini gemini = new Gemini();
    private String aiProvider = "openai";

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private long expirationMs = 86400000L;
    }

    @Getter
    @Setter
    public static class Upload {
        private String dir = "./uploads/resumes";
    }

    @Getter
    @Setter
    public static class Selenium {
        private boolean enabled = true;
        private boolean headless = true;
        private String screenshotDir = "./uploads/screenshots";
        private String linkedinBaseUrl = "https://www.linkedin.com";
    }

    @Getter
    @Setter
    public static class OpenAi {
        private String apiKey;
        private String model = "gpt-4o-mini";
        private String baseUrl = "https://api.openai.com/v1";
    }

    @Getter
    @Setter
    public static class Gemini {
        private String apiKey;
        private boolean enabled = false;
    }
}
