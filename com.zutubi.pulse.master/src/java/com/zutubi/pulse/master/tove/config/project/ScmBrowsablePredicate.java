package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.tove.annotations.FieldAction;
import com.zutubi.pulse.core.scm.api.ScmCapability;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.core.scm.ScmClientUtils;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.vfs.provider.pulse.AbstractPulseFileObject;
import com.zutubi.pulse.master.vfs.provider.pulse.ProjectConfigProvider;
import com.zutubi.pulse.master.tove.model.FieldDescriptor;
import com.zutubi.pulse.master.tove.handler.FieldActionPredicate;
import com.zutubi.util.TextUtils;
import com.zutubi.util.logging.Logger;
import org.apache.commons.vfs.FileSystemManager;

import java.util.Set;

/**
 * Checks that a valid SCM exists and that the implementation supports
 * browsing before showing a browse action.
 */
public class ScmBrowsablePredicate implements FieldActionPredicate
{
    private static final Logger LOG = Logger.getLogger(ScmBrowsablePredicate.class);

    private FileSystemManager fileSystemManager;
    private ScmManager scmManager;

    public boolean satisfied(FieldDescriptor field, FieldAction annotation)
    {
        String projectPath = field.getParentPath();
        if(TextUtils.stringSet(field.getBaseName()))
        {
            projectPath = "c" + projectPath;
        }
        else
        {
            projectPath = "wizards/" + projectPath;
        }
        try
        {
            AbstractPulseFileObject pfo = (AbstractPulseFileObject) fileSystemManager.resolveFile("pulse:///" + projectPath);
            ProjectConfigProvider projectConfigProvider = pfo.getAncestor(ProjectConfigProvider.class);
            if (projectConfigProvider != null && projectConfigProvider.getProjectConfig() != null)
            {
                ScmConfiguration config = projectConfigProvider.getProjectConfig().getScm();
                if(config != null && config.isValid())
                {
                    Set<ScmCapability> capabilities = ScmClientUtils.getCapabilities(config, scmManager);
                    return scmManager.isReady(config) && capabilities.contains(ScmCapability.BROWSE);
                }
            }
        }
        catch (Exception e)
        {
            LOG.warning(e);
        }

        return false;
    }

    public void setFileSystemManager(FileSystemManager fileSystemManager)
    {
        this.fileSystemManager = fileSystemManager;
    }

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }
}
