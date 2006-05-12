/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.project;

import com.zutubi.pulse.scheduling.CronTrigger;
import com.zutubi.pulse.scheduling.Trigger;
import com.zutubi.pulse.model.Capture;
import com.zutubi.pulse.model.DirectoryCapture;

/**
 */
public class EditDirectoryArtifactAction extends AbstractEditArtifactAction
{
    protected boolean verifyCapture(Capture capture)
    {
        return capture instanceof DirectoryCapture;
    }
}
