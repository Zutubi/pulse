/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.project;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.core.model.PlainFeature;
import com.zutubi.pulse.util.logging.Logger;
import com.zutubi.pulse.model.BuildResult;

import java.io.*;
import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;

/**
 * 
 *
 */
public class ViewArtifactAction extends ProjectActionSupport
{
    private static final Logger LOG = Logger.getLogger(ViewArtifactAction.class);

    private long id;
    private long buildId;
    private long commandId;
    private BuildResult buildResult;
    private CommandResult commandResult;
    private StoredFileArtifact artifact;
    private BufferedReader reader;
    private Map<Long, Feature.Level> lineLevels;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public long getBuildId()
    {
        return buildId;
    }

    public void setBuildId(long buildId)
    {
        this.buildId = buildId;
    }

    public long getCommandId()
    {
        return commandId;
    }

    public void setCommandId(long commandId)
    {
        this.commandId = commandId;
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
                for(long n = p.getFirstLine(); n <= p.getLastLine(); n++)
                {
                    updateLevel(n, p.getLevel());
                }
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

    public void validate()
    {
        buildResult = getBuildManager().getBuildResult(buildId);
        if(buildResult == null)
        {
            addActionError("Unknown build result [" + buildId + "]");
        }
        commandResult = getBuildManager().getCommandResult(commandId);
        if (commandResult == null)
        {
            addActionError("Unknown command result [" + commandId + "]");
        }

        artifact = getBuildManager().getFileArtifact(id);
        if (artifact == null)
        {
            addActionError("Unknown artifact [" + id + "]");
        }
    }

    public String execute()
    {
        File artifactFile = new File(commandResult.getOutputDir(), artifact.getPath());
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
