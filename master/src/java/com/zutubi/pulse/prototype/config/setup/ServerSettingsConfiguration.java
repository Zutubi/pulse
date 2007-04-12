package com.zutubi.pulse.prototype.config.setup;

import com.opensymphony.util.TextUtils;
import com.zutubi.prototype.annotation.ConfigurationCheck;
import com.zutubi.prototype.annotation.Form;
import com.zutubi.prototype.annotation.Password;
import com.zutubi.pulse.prototype.record.SymbolicName;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.annotations.Required;
import com.zutubi.validation.annotations.Url;
import com.zutubi.validation.annotations.Email;
import com.zutubi.validation.validators.EmailValidator;

/**
 *
 *
 */
@SymbolicName("internal.serverSettingsConfig")
@ConfigurationCheck(ServerSettingsConfigurationCheckHandler.class)
@Form(fieldOrder = {"baseUrl", "smtpHost", "smtpSSL", "fromAddress", "username", "password", "prefix", "smtpCustomPort", "smtpPort"})
public class ServerSettingsConfiguration implements Validateable
{
    @Url @Required
    private String baseUrl;
    
    @Email @Required
    private String fromAddress;
    private String smtpHost;
    private boolean smtpSSL;
    private boolean smtpCustomPort;
    private int smtpPort = 25;
    private String username;
    
    @Password
    private String password;
    private String prefix;

/*
    public void initialise()
    {
        SystemConfigurationSupport systemConfig = (SystemConfigurationSupport) configurationManager.getSystemConfig();
        baseUrl = systemConfig.getHostUrl();
    }
*/

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    public String getFromAddress()
    {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress)
    {
        this.fromAddress = fromAddress;
    }

    public String getSmtpHost()
    {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost)
    {
        this.smtpHost = smtpHost;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public boolean getSmtpSSL()
    {
        return smtpSSL;
    }

    public void setSmtpSSL(boolean smtpSSL)
    {
        this.smtpSSL = smtpSSL;
    }

    public boolean getSmtpCustomPort()
    {
        return smtpCustomPort;
    }

    public void setSmtpCustomPort(boolean smtpCustomPort)
    {
        this.smtpCustomPort = smtpCustomPort;
    }

    public int getSmtpPort()
    {
        return smtpPort;
    }

    public void setSmtpPort(int smtpPort)
    {
        this.smtpPort = smtpPort;
    }

    public void validate(ValidationContext context)
    {
        if (TextUtils.stringSet(smtpHost))
        {
            if (!TextUtils.stringSet(fromAddress))
            {
                context.addFieldError("fromAddress", "from address is required when smtp host is provided");
            }
        }

        // If the from address is specified, then ensure that a valid value is set.
        if (TextUtils.stringSet(fromAddress))
        {
            EmailValidator validator = new EmailValidator();
            validator.setValidationContext(context);
            validator.setFieldName("fromAddress");
            try
            {
                validator.validate(this);
            }
            catch (ValidationException e)
            {
                context.addFieldError("fromAddress", e.getMessage());
            }
        }
    }

    public String getPrefix()
    {
        return prefix;
    }

    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }
}
