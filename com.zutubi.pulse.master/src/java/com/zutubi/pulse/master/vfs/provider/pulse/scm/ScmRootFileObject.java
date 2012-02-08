package com.zutubi.pulse.master.vfs.provider.pulse.scm;

import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmContext;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.master.scm.ScmClientUtils;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.vfs.provider.pulse.ProjectConfigProvider;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 * The ScmRootFileObject is a special ScmFileObject that represents the root directory
 * of the scm directory structure.  
 * <p>
 * Using the scm configuration details, it makes the initial request to the scm
 * for the list of files and folders to be displayed.  From each of these, it
 * generates a new ScmFileObject which can then be navigated in turn.
 */
public class ScmRootFileObject extends AbstractScmFileObject
{
    /**
     * The displayName of the root scm file object is the scm location.
     */
    private String displayName = null;

    public ScmRootFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public void doAttach() throws FileSystemException
    {
        try
        {
            ScmConfiguration scm = getAncestor(ProjectConfigProvider.class).getProjectConfig().getScm();
            displayName = ScmClientUtils.withScmClient(scm, scmManager, new ScmClientUtils.ScmContextualAction<String>()
            {
                public String process(ScmClient client, ScmContext context) throws ScmException
                {
                    return client.getLocation(context);
                }
            });
        }
        catch (ScmException e)
        {
            throw new FileSystemException(e);
        }
    }

    public void doDetach()
    {
        synchronized(this)
        {
            displayName = null;
            loadedChildren = null;
        }
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    protected String getScmPath()
    {
        return "";
    }

    public ScmConfiguration getScm() throws FileSystemException
    {
        ProjectConfigProvider projectConfigProvider = getAncestor(ProjectConfigProvider.class);
        return projectConfigProvider.getProjectConfig().getScm();
    }

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }
}
