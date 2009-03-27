package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.security.AcegiUser;
import com.zutubi.pulse.master.security.AcegiUtils;
import com.zutubi.tove.annotations.*;
import com.zutubi.tove.security.Actor;
import com.zutubi.validation.ValidationContext;
import org.acegisecurity.providers.encoding.PasswordEncoder;

/**
 * Transient configuration type used by a user to change their own password.
 */
@SymbolicName("zutubi.setOwnPasswordConfig")
@Form(fieldOrder = {"currentPassword", "password", "confirmPassword"})
@Wire
public class SetOwnPasswordConfiguration extends SetPasswordConfiguration
{
    private static final Messages I18N = Messages.getInstance(SetOwnPasswordConfiguration.class);

    @Password
    private String currentPassword;
    @Transient
    private PasswordEncoder passwordEncoder;

    public String getCurrentPassword()
    {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword)
    {
        this.currentPassword = currentPassword;
    }

    @Override
    public void validate(ValidationContext context)
    {
        Actor actor = AcegiUtils.getLoggedInUser();
        if (actor instanceof AcegiUser)
        {
            if (!passwordEncoder.isPasswordValid(((AcegiUser) actor).getPassword(), currentPassword, null))
            {
                context.addFieldError("currentPassword", I18N.format("currentPassword.invalid"));
                return;
            }
        }
        super.validate(context);
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder)
    {
        this.passwordEncoder = passwordEncoder;
    }
}
