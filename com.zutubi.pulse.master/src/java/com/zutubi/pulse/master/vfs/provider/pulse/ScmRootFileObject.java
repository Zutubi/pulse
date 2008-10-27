package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.util.TextUtils;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 * Used to browse a Pulse project's SCM.  This file object represents the
 * root of the SCM view.
 */
public class ScmRootFileObject extends AbstractPulseFileObject implements ScmProvider
{
    private ConfigurationProvider configurationProvider;

    public ScmRootFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        return objectFactory.buildBean(ScmFileObject.class,
                                       new Class[]{FileName.class, AbstractFileSystem.class},
                                       new Object[]{fileName, pfs});
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        return new String[]{"scm"};
    }

    public ScmConfiguration getScm() throws FileSystemException
    {
        ProjectConfigProvider projectConfigProvider = getAncestor(ProjectConfigProvider.class);
        ScmConfiguration scmConfig = projectConfigProvider.getProjectConfig().getScm();
        String scmPath = scmConfig.getConfigurationPath();
        if(TextUtils.stringSet(scmPath))
        {
            if(!configurationProvider.isDeeplyValid(scmPath))
            {
                throw new FileSystemException("SCM configuration is invalid");
            }
        }
        else
        {
            // No path - we are in a wizard.  We can still check the flat
            // properties.
            if(!scmConfig.isValid())
            {
                throw new FileSystemException("SCM configuration is invalid");
            }
        }
        return scmConfig;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
