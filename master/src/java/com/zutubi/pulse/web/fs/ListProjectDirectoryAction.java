package com.zutubi.pulse.web.fs;

import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.MasterBuildPaths;
import com.opensymphony.util.TextUtils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Collections;
import java.util.Arrays;

/**
 * <class-comment/>
 */
public class ListProjectDirectoryAction extends FileSystemActionSupport
{
    private long buildId;
    private long recipeId;

    private String path;

    private MasterConfigurationManager configurationManager;
    private BuildManager buildManager;
    private ProjectManager projectManager;
    private List<Object> listing;

    public void setBuildId(long buildId)
    {
        this.buildId = buildId;
    }

    public void setRecipeId(long recipeId)
    {
        this.recipeId = recipeId;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public List<Object> getListing()
    {
        return listing;
    }

    public String execute() throws Exception
    {
        BuildResult buildResult = buildManager.getBuildResult(buildId);
        if (buildResult == null)
        {
            return SUCCESS;
        }

        projectManager.checkWrite(buildResult.getProject());

        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        File baseDir = paths.getBaseDir(buildResult.getProject(), buildResult, recipeId);

        File f = baseDir;
        if (TextUtils.stringSet(path))
        {
            f = new File(baseDir, path);
        }

        listing = new LinkedList<Object>();
        File[] files = list(f);
        if (files != null)
        {
            for (File file : files)
            {
                listing.add(new JsonFileWrapper(file));
            }
        }
        return SUCCESS;
    }

    private File[] list(File f)
    {
        File[] files = f.listFiles(new HiddenFileFilter());

        if (files != null)
        {
            Collections.sort(Arrays.asList(files), new DirectoryComparator());
        }

        return files;
    }


    /**
     * Required resource.
     *
     * @param buildManager
     */
    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    /**
     * Required resource.
     *
     * @param configurationManager
     */
    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    /**
     * Required resource.
     *
     * @param projectManager
     */
    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
