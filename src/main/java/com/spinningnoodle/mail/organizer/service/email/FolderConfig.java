package com.spinningnoodle.mail.organizer.service.email;

import lombok.Builder;
import org.eclipse.angus.mail.imap.IMAPFolder;

import java.util.Map;

@Builder
public record FolderConfig(Map<String, IMAPFolder> folderMap) {
}
