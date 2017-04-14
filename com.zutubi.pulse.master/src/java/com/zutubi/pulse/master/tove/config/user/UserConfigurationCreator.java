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

package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.tove.config.user.contacts.EmailContactConfiguration;
import com.zutubi.tove.annotations.*;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.util.RandomUtils;
import com.zutubi.util.StringUtils;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.annotations.Email;
import com.zutubi.validation.annotations.Required;
import org.springframework.security.authentication.encoding.PasswordEncoder;

/**
 * Helper type for creating users.
 */
@SymbolicName("zutubi.userConfigCreator")
@Form(fieldOrder = {"login", "name", "emailAddress", "authenticatedViaLdap", "password", "confirmPassword"})
@Wire
public class UserConfigurationCreator extends AbstractConfiguration implements Validateable
{
    private static final Messages I18N = Messages.getInstance(UserConfiguration.class);

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
        if (authenticatedViaLdap)
        {
            user.setAuthenticatedViaLdap(true);
            user.setPassword(passwordEncoder.encodePassword(RandomUtils.secureRandomString(10), null));
        } else
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
        if (!authenticatedViaLdap)
        {
            if (password != null && confirmPassword != null)
            {
                if (!password.equals(confirmPassword))
                {
                    context.addFieldError("password", I18N.format("passwords.differ"));
                }
            }
        }
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder)
    {
        this.passwordEncoder = passwordEncoder;
    }
}
