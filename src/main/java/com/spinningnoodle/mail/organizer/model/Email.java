package com.spinningnoodle.mail.organizer.model;

import com.spinningnoodle.mail.organizer.service.email.FolderConfig;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.angus.mail.imap.IMAPFolder;
import org.eclipse.angus.mail.imap.IMAPInputStream;
import org.eclipse.angus.mail.imap.IMAPMessage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.TextNode;
import org.springframework.util.StopWatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Builder
public record Email(String subject, String body, String sender, LocalDateTime date, Message message,
                    String messageId,
                    int messageNumber,
                    FolderConfig folderConfig) {

    /**
     * Creates an Email object from a Message object.
     *
     * @param message the Message object
     * @param folderConfig the FolderConfig object
     * @return the Email object
     * @throws MessagingException if there is an error accessing the message
     * @throws IOException if there is an error reading the message content
     */
    public static Email from(Message message, FolderConfig folderConfig) throws MessagingException, IOException {
        final var builder = Email.builder();
        builder.messageId(((IMAPMessage) message).getMessageID());
        builder.message(message);
        builder.messageNumber(message.getMessageNumber());
        builder.date(LocalDateTime.ofInstant(message.getReceivedDate().toInstant(), ZoneId.systemDefault()));
        if (message.getFrom() != null && message.getFrom().length > 0) {
            builder.sender(((InternetAddress) message.getFrom()[0]).getAddress());
        } else {
            builder.sender("unknown");
        }
        builder.subject(message.getSubject());
        builder.folderConfig(folderConfig);

        return builder.build();
    }

    /**
     * Returns a new Email object with the body content parsed.
     *
     * @return the updated Email object
     * @throws MessagingException if there is an error accessing the message
     * @throws IOException if there is an error reading the message content
     */
    public Email withBody() throws MessagingException, IOException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final var updated = new Email(subject, parse(message), sender, date, message, messageId, messageNumber, folderConfig);
        stopWatch.stop();
        return updated;
    }

    /**
     * Moves the email to the specified folder.
     *
     * @param folderName the name of the folder to move the email to
     * @throws MessagingException if there is an error moving the message
     */
    public void moveTo(String folderName) throws MessagingException {
        ((IMAPFolder) message.getFolder()).moveMessages(new Message[]{message}, folderConfig.folderMap().get(folderName));
    }

    @Override
    public String toString() {
        return "From: " + sender + "\nSubject: " + subject + "\nDate: " + date + "\nBody:" + body;
    }

    /**
     * Parses the content of a Message object.
     *
     * @param message the Message object
     * @return the parsed content as a String
     * @throws MessagingException if there is an error accessing the message
     * @throws IOException if there is an error reading the message content
     */
    private static String parse(Message message) throws MessagingException, IOException {
        if (message.isMimeType("multipart/*")) {
            Object content = message.getContent();
            StringBuilder builder = new StringBuilder();
            if (content instanceof Multipart) {
                MimeMultipart mimeMultipart = (MimeMultipart) content;
                for (int i = 0; i < mimeMultipart.getCount(); i++) {
                    BodyPart bodyPart2 = mimeMultipart.getBodyPart(i);
                    String disposition = bodyPart2.getDisposition();
                    if (disposition == null || disposition.equalsIgnoreCase(MimeBodyPart.INLINE)) {
                        builder.append(parse(bodyPart2.getContent().toString()));
                    }
                }
            } else if (content instanceof IMAPInputStream imapInputStream) {
                try (BufferedReader reader =
                             new BufferedReader(
                                     new InputStreamReader(imapInputStream))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line).append("\n");
                    }
                }
                return builder.toString();
            }
            return builder.toString();
        } else {
            return parse(message.getContent().toString());
        }
    }

    /**
     * Parses the body content of an email.
     *
     * @param body the body content as a String
     * @return the parsed content as a String
     */
    private static String parse(String body) {
        Document doc = Jsoup.parse(body);
        return doc.nodeStream().filter(n -> n instanceof TextNode)
                .map(n -> ((TextNode) n).text())
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
    }
}