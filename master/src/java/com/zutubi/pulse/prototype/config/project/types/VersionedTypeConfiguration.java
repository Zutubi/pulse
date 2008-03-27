package com.zutubi.pulse.prototype.config.project.types;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Transient;
import com.zutubi.config.annotations.Wire;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.scm.ScmClient;
import com.zutubi.pulse.core.scm.ScmClientFactory;
import com.zutubi.pulse.core.scm.ScmClientUtils;
import com.zutubi.pulse.personal.PatchArchive;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.util.IOUtils;

import java.io.InputStream;

/**
 *
 */
@SymbolicName("zutubi.versionedTypeConfig")
@Wire
public class VersionedTypeConfiguration extends TypeConfiguration
{
    private String pulseFileName;
    @Transient
    private ScmClientFactory scmClientFactory;

    public String getPulseFileName()
    {
        return pulseFileName;
    }

    public void setPulseFileName(String pulseFileName)
    {
        this.pulseFileName = pulseFileName;
    }

    public String getPulseFile(long id, ProjectConfiguration projectConfig, Revision revision, PatchArchive patch) throws Exception
    {
        ScmClient scmClient = null;
        InputStream is = null;
        try
        {
            scmClient = scmClientFactory.createClient(projectConfig.getScm());
            is = scmClient.retrieve(pulseFileName, revision);
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
}
