package com.spinningnoodle.mail.organizer.model;

import com.spinningnoodle.mail.organizer.service.email.FolderConfig;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.angus.mail.imap.IMAPFolder;
import org.eclipse.angus.mail.imap.IMAPMessage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.TextNode;
import org.springframework.util.StopWatch;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Builder
public record Email(String subject, String body, String sender, LocalDateTime date, Message message,
                    String messageId,
                    int messageNumber,
                    FolderConfig folderConfig) {

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


    public Email withBody() throws MessagingException, IOException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final var updated = new Email(subject, parse(message), sender, date, message, messageId, messageNumber,
                folderConfig);
        stopWatch.stop();
        return updated;
    }

    public void moveTo(String folderName) throws MessagingException {
        ((IMAPFolder) message.getFolder()).moveMessages(new Message[]{message},
                folderConfig.folderMap().get(folderName));
    }

    @Override
    public String toString() {
        return "From: " + sender + "\nSubject: " + subject + "\nDate: " + date + "\nBody:" + body;
    }

    private static String parse(Message message) throws MessagingException, IOException {
        if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < mimeMultipart.getCount(); i++) {
                BodyPart bodyPart2 = mimeMultipart.getBodyPart(i);
                String disposition = bodyPart2.getDisposition();
                if (disposition == null || disposition.equalsIgnoreCase(MimeBodyPart.INLINE)) {
                    builder.append(parse(bodyPart2.getContent().toString()));
                }
            }
            return builder.toString();
        } else {
            return parse(message.getContent().toString());
        }
    }

    private static String parse(String body) {
        Document doc = Jsoup.parse(body);
        return doc.nodeStream().filter(n -> n instanceof TextNode)
                .map(n -> ((TextNode) n).text())
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
    }
}
