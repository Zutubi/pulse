package com.zutubi.pulse.master.notifications.email;

import com.zutubi.pulse.master.tove.config.admin.EmailConfiguration;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Testing email service that records the recipients of emails.
 */
public class RecordingEmailService implements EmailService
{
    private List<Collection<String>> recipientsByEmail = new LinkedList<Collection<String>>();

    public void sendMail(Collection<String> recipients, String subject, Multipart message, EmailConfiguration config) throws MessagingException
    {
        recipientsByEmail.add(recipients);
    }

    public Future<MessagingException> queueMail(Collection<String> recipients, String subject, Multipart message, EmailConfiguration config)
    {
        recipientsByEmail.add(recipients);
        return null;
    }

    public int getEmailCount()
    {
        return recipientsByEmail.size();
    }

    public Collection<String> getRecipientsForEmail(int index)
    {
        return recipientsByEmail.get(index);
    }
}
