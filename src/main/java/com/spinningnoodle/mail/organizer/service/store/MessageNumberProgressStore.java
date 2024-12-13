package com.spinningnoodle.mail.organizer.service.store;

import com.spinningnoodle.mail.organizer.model.Email;
import com.spinningnoodle.mail.organizer.service.email.ImapConfiguration;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MessageNumberProgressStore implements ProgressStore {
    ImapConfiguration configuration;
    volatile int currentMessageId;
    String storeName;
    public MessageNumberProgressStore(ImapConfiguration configuration) {
        this.configuration = configuration;
        // setup.
        this.storeName = configuration.getUsername()+"._progressStore.txt";
        currentMessageId = readStore(storeName);
    }

    private int readStore(String storeName) {

        try {
            Path path = Path.of(storeName);
            if (Files.exists(path)) {
                return Integer.parseInt(Files.readString(path).strip());
            } else {
                return 0; // Default value if file does not exist
            }
        } catch (IOException | NumberFormatException e) {
            throw new IllegalStateException("Failed to read the progress store file.", e);
        }
    }


    @Override
    public boolean hasBeenProcessed(Email email) {
        return false;
    }

    @Override
    public void process(Email email) {

    }
}
