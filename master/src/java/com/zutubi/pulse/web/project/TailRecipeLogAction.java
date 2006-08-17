package com.zutubi.pulse.web.project;

import com.zutubi.pulse.MasterBuildPaths;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.RecipeResultNode;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.UserManager;
import com.zutubi.pulse.util.CircularBuffer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 */
public class TailRecipeLogAction extends ProjectActionSupport
{
    private static final int LINE_COUNT = 30;
    private static final int MAX_BYTES = 500 * LINE_COUNT;

    private long id;
    private long buildId;
    private int maxLines = -1;
    private int refreshInterval = -1;
    private BuildResult buildResult;
    private RecipeResultNode resultNode;
    private String tail = "";
    private MasterConfigurationManager configurationManager;
    private UserManager userManager;
    private boolean logExists;

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

    public BuildResult getBuildResult()
    {
        return buildResult;
    }

    public RecipeResultNode getResultNode()
    {
        return resultNode;
    }

    public String getTail()
    {
        return tail;
    }

    public int getMaxLines()
    {
        return maxLines;
    }

    public void setMaxLines(int maxLines)
    {
        this.maxLines = maxLines;
    }

    public int getRefreshInterval()
    {
        return refreshInterval;
    }

    public void setRefreshInterval(int refreshInterval)
    {
        this.refreshInterval = refreshInterval;
    }

    public boolean getLogExists()
    {
        return logExists;
    }

    public String execute() throws Exception
    {
        initialiseProperties();

        buildResult = getBuildManager().getBuildResult(buildId);
        if(buildResult == null)
        {
            addActionError("Unknown build [" + buildId + "]");
            return ERROR;
        }

        resultNode = buildResult.findResultNode(id);
        if(resultNode == null)
        {
            addActionError("Unknown stage [" + id + "]");
            return ERROR;
        }

        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        File recipeLog = new File(paths.getRecipeDir(buildResult.getProject(), buildResult, resultNode.getResult().getId()), RecipeResult.RECIPE_LOG);
        if(recipeLog.exists())
        {
            logExists = true;
            RandomAccessFile file = null;

            try
            {
                file = new RandomAccessFile(recipeLog, "r");
                long length = file.length();
                if (length > 0)
                {
                    if(length > MAX_BYTES)
                    {
                        file.seek(length - MAX_BYTES);
                        length = MAX_BYTES;

                        // Discard the next (possibly partial) line
                        file.readLine();
                    }

                    CircularBuffer<String> buffer = new CircularBuffer<String>(maxLines);
                    String line = file.readLine();
                    while(line != null)
                    {
                        buffer.append(line);
                        line = file.readLine();
                    }

                    StringBuilder builder = new StringBuilder((int) length);
                    for(String l: buffer)
                    {
                        builder.append(l);
                        builder.append('\n');
                    }

                    tail = builder.toString();
                }
            }
            catch(IOException e)
            {
                addActionError("Error tailing log '" + recipeLog.getAbsolutePath() + "': " + e.getMessage());
                return ERROR;
            }
            finally
            {
                if(file != null)
                {
                    file.close();
                }
            }
        }
        else
        {
            logExists = false;
        }

        return SUCCESS;
    }

    private void initialiseProperties()
    {
        Object principle = getPrinciple();
        if(principle != null)
        {
            User user = userManager.getUser((String) principle);
            if (user != null)
            {
                boolean changed = false;

                if(refreshInterval <= 0)
                {
                    refreshInterval = user.getTailRefreshInterval();
                }
                else if(refreshInterval != user.getTailRefreshInterval())
                {
                    user.setTailRefreshInterval(refreshInterval);
                    changed = true;
                }

                if(maxLines <= 0)
                {
                    maxLines = user.getTailLines();
                }
                else if(maxLines != user.getTailLines())
                {
                    user.setTailLines(maxLines);
                    changed = true;
                }

                if(changed)
                {
                    userManager.save(user);
                }
            }
        }

        // Just in case the user couldn't be found
        if(refreshInterval <= 0)
        {
            refreshInterval = 60;
        }

        if(maxLines <= 0)
        {
            maxLines = LINE_COUNT;
        }
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }
}
