package com.zutubi.pulse.core.scm.config.api;

import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Table;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.validation.annotations.Required;

/**
 * Maps from an SCM login to an email address for a specific committer.  The
 * email address may be complete (user@domain) or partial (user) in which case
 * the domain from the {@link com.zutubi.pulse.master.hook.email.EmailCommittersTaskConfiguration}
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
