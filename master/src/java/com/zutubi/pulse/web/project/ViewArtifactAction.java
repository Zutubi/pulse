package com.zutubi.pulse.web.project;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.core.model.PlainFeature;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.util.logging.Logger;
import com.zutubi.pulse.vfs.pulse.BuildResultNode;
import com.zutubi.pulse.vfs.pulse.CommandResultNode;
import com.zutubi.pulse.vfs.pulse.AbstractPulseFileObject;
import com.zutubi.pulse.vfs.pulse.StoredFileArtifactNode;
import com.zutubi.pulse.web.vfs.VFSActionSupport;
import com.opensymphony.util.TextUtils;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;

import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * 
 *
 */
public class ViewArtifactAction extends VFSActionSupport
{
    private static final Logger LOG = Logger.getLogger(ViewArtifactAction.class);

    private BuildResult buildResult;
    private CommandResult commandResult;
    private StoredFileArtifact artifact;
    private BufferedReader reader;
    private Map<Long, Feature.Level> lineLevels;

    private String path;

    /**
     * @deprecated
     */
    private String root;

    public void setPath(String path)
    {
        this.path = path;
    }

    /**
     * @deprecated
     */
    public void setRoot(String root)
    {
        this.root = root;
    }

    public BuildResult getBuildResult()
    {
        return buildResult;
    }

    public CommandResult getCommandResult()
    {
        return commandResult;
    }

    public StoredFileArtifact getArtifact()
    {
        return artifact;
    }

    public ReaderIterator getReaderIterator()
    {
        return new ReaderIterator();
    }

    public Feature.Level getLineLevel(long line)
    {
        return lineLevels.get(line);
    }

    private void determineLineLevels()
    {
        lineLevels = new TreeMap<Long, Feature.Level>();
        for(Feature f: artifact.getFeatures())
        {
            if(f instanceof PlainFeature)
            {
                PlainFeature p = (PlainFeature) f;
                updateLevel(p.getLineNumber(), p.getLevel());
            }
        }
    }

    private void updateLevel(long n, Feature.Level level)
    {
        if(lineLevels.containsKey(n))
        {
            if(!level.isGreaterThan(lineLevels.get(n)))
            {
                return;
            }
        }
        lineLevels.put(n, level);
    }

    public String execute() throws FileSystemException
    {
        if (TextUtils.stringSet(root))
        {
            path = root + path;
        }

        FileObject fo = getFS().resolveFile(path);
        if (!StoredFileArtifactNode.class.isAssignableFrom(fo.getClass()))
        {
            return ERROR;
        }

        AbstractPulseFileObject pfo = (AbstractPulseFileObject) fo;

        buildResult = ((BuildResultNode)pfo.getAncestor(BuildResultNode.class)).getBuildResult();
        commandResult = ((CommandResultNode)pfo.getAncestor(CommandResultNode.class)).getCommandResult();
        artifact = ((StoredFileArtifactNode)pfo).getFileArtifact();

        File artifactFile = ((StoredFileArtifactNode)pfo).getFile();
        if(!artifactFile.isFile())
        {
            addActionError("Artifact file '" + artifactFile.getAbsolutePath() + "' does not exist");
            return ERROR;
        }

        try
        {
            reader = new BufferedReader(new FileReader(artifactFile.getAbsolutePath()));
        }
        catch (FileNotFoundException e)
        {
            addActionError("Unable to open artifact file: " + e.getMessage());
            return ERROR;
        }

        determineLineLevels();

        return SUCCESS;
    }

    class ReaderIterator implements Iterator
    {
        private boolean buffered = false;
        private String line;

        public boolean hasNext()
        {
            if(buffered)
            {
                return true;
            }
            else
            {
                try
                {
                    line = reader.readLine();

                    if(line != null)
                    {
                        buffered = true;
                        return true;
                    }
                }
                catch (IOException e)
                {
                    LOG.severe("I/O error reading artifact: " + e.getMessage());
                }
            }

            return false;
        }

        public Object next()
        {
            if(buffered)
            {
                buffered = false;
                String temp = line;
                line = null;
                return temp;
            }

            try
            {
                return reader.readLine();
            }
            catch (IOException e)
            {
                LOG.severe("I/O error reading artifact: " + e.getMessage());
            }

            return "";
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

}
