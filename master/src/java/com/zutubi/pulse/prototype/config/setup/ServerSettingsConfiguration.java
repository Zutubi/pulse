package com.zutubi.pulse.prototype.config.setup;

import com.opensymphony.util.TextUtils;
import com.zutubi.config.annotations.ConfigurationCheck;
import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.Password;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.ControllingCheckbox;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.annotations.Email;
import com.zutubi.validation.annotations.Required;
import com.zutubi.validation.annotations.Url;
import com.zutubi.validation.validators.EmailValidator;
import com.zutubi.pulse.core.config.AbstractConfiguration;

/**
 *
 *
 */
@SymbolicName("internal.serverSettingsConfig")
@ConfigurationCheck("ServerSettingsConfigurationCheckHandler")
@Form(fieldOrder = {"baseUrl", "host", "ssl", "from", "username", "password", "subjectPrefix", "customPort", "port"})
public class ServerSettingsConfiguration extends AbstractConfiguration implements Validateable
{
    @Url @Required
    private String baseUrl;
    private String host;
    private boolean ssl;
    @Email
    private String from;
    @ControllingCheckbox(dependentFields = {"port"})
    private boolean customPort;
    private int port = 25;
    private String username;
    @Password
    private String password;
    private String subjectPrefix;

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

    public String getFrom()
    {
        return from;
    }

    public void setFrom(String from)
    {
        this.from = from;
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
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

    public boolean getSsl()
    {
        return ssl;
    }

    public void setSsl(boolean ssl)
    {
        this.ssl = ssl;
    }

    public boolean getCustomPort()
    {
        return customPort;
    }

    public void setCustomPort(boolean customPort)
    {
        this.customPort = customPort;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public void validate(ValidationContext context)
    {
        if (TextUtils.stringSet(host))
        {
            if (!TextUtils.stringSet(from))
            {
                context.addFieldError("from", "from address is required when smtp host is provided");
            }
        }

        // If the from address is specified, then ensure that a valid value is set.
        if (TextUtils.stringSet(from))
        {
            EmailValidator validator = new EmailValidator();
            validator.setValidationContext(context);
            validator.setFieldName("from");
            try
            {
                validator.validate(this);
            }
            catch (ValidationException e)
            {
                context.addFieldError("from", e.getMessage());
            }
        }
    }

    public String getSubjectPrefix()
    {
        return subjectPrefix;
    }

    public void setSubjectPrefix(String subjectPrefix)
    {
        this.subjectPrefix = subjectPrefix;
    }
}
