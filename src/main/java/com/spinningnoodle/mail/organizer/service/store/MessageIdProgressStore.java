package com.spinningnoodle.mail.organizer.service.store;

import com.spinningnoodle.mail.organizer.model.Email;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class MessageIdProgressStore implements ProgressStore {
    private final List<String> mailIds;

    public MessageIdProgressStore() throws IOException {
        Path path = Paths.get("progressStore.txt");
        if (path.toFile().exists()) {
            mailIds = new CopyOnWriteArrayList<>(Files.readAllLines(path));
        } else {
            mailIds = new CopyOnWriteArrayList<>();
        }
    }

    @Override
    public boolean hasBeenProcessed(Email email) {
        return mailIds.contains(email.messageId());
    }
    
    @SneakyThrows
    @Override
    public void process(Email email) {
        mailIds.add(email.messageId());
        save();
    }

    private void save() {

        try (FileWriter fileWriter = new FileWriter("progressStore.txt")) {
            for (String id : mailIds) {
                fileWriter.write(id + System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
