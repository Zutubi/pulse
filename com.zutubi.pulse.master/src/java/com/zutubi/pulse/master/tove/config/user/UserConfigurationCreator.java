package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.pulse.master.tove.config.user.contacts.EmailContactConfiguration;
import com.zutubi.tove.annotations.*;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.tove.config.api.ConfigurationCreator;
import com.zutubi.util.RandomUtils;
import com.zutubi.util.StringUtils;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.annotations.Email;
import com.zutubi.validation.annotations.Required;
import org.acegisecurity.providers.encoding.PasswordEncoder;

/**
 * Used when creating new users.  Handles the confirmation of the user's
 * password.
 */
@SymbolicName("zutubi.userConfigCreator")
@Form(fieldOrder = {"login", "name", "emailAddress", "authenticatedViaLdap", "password", "confirmPassword"})
@Wire
public class UserConfigurationCreator extends AbstractConfiguration implements ConfigurationCreator<UserConfiguration>, Validateable
{
    public static final String CONTACT_NAME = "primary email";

    @ID
    private String login;
    @Required
    private String name;
    @Email
    private String emailAddress;
    @ControllingCheckbox(uncheckedFields = {"password", "confirmPassword"})
    private boolean authenticatedViaLdap;
    @Password
    private String password;
    @Password
    private String confirmPassword;
    @Transient
    private PasswordEncoder passwordEncoder;

    public UserConfiguration create()
    {
        UserConfiguration user = new UserConfiguration(login, name);
        if(authenticatedViaLdap)
        {
            user.setAuthenticatedViaLdap(true);
            user.setPassword(passwordEncoder.encodePassword(RandomUtils.randomToken(10), null));
        }
        else
        {
            user.setPassword(passwordEncoder.encodePassword(password, null));
        }

        if (StringUtils.stringSet(emailAddress))
        {
            EmailContactConfiguration emailContact = new EmailContactConfiguration(CONTACT_NAME, emailAddress);
            emailContact.setPrimary(true);
            user.getPreferences().addContact(emailContact);
        }
        return user;
    }

    public String getLogin()
    {
        return login;
    }

    public void setLogin(String login)
    {
        this.login = login;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getEmailAddress()
    {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress)
    {
        this.emailAddress = emailAddress;
    }

    public boolean isAuthenticatedViaLdap()
    {
        return authenticatedViaLdap;
    }

    public void setAuthenticatedViaLdap(boolean authenticatedViaLdap)
    {
        this.authenticatedViaLdap = authenticatedViaLdap;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getConfirmPassword()
    {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword)
    {
        this.confirmPassword = confirmPassword;
    }

    public void validate(ValidationContext context)
    {
        if(!authenticatedViaLdap)
        {
            if(password != null && confirmPassword != null)
            {
                if(!password.equals(confirmPassword))
                {
                    context.addFieldError("password", "passwords do not match");
                }
            }
        }
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder)
    {
        this.passwordEncoder = passwordEncoder;
    }
}
