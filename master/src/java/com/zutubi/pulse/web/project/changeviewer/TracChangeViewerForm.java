package com.zutubi.pulse.web.project.changeviewer;

import com.zutubi.pulse.form.descriptor.annotation.Form;
import com.zutubi.pulse.form.descriptor.annotation.Text;
import com.zutubi.pulse.model.ChangeViewer;
import com.zutubi.pulse.model.TracChangeViewer;
import com.zutubi.validation.annotations.Required;

/**
 */
@Form(fieldOrder = "baseURL")
public class TracChangeViewerForm implements ChangeViewerForm
{
    private String baseURL;

    public ChangeViewer constructChangeViewer()
    {
        return new TracChangeViewer(baseURL, "");
    }

    public void initialise(TracChangeViewer tracChangeViewer)
    {
        baseURL = tracChangeViewer.getBaseURL();
    }

    @Required
    @Text(size = 60)
    public String getBaseURL()
    {
        return baseURL;
    }

    public void setBaseURL(String baseURL)
    {
        this.baseURL = baseURL;
    }
}
