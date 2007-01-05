package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.util.logging.Logger;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * <class comment/>
 */
public class FileArtifactFileObject extends AbstractPulseFileObject implements AddressableFileObject, FileArtifactProvider
{
    private static final Logger LOG = Logger.getLogger(FileArtifactFileObject.class);

    private File base;

    private Boolean canDecorate;

    public FileArtifactFileObject(final FileName name, final File base, final AbstractFileSystem fs)
    {
        super(name, fs);

        this.base = base;
    }

    private boolean canDecorate()
    {
        if (canDecorate == null)
        {
            try
            {
                StoredFileArtifact artifact = getFileArtifact();
                canDecorate = buildManager.canDecorateArtifact(artifact.getId());
            }
            catch (FileSystemException e)
            {
                canDecorate = false;
                LOG.error(e);
            }
        }
        return canDecorate;
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        File newBase = new File(base, fileName.getBaseName());

        return objectFactory.buildBean(FileArtifactFileObject.class,
                new Class[]{FileName.class, File.class, AbstractFileSystem.class},
                new Object[]{fileName, newBase, pfs}
        );
    }

    protected FileType doGetType() throws Exception
    {
        if (base.isDirectory())
        {
            return FileType.FOLDER;
        }
        return FileType.FILE;
    }

    protected String[] doListChildren() throws Exception
    {
        return base.list();
    }

    protected long doGetContentSize() throws Exception
    {
        return base.length();
    }

    protected InputStream doGetInputStream() throws Exception
    {
        return new FileInputStream(base);
    }

    public File toFile()
    {
        return base;
    }

    public boolean isLocal()
    {
        return true;
    }

    public String getUrlPath()
    {
        try
        {
            return "/file/artifacts/" + getArtifact().getId() + "/" + getArtifactPath();
        }
        catch (FileSystemException e)
        {
            LOG.warning(e);
            return "";
        }
    }

    public List<String> getActions()
    {
        List<String> actions = new LinkedList<String>();
        if (base.isFile())
        {
            actions.add("download");
        }
        if (canDecorate())
        {
            actions.add("decorate");
        }
        return actions;
    }

    public StoredFileArtifact getFileArtifact() throws FileSystemException
    {
        return getArtifact().findFile(getArtifact().getName() + "/" + getArtifactPath());
    }

    public File getFile()
    {
        return base;
    }

    public long getFileArtifactId() throws FileSystemException
    {
        return getFileArtifact().getId();
    }

    protected StoredArtifact getArtifact() throws FileSystemException
    {
        return ((ArtifactProvider)getAncestor(ArtifactProvider.class)).getArtifact();
    }

    protected String getArtifactPath() throws FileSystemException
    {
        AbstractPulseFileObject fo = getAncestor(ArtifactProvider.class);
        return fo.getName().getRelativeName(getName());
    }
}
