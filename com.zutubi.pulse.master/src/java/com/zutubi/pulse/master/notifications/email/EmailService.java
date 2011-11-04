package com.zutubi.pulse.master.notifications.email;

import com.zutubi.pulse.master.tove.config.admin.EmailConfiguration;

import javax.mail.MessagingException;
import java.util.Collection;

/**
 * Abstraction of a service that can send emails.
 */
public interface EmailService
{
    /**
     * Sends a single email message with the given details using the given
     * configuration.
     *
     * @param recipients   email addresses to send the email to (using the To:
     *                     header)
     * @param subject      subject line of the email
     * @param mimeType     mime-type of the email, e.g. text/plain
     * @param message      the email contents
     * @param config       configuration specifying how to send email
     * @param reuseSession if true, reuse an existing session with equivalent
     *                     configuration if one is available, and leave it
     *                     connected for future reuse
     * @throws MessagingException if the message cannot be sent
     */
    void sendMail(Collection<String> recipients, String subject, String mimeType, String message, final EmailConfiguration config, boolean reuseSession) throws MessagingException;
}
