package com.spinningnoodle.mail.organizer.service;

import com.spinningnoodle.mail.organizer.classifier.Classifier;
import com.spinningnoodle.mail.organizer.model.Email;
import com.spinningnoodle.mail.organizer.service.email.ImapService;
import com.spinningnoodle.mail.organizer.service.store.MessageIdProgressStore;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class OrganizerService {
    private final ImapService emailService;
    private final Classifier classifier;
    private final MessageIdProgressStore progressStore;

    public OrganizerService(ImapService emailService, Classifier classifier, MessageIdProgressStore progressStore) {
        this.emailService = emailService;
        this.classifier = classifier;
        this.progressStore = progressStore;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() throws MessagingException {
        log.info("Started work");
        boolean completed = false;
        do {
            try {
                emailService.retrieve()
                        .filter(email -> !progressStore.hasBeenProcessed(email))
                        .onErrorContinue((throwable, o) -> {
                            log.warn("Error when reading mail {}", throwable.getMessage(), throwable);
                        })
//                        .parallel()
//                        .runOn(Schedulers.fromExecutor(Executors.newVirtualThreadPerTaskExecutor()))
                        .flatMap(e -> {

                            try {
                                if (classifier.needsBody()) {
                                    return Mono.just(e.withBody());
                                } else {
                                    return Mono.just(e);
                                }
                            } catch (MessagingException | IOException ex) {
                                // since it can't encode...
                                progressStore.process(e);
                                return Mono.error(ex);
                            }
                        })
//                        .sequential()
//                        .parallel(4)
//                        .runOn(Schedulers.boundedElastic())
                        .flatMap(mail -> classifier.classify(mail)
                                .map(c -> Tuples.of(mail, c)))
                        .doOnNext(tuples -> {
                            final var email = tuples.getT1();
                            final var mailType = tuples.getT2();
                            log.info("{} Message Type: {} date: {} from:{}  subject: {}",
                                    email.messageNumber(), mailType, email.date().toLocalDate(), email.sender(),
                                    email.subject());
                            try {
                                if (mailType.equals("Other")) {

                                } else {
                                    email.moveTo(mailType);
                                }
                                progressStore.process(email);
                            } catch (MessagingException e) {
                                progressStore.process(email);
                                // we can continue, but at least recycle connection?
                                throw new RuntimeException(e);
                            }
                        })
                        .then()
                        .doAfterTerminate(() -> {
                            log.info("Finished work");
                        })
                        .block();
                completed = true;
            } catch (Exception e) {
                log.error("Encountered an exception, continuing", e);
                emailService.reconnect();
            }
        } while (!completed);
    }
}
