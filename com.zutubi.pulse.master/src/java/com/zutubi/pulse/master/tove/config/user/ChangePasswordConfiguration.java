package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.model.UserManager;
import com.zutubi.pulse.master.security.AcegiUtils;
import com.zutubi.tove.annotations.*;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;

/**
 * Transient configuration type used by a user to change their own newPassword.
 */
@SymbolicName("zutubi.changePasswordConfig")
@Form(fieldOrder = {"currentPassword", "newPassword", "confirmNewPassword"})
@Wire
public class ChangePasswordConfiguration extends AbstractConfiguration implements Validateable
{
    private static final Messages I18N = Messages.getInstance(ChangePasswordConfiguration.class);
    
    @Password
    private String currentPassword;
    @Password
    private String newPassword;
    @Password
    private String confirmNewPassword;

    @Transient
    AccessManager accessManager;
    @Transient
    private UserManager userManager;
    
    public String getCurrentPassword()
    {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword)
    {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword()
    {
        return newPassword;
    }

    public void setNewPassword(String newPassword)
    {
        this.newPassword = newPassword;
    }

    public String getConfirmNewPassword()
    {
        return confirmNewPassword;
    }

    public void setConfirmNewPassword(String confirmNewPassword)
    {
        this.confirmNewPassword = confirmNewPassword;
    }

    public void validate(ValidationContext context)
    {
        String username = AcegiUtils.getLoggedInUsername();
        if (username == null)
        {
            context.addActionError(I18N.format("user.none"));
        }
        
        UserConfiguration user = userManager.getUserConfig(username);
        if (user == null)
        {
            context.addActionError(I18N.format("user.invalid", username));
        }
        
        if (!userManager.checkPassword(user, currentPassword))
        {
            context.addFieldError("currentPassword", I18N.format("currentPassword.incorrect"));
        }
        
        if (newPassword != null && confirmNewPassword != null)
        {
            if (!newPassword.equals(confirmNewPassword))
            {
                context.addFieldError("newPassword", I18N.format("newPassword.differs"));
            }
        }
    }

    public void setAccessManager(AccessManager accessManager)
    {
        this.accessManager = accessManager;
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }
}