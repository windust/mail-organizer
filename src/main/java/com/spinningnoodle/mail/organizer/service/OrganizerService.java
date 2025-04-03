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
                        .flatMap(mail ->
                                classifier
                                        .classify(mail)
                                        .map(c -> new EmailWithCategory(mail, c)))
                        .doOnNext(emailWithCategory -> {
                            final var email = emailWithCategory.email;
                            final var category = emailWithCategory.category;
                            log.info("{} Category: {} date: {} from:{}  subject: {}",
                                    email.messageNumber(), category, email.date().toLocalDate(), email.sender(),
                                    email.subject());
                            if (!classifier.dryRun()) {
                                try {
                                    if (!category.equals("Other")) {
                                        email.moveTo(category);
                                    }
                                    progressStore.process(email);
                                } catch (MessagingException e) {
                                    progressStore.process(email);
                                    // we can continue, but at least recycle connection?
                                    throw new RuntimeException(e);
                                }
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

    private record EmailWithCategory(Email email, String category) {}
}
