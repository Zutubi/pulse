package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.scm.SCMFile;
import com.zutubi.util.Mapping;
import com.zutubi.util.StringUtils;
import com.zutubi.util.logging.Logger;
import com.zutubi.pulse.vfs.FileAction;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.*;
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
public class SCMFileObject extends AbstractPulseFileObject
{
    private static final Logger LOG = Logger.getLogger(SCMFileObject.class);

    private SCMFile scmFile;
    private List<SCMFile> scmChildren;

    public SCMFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
        this.scmFile = new SCMFile(true, "");
    }

    public SCMFileObject(SCMFile scmFile, FileName name, AbstractFileSystem fs)
    {
        super(name, fs);
        this.scmFile = scmFile;
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        return objectFactory.buildBean(SCMFileObject.class,
                                       new Class[]{SCMFile.class, FileName.class, AbstractFileSystem.class},
                                       new Object[]{getSCMChild(fileName), fileName, pfs});
    }

    private SCMFile getSCMChild(final FileName fileName) throws FileSystemException
    {
        SCMFile file = CollectionUtils.find(getSCMChildren(), new Predicate<SCMFile>()
        {
            public boolean satisfied(SCMFile scmFile)
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

    private List<SCMFile> getSCMChildren() throws FileSystemException
    {
        if (scmChildren == null)
        {
            try
            {
                scmChildren = getAncestor(SCMProvider.class).getScm().createClient().getListing(scmFile.getPath());
            }
            catch (SCMException e)
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
        List<SCMFile> children = getSCMChildren();
        return CollectionUtils.mapToArray(children, new Mapping<SCMFile, String>()
        {
            public String map(SCMFile scmFile)
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
                return getAncestor(SCMProvider.class).getScm().createClient().getLocation();
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
}
