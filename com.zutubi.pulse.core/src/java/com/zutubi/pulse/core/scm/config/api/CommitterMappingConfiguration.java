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

package com.zutubi.pulse.core.scm.config.api;

import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Table;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.validation.annotations.Required;

/**
 * Maps from an SCM login to an email address for a specific committer.  The
 * email address may be complete (user@domain) or partial (user) in which case
 * the domain from the {@link com.zutubi.pulse.master.hook.email.SendEmailTaskConfiguration}
 * will be appended.
 */
@SymbolicName("zutubi.committerMappingConfig")
@Form(fieldOrder = {"scmLogin", "email"})
@Table(columns = {"scmLogin", "email"})
public class CommitterMappingConfiguration extends AbstractConfiguration
{
    @Required
    private String scmLogin;
    @Required
    private String email;

    public String getScmLogin()
    {
        return scmLogin;
    }

    public void setScmLogin(String scmLogin)
    {
        this.scmLogin = scmLogin;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }
}
