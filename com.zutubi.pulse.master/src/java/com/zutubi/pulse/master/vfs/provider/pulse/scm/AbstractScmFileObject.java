package com.zutubi.pulse.master.vfs.provider.pulse.scm;

import com.google.common.base.Function;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmContext;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.api.ScmFile;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.scm.ScmClientUtils;
import static com.zutubi.pulse.master.scm.ScmClientUtils.withScmClient;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.vfs.provider.pulse.AbstractPulseFileObject;
import com.zutubi.pulse.master.vfs.provider.pulse.ProjectConfigProvider;
import com.zutubi.pulse.master.vfs.provider.pulse.ProjectProvider;
import com.zutubi.pulse.master.vfs.provider.pulse.PulseFileName;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.logging.Logger;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.UriParser;

import java.util.List;

/**
 * The base ScmFileObject class that contains the common functionality between
 * the ScmFileObject instance and the ScmRootFileObject instance.
 * <p>
 * Note: So that the scm file objects can run without caching, the children list
 * embeds the file/directory information in the listing, and then strips this out
 * when creating a new instance.
 */
public abstract class AbstractScmFileObject extends AbstractPulseFileObject
{
    private static final Logger LOG = Logger.getLogger(AbstractScmFileObject.class);

    private static final String DIRECTORY_SUFFIX = ".dir";
    private static final String FILE_SUFFIX = ".file";
    protected ScmManager scmManager;
    protected List<ScmFile> loadedChildren;

    public AbstractScmFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws FileSystemException
    {
        // trickery - parse the fileName, extracting the file/directory information
        // that we added during the listing.

        FileType type = FileType.FOLDER;
        String path = fileName.getPath();
        if (fileName.getBaseName().endsWith(DIRECTORY_SUFFIX))
        {
            type = FileType.FOLDER;
            path = path.substring(0, path.length() - 4);
        }
        else if (fileName.getBaseName().endsWith(FILE_SUFFIX))
        {
            type = FileType.FILE;
            path = path.substring(0, path.length() - 5);
        }

        PulseFileName name = new PulseFileName(fileName.getScheme(), path, type);

        // now we need to determine the scm relative path to be used with the ScmFile instance.

        FileObject root = getAncestor(ScmRootFileObject.class);

        String relativeName = root.getName().getRelativeName(name);
        ScmFile fi = new ScmFile(UriParser.decode(relativeName), name.getType() == FileType.FOLDER);

        return objectFactory.buildBean(ScmFileObject.class,
                new Class[]{ScmFile.class, FileName.class, AbstractFileSystem.class},
                new Object[]{fi, name, pfs});
    }


    protected String[] doListChildren() throws Exception
    {
        List<ScmFile> children = getScmChildren();
        return UriParser.encode(CollectionUtils.mapToArray(children, new Function<ScmFile, String>()
        {
            public String apply(ScmFile scmFile)
            {
                return scmFile.getName() + ((scmFile.isDirectory()) ? DIRECTORY_SUFFIX : FILE_SUFFIX);
            }
        }, new String[children.size()]));
    }

    private List<ScmFile> getScmChildren() throws FileSystemException
    {
        synchronized (this)
        {
            if (loadedChildren == null)
            {
                try
                {
                    Project project = null;
                    ProjectProvider projectProvider = getAncestor(ProjectProvider.class);
                    if (projectProvider != null)
                    {
                        project = projectProvider.getProject();
                    }

                    ScmClientUtils.ScmContextualAction<List<ScmFile>> action = new ScmClientUtils.ScmContextualAction<List<ScmFile>>()
                    {
                        public List<ScmFile> process(ScmClient client, ScmContext context) throws ScmException
                        {
                            return client.browse(context, getScmPath(), null);
                        }
                    };

                    if (project == null)
                    {
                        ProjectConfigProvider configProvider = getAncestor(ProjectConfigProvider.class);
                        loadedChildren = withScmClient(configProvider.getProjectConfig().getScm(), scmManager, action);
                    }
                    else
                    {
                        loadedChildren = withScmClient(project.getConfig(), project.getState(), scmManager, action);
                    }
                }
                catch (ScmException e)
                {
                    LOG.warning(e);
                    throw new FileSystemException("Unable to list SCM directory '" + getScmPath() + "': " + e.getMessage(), e);
                }
            }
            return loadedChildren;
        }
    }

    protected abstract String getScmPath();

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }
}
