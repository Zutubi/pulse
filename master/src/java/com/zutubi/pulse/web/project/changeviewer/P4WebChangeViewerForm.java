package com.zutubi.pulse.web.project.changeviewer;

import com.zutubi.pulse.form.descriptor.annotation.Form;
import com.zutubi.pulse.form.descriptor.annotation.Text;
import com.zutubi.pulse.model.ChangeViewer;
import com.zutubi.pulse.model.P4WebChangeViewer;
import com.zutubi.validation.annotations.Required;

/**
 */
@Form(fieldOrder = "baseURL")
public class P4WebChangeViewerForm implements ChangeViewerForm
{
    private String baseURL;

    public ChangeViewer constructChangeViewer()
    {
        return new P4WebChangeViewer(baseURL, "");
    }

    public void initialise(P4WebChangeViewer p4WebChangeViewer)
    {
        baseURL = p4WebChangeViewer.getBaseURL();
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
