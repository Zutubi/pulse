package com.cinnamonbob.web.project;

import com.cinnamonbob.model.BuildResult;
import com.cinnamonbob.MasterBuildPaths;
import com.cinnamonbob.bootstrap.ConfigurationManager;
import com.cinnamonbob.web.DirectoryEntry;
import com.opensymphony.util.TextUtils;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;

/**
 */
public class BrowseProjectDir extends ProjectActionSupport
{
    private long buildId;
    private long recipeId;
    private String path;
    private BuildResult buildResult;
    private List<DirectoryEntry> entries;
    private InputStream inputStream;
    private String contentType;

    private ConfigurationManager configurationManager;

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public long getBuildId()
    {
        return buildId;
    }

    public void setBuildId(long buildId)
    {
        this.buildId = buildId;
    }

    public long getRecipeId()
    {
        return recipeId;
    }

    public void setRecipeId(long recipeId)
    {
        this.recipeId = recipeId;
    }

    public BuildResult getBuildResult()
    {
        return buildResult;
    }

    public List<DirectoryEntry> getEntries()
    {
        return entries;
    }

    private String getParentPath()
    {
        if(path.endsWith(File.separator))
        {
            path = path.substring(0, path.length() - 1);
        }

        int index = path.lastIndexOf(File.separatorChar);
        if(index == -1)
        {
            return "";
        }
        else
        {
            return path.substring(0, index);
        }
    }

    private void createDirectoryEntries(File dir)
    {
        entries = new LinkedList<DirectoryEntry>();

        if(TextUtils.stringSet(path))
        {
            entries.add(new DirectoryEntry(dir.getParentFile(), "..", getParentPath()));
        }

        File[] files = dir.listFiles();
        Arrays.sort(files);
        for(File f: files)
        {
            entries.add(new DirectoryEntry(f, f.getName(), path + File.separatorChar + f.getName()));
        }
    }

    public InputStream getInputStream()
    {
        return inputStream;
    }

    public String getContentType()
    {
        return contentType;
    }

    public String execute()
    {
        buildResult = getBuildManager().getBuildResult(buildId);
        if(buildResult == null)
        {
            addActionError("Unknown build [" + buildId + "]");
            return ERROR;
        }

        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        File file;
        if(TextUtils.stringSet(path))
        {
            file = new File(paths.getBaseDir(buildResult.getProject(), buildResult, recipeId), path);
        }
        else
        {
            file = paths.getBaseDir(buildResult.getProject(), buildResult, recipeId);
        }

        if(file.isDirectory())
        {
            createDirectoryEntries(file);
            return "dir";
        }
        else if(file.isFile())
        {
            try
            {
                inputStream = new FileInputStream(file);
                contentType = DirectoryEntry.guessMimeType(file.getName(), file);
                return "file";
            }
            catch (FileNotFoundException e)
            {
                addActionError("Unable to open file: " + e.getMessage());
                return ERROR;
            }
        }

        addActionError("Path '" + path + "' does not exist");
        return ERROR;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
