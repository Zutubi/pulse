package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.core.scm.api.ScmClientFactory;
import com.zutubi.pulse.core.scm.ScmClientUtils;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.scm.config.ScmConfiguration;
import com.zutubi.pulse.master.scm.ScmContextFactory;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.Predicate;
import com.zutubi.util.StringUtils;
import com.zutubi.util.logging.Logger;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Used to browse a Pulse project's SCM.
 */
public class ScmFileObject extends AbstractPulseFileObject
{
    private static final Logger LOG = Logger.getLogger(ScmFileObject.class);

    private ScmClientFactory scmClientFactory;

    private ScmFile scmFile;
    private List<ScmFile> scmChildren;
    private ScmContextFactory scmContextFactory;

    public ScmFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
        this.scmFile = new ScmFile("", true);
    }

    public ScmFileObject(ScmFile scmFile, FileName name, AbstractFileSystem fs)
    {
        super(name, fs);
        this.scmFile = scmFile;
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        return objectFactory.buildBean(ScmFileObject.class,
                                       new Class[]{ScmFile.class, FileName.class, AbstractFileSystem.class},
                                       new Object[]{getScmChild(fileName), fileName, pfs});
    }

    private ScmFile getScmChild(final FileName fileName) throws FileSystemException
    {
        ScmFile file = CollectionUtils.find(getScmChildren(), new Predicate<ScmFile>()
        {
            public boolean satisfied(ScmFile scmFile)
            {
                return scmFile.getName().equals(fileName.getBaseName());
            }
        });

        if(file == null)
        {
            throw new FileSystemException("File '" + fileName.getPath() + "' does not exist");
        }
        
        return file;
    }

    private List<ScmFile> getScmChildren() throws FileSystemException
    {
        if (scmChildren == null)
        {
            ScmClient client = null;
            try
            {
                ScmConfiguration scm = getAncestor(ScmProvider.class).getScm();
                long projectId = getAncestor(ProjectConfigProvider.class).getProjectConfig().getProjectId();
                ScmContext context = scmContextFactory.createContext(projectId, scm);
                client = scmClientFactory.createClient(scm);
                scmChildren = client.browse(context, scmFile.getPath(), null);
            }
            catch (ScmException e)
            {
                LOG.warning(e);
                throw new FileSystemException("Unable to list SCM directory '" + scmFile.getPath() + "': " + e.getMessage(), e);
            }
            finally
            {
                ScmClientUtils.close(client);
            }
        }
        return scmChildren;
    }

    protected FileType doGetType() throws Exception
    {
        return scmFile.isDirectory() ? FileType.FOLDER : FileType.FILE;
    }

    protected String[] doListChildren() throws Exception
    {
        List<ScmFile> children = getScmChildren();
        return CollectionUtils.mapToArray(children, new Mapping<ScmFile, String>()
        {
            public String map(ScmFile scmFile)
            {
                return scmFile.getName();
            }
        }, new String[children.size()]);
    }


    public String getDisplayName()
    {
        if (scmFile.getPath().length() > 0)
        {
            return super.getDisplayName();
        }
        else
        {
            ScmClient client = null;
            try
            {
                client = scmClientFactory.createClient(getAncestor(ScmProvider.class).getScm());
                return client.getLocation();
            }
            catch (Exception e)
            {
                LOG.warning(e);
                return super.getDisplayName();
            }
            finally
            {
                ScmClientUtils.close(client);
            }
        }
    }

    public List<FileAction> getActions()
    {
        if (scmFile.isFile())
        {
            try
            {
                ProjectProvider projectProvider = getAncestor(ProjectProvider.class);
                if (projectProvider != null)
                {
                    long projectId = projectProvider.getProjectId();
                    return Arrays.asList(new FileAction("download", "/downloadSCMFile.action?projectId=" + projectId + "&path=" + StringUtils.formUrlEncode(scmFile.getPath())));
                }
            }
            catch (FileSystemException e)
            {
                LOG.severe(e);
            }
        }

        return Collections.emptyList();
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
