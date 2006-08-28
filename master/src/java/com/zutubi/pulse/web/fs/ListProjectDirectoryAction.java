package com.zutubi.pulse.web.fs;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.MasterBuildPaths;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.RecipeResultNode;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * <class-comment/>
 */
public class ListProjectDirectoryAction extends AbstractProjectWorkingCopyAction
{
    /**
     * List of results.
     */
    private List<INode> listing;

    public List<INode> getListing()
    {
        return listing;
    }

    public String execute() throws Exception
    {
        BuildResult buildResult = buildManager.getBuildResult(getBuildId());

        // if build is pending, then there is nothing that we can display.
        Project project = buildResult.getProject();
        projectManager.checkWrite(project);

        // if path is empty, then show roots - build roots.
        if (!TextUtils.stringSet(getRecipeId()))
        {
            constructBuildRoots(buildResult);
            return SUCCESS;
        }

        // the first component of the path should be the recipe id.
        String recipeId = getRecipeId();
        String remainingPath = getWorkingCopyPathSegment();

        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        File baseDir = paths.getBaseDir(project, buildResult, Long.valueOf(recipeId));

        if (!baseDir.isDirectory())
        {
            // we are unable to get a listing of the base directory, likely because it has been cleaned
            // up. Return the appropriate message.
            listing = Arrays.asList((INode)new INode()
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
                    return File.separator;
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

    private void constructBuildRoots(BuildResult buildResult)
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
                    return File.separator;
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
}
