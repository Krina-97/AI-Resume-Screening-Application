package com.airesume.screening.service;

import com.airesume.screening.entity.Candidate;
import com.airesume.screening.entity.InterviewStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    private final Environment environment;

    public EmailNotificationService(JavaMailSender mailSender, Environment environment) {
        this.mailSender = mailSender;
        this.environment = environment;
    }

    public void sendInterviewInvite(Candidate candidate, InterviewStatus interview) {
        String from = environment.getProperty("spring.mail.username");
        if (!StringUtils.hasText(from)) {
            log.info("Interview scheduled for {} but mail is not configured (spring.mail.username).", candidate.getEmail());
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(candidate.getEmail());
            message.setSubject("Interview scheduled");
            message.setText(buildBody(candidate, interview));
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Failed to send interview email: {}", ex.getMessage());
        }
    }

    private String buildBody(Candidate candidate, InterviewStatus interview) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hello ").append(candidate.getFullName()).append(",\n\n");
        sb.append("Your interview has been scheduled.\n");
        if (interview.getScheduledAt() != null) {
            sb.append("When: ").append(interview.getScheduledAt()).append("\n");
        }
        if (StringUtils.hasText(interview.getInterviewerName())) {
            sb.append("Interviewer: ").append(interview.getInterviewerName()).append("\n");
        }
        if (StringUtils.hasText(interview.getMeetingLink())) {
            sb.append("Meeting link: ").append(interview.getMeetingLink()).append("\n");
        }
        sb.append("\nRegards,\nHR Team");
        return sb.toString();
    }
}
