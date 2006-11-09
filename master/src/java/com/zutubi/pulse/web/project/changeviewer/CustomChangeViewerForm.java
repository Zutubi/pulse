package com.zutubi.pulse.web.project.changeviewer;

import com.zutubi.pulse.model.CustomChangeViewer;
import com.zutubi.pulse.model.ChangeViewer;
import com.zutubi.pulse.form.descriptor.annotation.Form;
import com.zutubi.pulse.form.descriptor.annotation.Text;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;

/**
 */
@Form(fieldOrder = { "changesetURL", "fileViewURL", "fileDownloadURL", "fileDiffURL" })
public class CustomChangeViewerForm implements ChangeViewerForm, Validateable
{
    private CustomChangeViewer viewer = new CustomChangeViewer();

    public void initialise(CustomChangeViewer customChangeViewer)
    {
        // Don't reuse the object: it is stored in the HTTP session with the
        // wizard and this does not mix well with Hibernate sessions.
        viewer = (CustomChangeViewer) customChangeViewer.copy();
    }

    @Text(size = 60)
    public String getChangesetURL()
    {
        return viewer.getChangesetURL();
    }

    public void setChangesetURL(String changesetURL)
    {
        viewer.setChangesetURL(changesetURL);
    }

    @Text(size = 60)
    public String getFileViewURL()
    {
        return viewer.getFileViewURL();
    }

    public void setFileViewURL(String fileViewURL)
    {
        viewer.setFileViewURL(fileViewURL);
    }

    @Text(size = 60)
    public String getFileDownloadURL()
    {
        return viewer.getFileDownloadURL();
    }

    public void setFileDownloadURL(String fileDownloadURL)
    {
        viewer.setFileDownloadURL(fileDownloadURL);
    }

    @Text(size = 60)
    public String getFileDiffURL()
    {
        return viewer.getFileDiffURL();
    }

    public void setFileDiffURL(String fileDiffURL)
    {
        viewer.setFileDiffURL(fileDiffURL);
    }

    public ChangeViewer constructChangeViewer()
    {
        return viewer;
    }

    public void validate(ValidationContext context)
    {
        try
        {
            CustomChangeViewer.validateChangesetURL(getChangesetURL());
        }
        catch(IllegalArgumentException e)
        {
            context.addFieldError("changesetURL", e.getMessage());
        }

        validateFileURL(context, "fileViewURL", getFileViewURL());
        validateFileURL(context, "fileDownloadURL", getFileDownloadURL());
        validateFileURL(context, "fileDiffURL", getFileDiffURL());
    }

    private void validateFileURL(ValidationContext context, String field, String url)
    {
        try
        {
            CustomChangeViewer.validateFileURL(url);
        }
        catch(IllegalArgumentException e)
        {
            context.addFieldError(field, e.getMessage());
        }
    }
}
