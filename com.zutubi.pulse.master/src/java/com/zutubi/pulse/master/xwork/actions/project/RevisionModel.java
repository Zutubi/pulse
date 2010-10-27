package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.tove.config.project.changeviewer.ChangeViewerConfiguration;

/**
 * Defines JSON structure for a revision.
 */
public class RevisionModel
{
    private String revisionString;
    private String link;

    public RevisionModel(String revisionString)
    {
        this.revisionString = revisionString;
    }

    public RevisionModel(Revision revision, ChangeViewerConfiguration changeViewerConfig)
    {
        this.revisionString = revision == null ? null : revision.getRevisionString();
        if (revision != null && changeViewerConfig != null)
        {
            this.link = changeViewerConfig.getRevisionURL(revision);
        }
    }

    public String getRevisionString()
    {
        return revisionString;
    }

    public void setRevisionString(String revisionString)
    {
        this.revisionString = revisionString;
    }

    public String getLink()
    {
        return link;
    }
}
