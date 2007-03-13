package com.zutubi.pulse.web.project;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.util.logging.Logger;
import com.zutubi.pulse.vfs.pulse.*;
import com.zutubi.pulse.web.vfs.VFSActionSupport;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;

import java.io.*;
import java.util.*;

/**
 * 
 *
 */
public class ViewArtifactAction extends VFSActionSupport
{
    private static final Logger LOG = Logger.getLogger(ViewArtifactAction.class);

    private MasterConfigurationManager configurationManager;
    private BuildManager buildManager;

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

    public List<Feature.Level> getFeatureLevels()
    {
        List<Feature.Level> list = Arrays.asList(Feature.Level.values());
        Collections.reverse(list);
        return list;
    }

    private void updateLevel(long n, Feature.Level level)
    {
        if(lineLevels.containsKey(n))
        {
            if(level.compareTo(lineLevels.get(n)) > 0)
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
        if (!FileArtifactProvider.class.isAssignableFrom(fo.getClass()))
        {
            return ERROR;
        }

        AbstractPulseFileObject pfo = (AbstractPulseFileObject) fo;

        buildResult = pfo.getAncestor(BuildResultProvider.class).getBuildResult();
        RecipeResult recipeResult = pfo.getAncestor(RecipeResultProvider.class).getRecipeResult();
        commandResult = pfo.getAncestor(CommandResultProvider.class).getCommandResult();
        commandResult.loadFeatures(recipeResult.getRecipeDir(configurationManager.getDataDirectory()));
        artifact = buildManager.getFileArtifact(((FileArtifactProvider)pfo).getFileArtifactId());

        File artifactFile = ((FileArtifactProvider)pfo).getFile();
        if(!artifactFile.isFile())
        {
            addActionError("LocalArtifact file '" + artifactFile.getAbsolutePath() + "' does not exist");
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

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
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
