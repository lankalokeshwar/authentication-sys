package com.example.Authentication_sys.consumer;

import com.example.Authentication_sys.dto.UserUpdatedEvent;
import com.example.Authentication_sys.model.User;
import com.example.Authentication_sys.repository.UserRepository;
import com.example.Authentication_sys.service.EmailService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageConsumers {

    private static final Logger log = LoggerFactory.getLogger(MessageConsumers.class);

    @Autowired private UserRepository userRepository;
    @Autowired private EmailService emailService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @SqsListener("${aws.sqs.dbUpdateQueueUrl}")
    public void onDbUpdateMessage(String body) throws Exception {
        UserUpdatedEvent event = parseEvent(body);
        log.info("DB update received for email: {}", event.getEmail());

        userRepository.findByEmail(event.getEmail()).ifPresent(user -> {
            user.setFirstName(event.getFirstName());
            user.setLastName(event.getLastName());
            user.setPhoneNumber(event.getPhoneNumber());

            if (event.getDateOfBirth() != null) user.setDateOfBirth(event.getDateOfBirth());
            if (event.getGender()      != null) user.setGender(event.getGender());

            userRepository.save(user);
            log.info("DB update completed for email: {}", event.getEmail());
        });
    }

    @SqsListener("${aws.sqs.emailQueueUrl}")
    public void onEmailMessage(String body) throws Exception {
        UserUpdatedEvent event = parseEvent(body);
        log.info("Email notification sending for email: {}", event.getEmail());

        User user = new User();
        user.setFirstName(event.getFirstName());
        user.setLastName(event.getLastName());
        user.setPhoneNumber(event.getPhoneNumber());
        user.setEmail(event.getEmail());
        user.setDateOfBirth(event.getDateOfBirth());
        user.setGender(event.getGender());

        emailService.sendProfileUpdateEmail(user);
    }

    /** SNS wraps the payload inside a "Message" field — unwrap before deserialising. */
    private UserUpdatedEvent parseEvent(String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);
        String inner = root.has("Message") ? root.get("Message").asText() : body;
        return objectMapper.readValue(inner, UserUpdatedEvent.class);
    }
}
