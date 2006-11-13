package com.zutubi.pulse.web.project.changeviewer;

import com.zutubi.pulse.model.ChangeViewer;
import com.zutubi.pulse.model.FisheyeChangeViewer;

/**
 */
public class FisheyeChangeViewerForm extends BasePathChangeViewerForm
{
    public ChangeViewer constructChangeViewer()
    {
        return new FisheyeChangeViewer(getBaseURL(), getProjectPath());
    }
}
