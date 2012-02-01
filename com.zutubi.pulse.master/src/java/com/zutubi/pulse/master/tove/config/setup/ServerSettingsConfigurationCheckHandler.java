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
        emailService.sendMail(Arrays.asList(testAddress), "Test Email", message, configuration, false);
    }

    public void setEmailService(EmailService emailService)
    {
        this.emailService = emailService;
    }
}
