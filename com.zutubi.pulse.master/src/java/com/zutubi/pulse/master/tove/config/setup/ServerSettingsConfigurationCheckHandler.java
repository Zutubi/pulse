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

package com.zutubi.pulse.master.tove.config.setup;

import com.zutubi.pulse.master.notifications.email.EmailService;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wire;
import com.zutubi.tove.config.api.AbstractConfigurationCheckHandler;
import com.zutubi.validation.annotations.Required;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.util.Arrays;

/**
 * Server settings page of the setup wizard.
 */
@SymbolicName("zutubi.serverSettingsConfigurationCheckHandler")
@Wire
public class ServerSettingsConfigurationCheckHandler extends AbstractConfigurationCheckHandler<ServerSettingsConfiguration>
{
    @Required
    private String testAddress;

    private EmailService emailService;

    public String getTestAddress()
    {
        return testAddress;
    }

    public void setTestAddress(String testAddress)
    {
        this.testAddress = testAddress;
    }

    public void test(ServerSettingsConfiguration configuration) throws MessagingException
    {
        MimeMultipart message = new MimeMultipart();
        MimeBodyPart bodyPart = new MimeBodyPart();
        bodyPart.setContent("Welcome to Zutubi Pulse!", "text/plain");
        message.addBodyPart(bodyPart);
        emailService.sendMail(Arrays.asList(testAddress), "Test Email", message, configuration);
    }

    public void setEmailService(EmailService emailService)
    {
        this.emailService = emailService;
    }
}
