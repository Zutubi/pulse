package com.zutubi.pulse.web.fs;

import com.zutubi.pulse.model.*;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.MasterBuildPaths;
import com.zutubi.pulse.core.model.RecipeResult;
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

    private String path;

    private MasterConfigurationManager configurationManager;
    private BuildManager buildManager;
    private ProjectManager projectManager;
    private List<INode> listing;

    public void setBuildId(long buildId)
    {
        this.buildId = buildId;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public List<INode> getListing()
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

        // if build is pending, then there is nothing that we can display.

        Project project = buildResult.getProject();
        projectManager.checkWrite(project);

        final String separator = File.separator;

        // if path is empty, then show roots - build roots.
        if (!TextUtils.stringSet(path))
        {
            List<INode> details = new LinkedList<INode>();
            RecipeResultNode node = buildResult.getRoot();
            for (final RecipeResultNode child : node.getChildren())
            {
                final RecipeResult result = child.getResult();
                details.add(new INode()
                {
                    public boolean isContainer()
                    {
                        return true;
                    }

                    public String getName()
                    {
                        return "stage :: " + child.getStage() + " :: " + child.getResult().getRecipeNameSafe() + "@" + child.getHostSafe();
                    }

                    public String getSeparator()
                    {
                        return separator;
                    }

                    public String getType()
                    {
                        return "stage";
                    }

                    public String getId()
                    {
                        return String.valueOf(result.getId());
                    }
                });
            }
            listing = details;
            return SUCCESS;
        }

        // the first component of the path should be the recipe id.
        String recipeId = path;
        String remainingPath = "";

        int indexOfSep = path.indexOf(separator);
        if (indexOfSep != -1)
        {
            recipeId = path.substring(0, indexOfSep);
            remainingPath = path.substring(indexOfSep + 1);
        }

        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        File baseDir = paths.getBaseDir(project, buildResult, Long.valueOf(recipeId));

        if (!baseDir.isDirectory())
        {
            // we are unable to get a listing of the base directory, likely because it has been cleaned
            // up. Return the appropriate message.
            List l = Arrays.asList(new INode()
            {
                public boolean isContainer()
                {
                    return false;
                }

                public String getName()
                {
                    return "listing not available.";
                }

                public String getSeparator()
                {
                    return separator;
                }

                public String getType()
                {
                    return "file";
                }

                public String getId()
                {
                    return null;
                }
            });
            listing = l;
            return SUCCESS;
        }

        File f = baseDir;
        if (TextUtils.stringSet(remainingPath))
        {
            f = new File(baseDir, remainingPath);
        }

        listing = new LinkedList<INode>();
        File[] files = list(f);
        if (files != null)
        {
            for (File file : files)
            {
                listing.add(new JavaFileNode(file));
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
