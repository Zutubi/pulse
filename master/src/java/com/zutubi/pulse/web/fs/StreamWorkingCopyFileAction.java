package com.zutubi.pulse.web.fs;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.MasterBuildPaths;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;

/**
 * <class-comment/>
 */
public class StreamWorkingCopyFileAction extends AbstractProjectWorkingCopyAction
{
    private InputStream inputStream;

    private String contentType;

    private String filename;

    public String getFilename()
    {
        return filename;
    }

    public InputStream getInputStream()
    {
        return inputStream;
    }

    public String getContentType()
    {
        return contentType;
    }

    public String execute() throws Exception
    {
        BuildResult buildResult = buildManager.getBuildResult(getBuildId());

        // if build is pending, then there is nothing that we can display.
        Project project = buildResult.getProject();
        projectManager.checkWrite(project);

        // the first component of the path should be the recipe id.
        String recipeId = getRecipeId();
        String remainingPath = getWorkingCopyPathSegment();

        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        File baseDir = paths.getBaseDir(project, buildResult, Long.valueOf(recipeId));

        File file = baseDir;
        if (TextUtils.stringSet(remainingPath))
        {
            file = new File(baseDir, remainingPath);
        }

        filename = file.getName();
        contentType = FileSystemUtils.getMimeType(file);
        inputStream = new FileInputStream(file);

        return SUCCESS;
    }
}
