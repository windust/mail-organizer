package com.spinningnoodle.mail.organizer.service.store;

import com.spinningnoodle.mail.organizer.model.Email;

public interface ProgressStore {
    boolean hasBeenProcessed(Email email);
    void process(Email email);

}
