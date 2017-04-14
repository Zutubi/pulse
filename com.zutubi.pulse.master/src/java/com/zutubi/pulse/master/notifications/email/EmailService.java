/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.notifications.email;

import com.zutubi.pulse.master.tove.config.admin.EmailConfiguration;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import java.util.Collection;
import java.util.concurrent.Future;

/**
 * Abstraction of a service that can send emails.
 */
public interface EmailService
{
    /**
     * Synchronously sends an email with the given details using the calling
     * thread.
     *
     * @param recipients   email addresses to send the email to (using the To:
     *                     header)
     * @param subject      subject line of the email
     * @param message      the email contents
     * @param config       configuration specifying how to send email
     * @throws MessagingException if there is an error sending the mail
     */
    void sendMail(final Collection<String> recipients, final String subject, final Multipart message, final EmailConfiguration config) throws MessagingException;

    /**
     * Queues a single email message with the given details using the given
     * configuration.  Emails are sent by a background thread asynchronously,
     * ensuring this call returns quickly regardless of network conditions.
     * <p/>
     * Note if the queue becomes too large some emails may be rejected with a
     * runtime exception.
     *
     * @param recipients   email addresses to send the email to (using the To:
     *                     header)
     * @param subject      subject line of the email
     * @param message      the email contents
     * @param config       configuration specifying how to send email
     *
     * @return a future that will yield a MessagingException if the message
     *         could not be sent, or null if it was sent successfully
     */
    Future<MessagingException> queueMail(Collection<String> recipients, String subject, Multipart message, final EmailConfiguration config);
}
