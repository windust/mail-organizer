package com.spinningnoodle.mail.organizer.service.email;

import com.spinningnoodle.mail.organizer.model.ClassifierConfiguration;
import com.spinningnoodle.mail.organizer.model.Email;
import jakarta.mail.*;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.angus.mail.imap.IMAPFolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Properties;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ImapService {
    final ImapConfiguration configuration;
    private final ClassifierConfiguration classifierConfiguration;
    private volatile Store store;
    private volatile Folder emailFolder;
    private volatile FolderConfig folderConfig;

    public ImapService(ImapConfiguration configuration, ClassifierConfiguration classifierConfiguration) throws MessagingException {
        this.configuration = configuration;
        store = connect(configuration);
        emailFolder = store.getFolder("INBOX");
        emailFolder.open(Folder.READ_WRITE);
        folderConfig = retrieveConfig(emailFolder, classifierConfiguration);
        this.classifierConfiguration = classifierConfiguration;
    }

    public int count() throws MessagingException {
        return emailFolder.getMessageCount();
    }
    
    public Flux<Email> retrieve() throws MessagingException {

        int batchSize = 1000; // Define your preferred batch size
        int totalMessages = emailFolder.getMessageCount();
        return Flux.range(1, (totalMessages + batchSize - 1) / batchSize)
                .concatMap(batch -> {
                    int start = (batch - 1) * batchSize + 1;
                    int end = Math.min(batch * batchSize, totalMessages);
                    try {
                        return retrieve(start, end);
                    } catch (MessagingException e) {
                        return Flux.error(e);
                    }
                });
    }
    public Flux<Email> retrieve(int start, int end) throws MessagingException {
        return Flux.create(sink -> {
            try {
                log.info("Fetching messages from {} to {}", start, end);
                Message[] messages = emailFolder.getMessages(start, end);
                FetchProfile fp = new FetchProfile();
                fp.add(FetchProfile.Item.ENVELOPE);
                fp.add(FetchProfile.Item.CONTENT_INFO);
                fp.add(IMAPFolder.FetchProfileItem.FLAGS);
                fp.add(UIDFolder.FetchProfileItem.UID);
                fp.add(IMAPFolder.FetchProfileItem.INTERNALDATE);
                emailFolder.fetch(messages, fp);
                log.info("Starting sinking messages");
                for (Message message : messages) {
                    sink.next(Email.from(message, folderConfig));
                }
                sink.complete();
                log.info("Finished sinking messages");
            } catch (MessagingException | IOException e) {
                sink.error(e);
            }
        });
    }

    private static Store connect(ImapConfiguration imapConfiguration) throws MessagingException {

        // Set mail properties
        Properties properties = new Properties();
        properties.put("mail.store.protocol", "imap");
        properties.put("mail.imap.host", imapConfiguration.getHost());
        properties.put("mail.imap.port", imapConfiguration.getPort());
        properties.put("mail.imap.ssl.enable", imapConfiguration.isSsl());

        // Create a new session
        Session session = Session.getDefaultInstance(properties, null);

        // Connect to the store
        Store store = session.getStore("imap");
        store.connect(imapConfiguration.getUsername(), imapConfiguration.getPassword());
        return store;
    }

    private FolderConfig retrieveConfig(Folder emailFolder, ClassifierConfiguration classifierConfiguration) throws MessagingException {
        // spam
        final var builder = FolderConfig.builder();
        final var map = classifierConfiguration.getDescriptions().keySet().stream().collect(Collectors.toMap(v -> v, v -> {
            try {
                return ensureFolder(v, emailFolder);
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        }));
        return new FolderConfig(map);
    }

    private IMAPFolder ensureFolder(String folderName, Folder inbox) throws MessagingException {
        // create.
        Folder folder = inbox.getFolder(folderName);
        var created = folder.create(Folder.HOLDS_MESSAGES);
        log.info("created folder {}: {}", folder, created);

        return (IMAPFolder) folder;
    }

    public void reconnect() throws MessagingException {
        store.close();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        store = connect(configuration);
        emailFolder = store.getFolder("INBOX");
        emailFolder.open(Folder.READ_WRITE);
        folderConfig = retrieveConfig(emailFolder, classifierConfiguration);
    }
}
