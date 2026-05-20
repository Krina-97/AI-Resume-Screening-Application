package com.airesume.screening.service;

import com.airesume.screening.config.AppProperties;
import com.airesume.screening.dto.LinkedInVerificationDto;
import com.airesume.screening.entity.Candidate;
import com.airesume.screening.entity.LinkedInVerificationLog;
import com.airesume.screening.exception.ApiException;
import com.airesume.screening.repository.CandidateRepository;
import com.airesume.screening.repository.LinkedInVerificationLogRepository;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.UUID;

@Service
@Slf4j
public class LinkedInAutomationService {

    private final AppProperties appProperties;
    private final CandidateRepository candidateRepository;
    private final LinkedInVerificationLogRepository logRepository;

    public LinkedInAutomationService(AppProperties appProperties,
                                   CandidateRepository candidateRepository,
                                   LinkedInVerificationLogRepository logRepository) {
        this.appProperties = appProperties;
        this.candidateRepository = candidateRepository;
        this.logRepository = logRepository;
    }

    public LinkedInVerificationDto verifyProfile(Long candidateId) {
        if (!appProperties.getSelenium().isEnabled()) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Selenium automation is disabled");
        }

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Candidate not found"));

        String query = StringUtils.hasText(candidate.getLinkedinUrl())
                ? candidate.getLinkedinUrl()
                : candidate.getFullName();

        if (!StringUtils.hasText(query)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Candidate has no name or LinkedIn URL to search");
        }

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        if (appProperties.getSelenium().isHeadless()) {
            options.addArguments("--headless=new", "--disable-gpu", "--window-size=1280,900");
        }
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(options);
        String screenshotPath = null;
        String headline = "";
        String profileUrl = "";
        String status = "SUCCESS";
        StringBuilder details = new StringBuilder();

        try {
            String searchUrl = "https://www.google.com/search?q=" + java.net.URLEncoder.encode(query + " site:linkedin.com/in", java.nio.charset.StandardCharsets.UTF_8);
            driver.get(searchUrl);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            wait.until(d -> d.findElements(By.cssSelector("div#search, div#rso, a")).size() > 0);

            var links = driver.findElements(By.cssSelector("a[href*='linkedin.com/in']"));
            if (!links.isEmpty()) {
                profileUrl = links.get(0).getAttribute("href");
                headline = links.get(0).getText();
            } else {
                status = "NOT_FOUND";
                details.append("No LinkedIn profile links detected in search results. ");
            }

            Path shotDir = Paths.get(appProperties.getSelenium().getScreenshotDir());
            Files.createDirectories(shotDir);
            Path shot = shotDir.resolve("linkedin-" + candidateId + "-" + UUID.randomUUID() + ".png");
            byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Files.write(shot, png);
            screenshotPath = shot.toAbsolutePath().toString();
            details.append("Screenshot saved. ");
        } catch (Exception ex) {
            log.error("LinkedIn verification failed", ex);
            status = "FAILED";
            details.append(ex.getMessage());
        } finally {
            driver.quit();
        }

        LinkedInVerificationLog entity = LinkedInVerificationLog.builder()
                .candidateId(candidateId)
                .searchQuery(query)
                .profileHeadline(headline)
                .profileUrl(profileUrl)
                .verificationStatus(status)
                .screenshotPath(screenshotPath)
                .logDetails(details.toString())
                .build();
        logRepository.save(entity);

        return LinkedInVerificationDto.builder()
                .id(entity.getId())
                .searchQuery(entity.getSearchQuery())
                .profileHeadline(entity.getProfileHeadline())
                .profileUrl(entity.getProfileUrl())
                .verificationStatus(entity.getVerificationStatus())
                .screenshotPath(entity.getScreenshotPath())
                .logDetails(entity.getLogDetails())
                .verifiedAt(entity.getVerifiedAt())
                .build();
    }

    public java.util.List<LinkedInVerificationDto> history(Long candidateId) {
        return logRepository.findByCandidateIdOrderByVerifiedAtDesc(candidateId).stream()
                .map(log -> LinkedInVerificationDto.builder()
                        .id(log.getId())
                        .searchQuery(log.getSearchQuery())
                        .profileHeadline(log.getProfileHeadline())
                        .profileUrl(log.getProfileUrl())
                        .verificationStatus(log.getVerificationStatus())
                        .screenshotPath(log.getScreenshotPath())
                        .logDetails(log.getLogDetails())
                        .verifiedAt(log.getVerifiedAt())
                        .build())
                .toList();
    }
}
