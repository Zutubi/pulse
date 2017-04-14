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
import com.zutubi.pulse.master.model.UserManager;
import com.zutubi.pulse.master.security.SecurityUtils;
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
        String username = SecurityUtils.getLoggedInUsername();
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