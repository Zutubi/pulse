package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.MasterBuildPaths;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.build.log.LogFile;
import com.zutubi.pulse.master.build.log.RecipeLogFile;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.tove.config.user.UserPreferencesConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;

import java.io.IOException;
import java.io.InputStream;

/**
 */
public class TailRecipeLogAction extends StageActionBase
{
    private static final int LINE_COUNT = 30;

    protected boolean raw = false;
    protected int maxLines = -1;
    private int refreshInterval = -1;
    protected String tail = "";
    protected MasterConfigurationManager configurationManager;
    protected boolean logExists;
    protected InputStream inputStream;
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

    public InputStream getInputStream()
    {
        return inputStream;
    }

    public String execute() throws Exception
    {
        initialiseProperties();

        BuildResult buildResult = getRequiredBuildResult();
        RecipeResultNode resultNode = getRequiredRecipeResultNode();
        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        LogFile recipeLog = new RecipeLogFile(buildResult, resultNode.getResult().getId(), paths);
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

    protected String getRaw(LogFile recipeLog)
    {
        try
        {
            inputStream = recipeLog.openInputStream();
        }
        catch (IOException e)
        {
            addActionError("Unable to open recipe log: " + e.getMessage());
            return ERROR;
        }

        return "raw";
    }

    protected String getTail(LogFile recipeLog) throws IOException
    {
        try
        {
            this.tail = recipeLog.getTail(maxLines);
        }
        catch (IOException e)
        {
            addActionError("Error tailing log: " + e.getMessage());
            return ERROR;
        }

        return "tail";
    }

    protected void initialiseProperties()
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
