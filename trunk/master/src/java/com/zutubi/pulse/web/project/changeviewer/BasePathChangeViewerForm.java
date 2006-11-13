package com.zutubi.pulse.web.project.changeviewer;

import com.zutubi.pulse.form.descriptor.annotation.Form;
import com.zutubi.pulse.form.descriptor.annotation.Text;
import com.zutubi.pulse.model.BasePathChangeViewer;
import com.zutubi.validation.annotations.Required;

/**
 */
@Form(fieldOrder = { "baseURL", "projectPath" })
public abstract class BasePathChangeViewerForm implements ChangeViewerForm
{
    private String baseURL;
    private String projectPath;

    public void initialise(BasePathChangeViewer viewer)
    {
        baseURL = viewer.getBaseURL();
        projectPath = viewer.getProjectPath();
    }

    @Required @Text(size = 60)
    public String getBaseURL()
    {
        return baseURL;
    }

    public void setBaseURL(String baseURL)
    {
        this.baseURL = baseURL;
    }

    @Text(size = 60)
    public String getProjectPath()
    {
        return projectPath;
    }

    public void setProjectPath(String projectPath)
    {
        this.projectPath = projectPath;
    }
}
