package com.zutubi.pulse.web.project.changeviewer;

import com.zutubi.pulse.model.ChangeViewer;
import com.zutubi.pulse.model.ViewVCChangeViewer;

/**
 */
public class ViewVCChangeViewerForm extends BasePathChangeViewerForm
{
    public ChangeViewer constructChangeViewer()
    {
        return new ViewVCChangeViewer(getBaseURL(), getProjectPath());
    }
}
