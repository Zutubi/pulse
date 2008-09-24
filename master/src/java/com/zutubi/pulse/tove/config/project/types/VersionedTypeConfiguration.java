package com.zutubi.pulse.tove.config.project.types;

import com.zutubi.config.annotations.FieldAction;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Transient;
import com.zutubi.config.annotations.Wire;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.scm.*;
import com.zutubi.pulse.core.scm.config.ScmConfiguration;
import com.zutubi.pulse.personal.PatchArchive;
import com.zutubi.pulse.tove.config.project.ProjectConfiguration;
import com.zutubi.util.io.IOUtils;
import com.zutubi.validation.annotations.Required;

import java.io.InputStream;

/**
 * Pulse file project where the pulse file is stored in the project's SCM.
 */
@SymbolicName("zutubi.versionedTypeConfig")
@Wire
public class VersionedTypeConfiguration extends TypeConfiguration
{
    @Required
    @FieldAction(template = "actions/browse-scm-file")
    private String pulseFileName;
    @Transient
    private ScmClientFactory scmClientFactory;

    private ScmContextFactory scmContextFactory;

    public String getPulseFileName()
    {
        return pulseFileName;
    }

    public void setPulseFileName(String pulseFileName)
    {
        this.pulseFileName = pulseFileName;
    }

    public String getPulseFile(ProjectConfiguration projectConfig, Revision revision, PatchArchive patch) throws Exception
    {
        ScmClient scmClient = null;
        InputStream is = null;
        try
        {
            ScmConfiguration scm = projectConfig.getScm();
            ScmContext context = scmContextFactory.createContext(projectConfig.getProjectId(), scm);
            scmClient = scmClientFactory.createClient(scm);
            is = scmClient.retrieve(context, pulseFileName, revision);
            return IOUtils.inputStreamToString(is);
        }
        finally
        {
            ScmClientUtils.close(scmClient);
            IOUtils.close(is);
        }
    }

    public void setScmClientFactory(ScmClientFactory scmClientFactory)
    {
        this.scmClientFactory = scmClientFactory;
    }

    public void setScmContextFactory(ScmContextFactory scmContextFactory)
    {
        this.scmContextFactory = scmContextFactory;
    }
}
