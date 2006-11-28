package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
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
public class ArtifactFileObject extends AbstractPulseFileObject implements StoredFileArtifactNode
{
    private File file;

    public ArtifactFileObject(final FileName name, final File base, final AbstractFileSystem fs)
    {
        super(name, fs);

        this.file = base;
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        return new ArtifactFileObject(fileName, new File(file, fileName.getBaseName()), pfs);
    }

    protected FileType doGetType() throws Exception
    {
        if (file.isDirectory())
        {
            return FileType.FOLDER;
        }
        if (file.isFile())
        {
            return FileType.FILE;
        }
        return null;
    }

    public File toFile()
    {
        return this.file;
    }

    protected String[] doListChildren() throws Exception
    {
        return file.list();
    }

    protected long doGetContentSize() throws Exception
    {
        return file.length();
    }

    protected InputStream doGetInputStream() throws Exception
    {
        return new FileInputStream(file);
    }

    public String getDisplayName()
    {
        return file.getName();
    }

    public List<String> getActions()
    {
        if (file.isFile())
        {
            List<String> actions = new LinkedList<String>();
            actions.add("download");

            try
            {
                StoredFileArtifact artifact = getFileArtifact();
                if (artifact != null && artifact.canDecorate())
                {
                    actions.add("decorate");
                }
            }
            catch (FileSystemException e)
            {
                // noop.
            }

            return actions;
        }
        return super.getActions();
    }

    public StoredFileArtifact getFileArtifact() throws FileSystemException
    {
        StoredArtifactNode node = (StoredArtifactNode) getAncestor(StoredArtifactNode.class);
        StoredArtifact artifact = node.getArtifact();

        FileName fn = ((FileObject)node).getName();
        String path = artifact.getName() + "/" + fn.getRelativeName(getName());
        return artifact.findFile(path);
    }

    public File getFile()
    {
        return file;
    }
}
