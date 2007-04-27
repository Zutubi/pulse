package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.EmailCommittersPostBuildAction;
import com.zutubi.pulse.model.PostBuildAction;
import com.zutubi.pulse.renderer.BuildResultRenderer;
import com.zutubi.pulse.renderer.TemplateInfo;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 */
public class EditEmailActionAction extends AbstractEditPostBuildActionAction
{
    // Create it so webwork doesn't try to
    private EmailCommittersPostBuildAction action = new EmailCommittersPostBuildAction();
    private Map<String, String> availableTemplates;
    private BuildResultRenderer buildResultRenderer;
    private boolean ignorePulseUsers = false;

    public boolean isEmail()
    {
        return true;
    }

    public boolean isIgnorePulseUsers()
    {
        return ignorePulseUsers;
    }

    public void setIgnorePulseUsers(boolean ignorePulseUsers)
    {
        this.ignorePulseUsers = ignorePulseUsers;
    }

    public Map<String, String> getAvailableTemplates()
    {
        return availableTemplates;
    }

    public void prepare() throws Exception
    {
        super.prepare();
        if(hasErrors())
        {
            return;
        }

        PostBuildAction a = lookupAction();

        if(a == null)
        {
            return;
        }

        if (!(a instanceof EmailCommittersPostBuildAction))
        {
            addActionError("Invalid post build action type '" + a.getType() + "'");
            return;
        }

        action = (EmailCommittersPostBuildAction) a;
        availableTemplates = new TreeMap<String, String>();

        List<TemplateInfo> templates = buildResultRenderer.getAvailableTemplates(false);
        for(TemplateInfo info: templates)
        {
            availableTemplates.put(info.getTemplate(), info.getDisplay());
        }
    }

    public String doInput()
    {
        if(hasErrors())
        {
            return ERROR;
        }

        ignorePulseUsers = action.isIgnorePulseUsers();
        return super.doInput();
    }

    public EmailCommittersPostBuildAction getPostBuildAction()
    {
        action.setIgnorePulseUsers(ignorePulseUsers);
        return action;
    }

    public void setBuildResultRenderer(BuildResultRenderer buildResultRenderer)
    {
        this.buildResultRenderer = buildResultRenderer;
    }
}
