package com.zutubi.pulse.web.project;

import com.zutubi.pulse.MasterBuildPaths;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.RecipeResultNode;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.tove.config.user.UserPreferencesConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.util.CircularBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 */
public class TailRecipeLogAction extends StageActionBase
{
    private static final int LINE_COUNT = 30;
    private static final int MAX_BYTES = 500 * LINE_COUNT;

    private boolean raw = false;
    private int maxLines = -1;
    private int refreshInterval = -1;
    private String tail = "";
    private MasterConfigurationManager configurationManager;
    private boolean logExists;
    private FileInputStream inputStream;
    private ConfigurationProvider configurationProvider;

    public void setRaw(boolean raw)
    {
        this.raw = raw;
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

    public FileInputStream getInputStream()
    {
        return inputStream;
    }

    public String execute() throws Exception
    {
        initialiseProperties();

        BuildResult buildResult = getRequiredBuildResult();
        RecipeResultNode resultNode = getRequiredRecipeResultNode();
        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        File recipeLog = new File(paths.getRecipeDir(buildResult, resultNode.getResult().getId()), RecipeResult.RECIPE_LOG);
        if (recipeLog.exists())
        {
            logExists = true;
            if (raw)
            {
                return getRaw(recipeLog);
            }
            else
            {
                return getTail(recipeLog);
            }
        }
        else
        {
            logExists = false;
        }

        return "tail";
    }

    private String getRaw(File recipeLog)
    {
        try
        {
            inputStream = new FileInputStream(recipeLog);
        }
        catch (IOException e)
        {
            addActionError("Unable to open recipe log '" + recipeLog.getAbsolutePath() + "': " + e.getMessage());
            return ERROR;
        }

        return "raw";
    }

    private String getTail(File recipeLog) throws IOException
    {
        RandomAccessFile file = null;

        try
        {
            file = new RandomAccessFile(recipeLog, "r");
            long length = file.length();
            if (length > 0)
            {
                if (length > MAX_BYTES)
                {
                    file.seek(length - MAX_BYTES);
                    length = MAX_BYTES;

                    // Discard the next (possibly partial) line
                    file.readLine();
                }

                CircularBuffer<String> buffer = new CircularBuffer<String>(maxLines);
                String line = file.readLine();
                while (line != null)
                {
                    buffer.append(line);
                    line = file.readLine();
                }

                StringBuilder builder = new StringBuilder((int) length);
                for (String l : buffer)
                {
                    builder.append(l);
                    builder.append('\n');
                }

                tail = builder.toString();
            }
        }
        catch (IOException e)
        {
            addActionError("Error tailing log '" + recipeLog.getAbsolutePath() + "': " + e.getMessage());
            return ERROR;
        }
        finally
        {
            if (file != null)
            {
                file.close();
            }
        }

        return "tail";
    }

    private void initialiseProperties()
    {
        Object principle = getPrinciple();
        if (principle != null)
        {
            User user = userManager.getUser((String) principle);
            if (user != null)
            {
                boolean changed = false;

                UserPreferencesConfiguration preferences = user.getPreferences();
                if (refreshInterval <= 0)
                {
                    refreshInterval = preferences.getTailRefreshInterval();
                }
                else if (refreshInterval != preferences.getTailRefreshInterval())
                {
                    preferences = configurationProvider.deepClone(preferences);
                    preferences.setTailRefreshInterval(refreshInterval);
                    changed = true;
                }

                if (maxLines <= 0)
                {
                    maxLines = preferences.getTailLines();
                }
                else if (maxLines != preferences.getTailLines())
                {
                    if (!changed)
                    {
                        preferences = configurationProvider.deepClone(preferences);
                    }
                    preferences.setTailLines(maxLines);
                    changed = true;
                }

                if (changed)
                {
                    configurationProvider.save(preferences);
                }
            }
        }

        // Just in case the user couldn't be found
        if (refreshInterval <= 0)
        {
            refreshInterval = 60;
        }

        if (maxLines <= 0)
        {
            maxLines = LINE_COUNT;
        }
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
