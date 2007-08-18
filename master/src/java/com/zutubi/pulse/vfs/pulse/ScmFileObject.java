package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.core.scm.ScmClient;
import com.zutubi.pulse.core.scm.ScmClientFactory;
import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.core.scm.ScmFile;
import com.zutubi.pulse.vfs.FileAction;
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

    public ScmFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
        this.scmFile = new ScmFile(true, "");
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
            try
            {
                ScmClient client = scmClientFactory.createClient(getAncestor(ScmProvider.class).getScm());
                scmChildren = client.browse(scmFile.getPath());
            }
            catch (ScmException e)
            {
                LOG.warning(e);
                throw new FileSystemException("Unable to list SCM directory '" + scmFile.getPath() + "': " + e.getMessage(), e);
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
            try
            {
                ScmClient client = scmClientFactory.createClient(getAncestor(ScmProvider.class).getScm());
                return client.getLocation();
            }
            catch (Exception e)
            {
                LOG.warning(e);
                return super.getDisplayName();
            }
        }
    }


    @SuppressWarnings({"unchecked"})
    public List<FileAction> getActions()
    {
        if (scmFile.isFile())
        {
            try
            {
                long projectId = getAncestor(ProjectProvider.class).getProjectId();
                return Arrays.asList(new FileAction("download", "/downloadSCMFile.action?projectId=" + projectId + "&path=" + StringUtils.formUrlEncode(scmFile.getPath())));
            }
            catch (FileSystemException e)
            {
                LOG.severe(e);
            }
        }

        return Collections.EMPTY_LIST;
    }

    public void setScmClientFactory(ScmClientFactory scmClientFactory)
    {
        this.scmClientFactory = scmClientFactory;
    }
}
