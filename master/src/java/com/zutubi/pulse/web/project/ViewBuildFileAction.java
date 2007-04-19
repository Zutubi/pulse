package com.zutubi.pulse.web.project;

import com.uwyn.jhighlight.renderer.XmlXhtmlRenderer;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;
import com.zutubi.util.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

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
    private MasterConfigurationManager configurationManager;

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

        checkPermissions(result);        

        FileInputStream is = null;
        ByteArrayOutputStream os = null;

        try
        {
            is = new FileInputStream(new File(result.getAbsoluteOutputDir(configurationManager.getDataDirectory()), BuildResult.PULSE_FILE));
            os = new ByteArrayOutputStream();
            XmlXhtmlRenderer renderer = new XmlXhtmlRenderer();
            renderer.highlight(BuildResult.PULSE_FILE, is, os, null, true);
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

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
