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
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.Password;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;

/**
 * Transient configuration type used to change a user's password.
 */
@SymbolicName("zutubi.setPasswordConfig")
@Form(fieldOrder = {"password", "confirmPassword"})
public class SetPasswordConfiguration extends AbstractConfiguration implements Validateable
{
    private static final Messages I18N = Messages.getInstance(SetPasswordConfiguration.class);

    @Password
    private String password;
    @Password
    private String confirmPassword;

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
        if(password != null && confirmPassword != null)
        {
            if(!password.equals(confirmPassword))
            {
                context.addFieldError("password", I18N.format("passwords.differ"));
            }
        }
    }
}
