package com.zutubi.pulse.vfs.pulse;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.core.model.StoredArtifact;

/**
 * <class comment/>
 */
public class NamedArtifactFileObject extends AbstractPulseFileObject implements AddressableFileObject
{
    private final String artifactName;

    public NamedArtifactFileObject(final FileName name, final String artifactName, final AbstractFileSystem fs)
    {
        super(name, fs);

        this.artifactName = artifactName;
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        return null;
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.IMAGINARY;
    }

    protected String[] doListChildren() throws Exception
    {
        return new String[0];
    }

    public String getUrlPath()
    {
        // locate the artifact.
        try
        {
            BuildResult result = getBuildResult();

            // need to execute this call within the context of a transaction else lazy loading issues occur when
            // this node is traversed by servlet.
            StoredArtifact artifact = buildManager.getArtifact(result.getId(), artifactName);
            if (artifact != null)
            {
                // is html artifact.
                if (artifact.hasIndexFile() && !artifact.isSingleFile())
                {
                    return "/file/artifacts/" + artifact.getId() + "/" + artifact.findIndexFile();
                }
            }
            return null;
        }
        catch (FileSystemException e)
        {
            return null;
        }
    }

    protected BuildResult getBuildResult() throws FileSystemException
    {
        BuildResultProvider provider = (BuildResultProvider) getAncestor(BuildResultProvider.class);
        return provider.getBuildResult();
    }
}
