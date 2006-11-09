package com.zutubi.pulse.web.project.changeviewer;

import com.zutubi.pulse.model.ChangeViewer;
import com.zutubi.pulse.model.TracChangeViewer;

/**
 */
public class TracChangeViewerForm extends BasePathChangeViewerForm
{
    public ChangeViewer constructChangeViewer()
    {
        return new TracChangeViewer(getBaseURL(), getProjectPath());
    }
}
