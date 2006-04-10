package com.zutubi.pulse.web.project;

import com.zutubi.pulse.core.util.IOUtils;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;
import com.uwyn.jhighlight.highlighter.XmlHighlighter;
import com.uwyn.jhighlight.renderer.XmlXhtmlRenderer;

import java.io.*;

/**
 * An action to show the build file used for a build.  The file is
 * highlighted beautifully for the benefit of the user.
 */
public class ViewBuildFileAction extends ProjectActionSupport
{
    private long id;
    private BuildResult result;
    private String classes;
    private String highlightedFile;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public Project getProject()
    {
        return result.getProject();
    }

    public BuildResult getResult()
    {
        return result;
    }

    public String getClasses()
    {
        return classes;
    }

    public String getHighlightedFile()
    {
        return highlightedFile;
    }

    public String execute()
    {
        result = getBuildManager().getBuildResult(id);
        if (result == null)
        {
            addActionError("Unknown build [" + id + "]");
            return ERROR;
        }

        FileInputStream is = null;
        ByteArrayOutputStream os = null;

        try
        {
            is = new FileInputStream(new File(result.getOutputDir(), BuildResult.CINNABO_FILE));
            os = new ByteArrayOutputStream();
            XmlXhtmlRenderer renderer = new XmlXhtmlRenderer();
            renderer.highlight(BuildResult.CINNABO_FILE, is, os, null, true);
            highlightedFile = os.toString();
        }
        catch (IOException e)
        {
            // Ignore.
        }
        finally
        {
            IOUtils.close(is);
            IOUtils.close(os);
        }

        return SUCCESS;
    }
}
