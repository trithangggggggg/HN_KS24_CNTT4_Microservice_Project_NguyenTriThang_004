package org.example.reservationservice.kafka;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationCreatedProducer {

    private static final Logger log = LoggerFactory.getLogger(ReservationCreatedProducer.class);
    private static final String TOPIC = "reservation-created";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendReservationCreated(String customerEmail) {
        kafkaTemplate.send(TOPIC, customerEmail);
        log.info("Sent reservation created event for customer email: {}", customerEmail);
    }
}
