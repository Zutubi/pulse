package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.PostBuildAction;
import com.zutubi.pulse.model.TagPostBuildAction;

/**
 */
public class EditTagActionAction extends AbstractEditPostBuildActionAction
{
    // Create it so webwork doesn't try to
    private TagPostBuildAction action = new TagPostBuildAction();
    private boolean moveExisting = false;

    public boolean getMoveExisting()
    {
        return moveExisting;
    }

    public void setMoveExisting(boolean moveExisting)
    {
        this.moveExisting = moveExisting;
    }

    public void prepare() throws Exception
    {
        super.prepare();
        if(hasErrors())
        {
            return;
        }

        PostBuildAction a = getProject().getPostBuildAction(getId());
        if (a == null)
        {
            addActionError("Unknown post build action [" + getId() + "]");
            return;
        }

        if (!(a instanceof TagPostBuildAction))
        {
            addActionError("Invalid post build action type '" + a.getType() + "'");
            return;
        }

        action = (TagPostBuildAction) a;
    }

    public String doInput()
    {
        moveExisting = action.getMoveExisting();        
        return super.doInput();
    }

    public void validate()
    {
        super.validate();

        try
        {
            TagPostBuildAction.validateTag(action.getTag());
        }
        catch (Exception e)
        {
            addFieldError("postBuildAction.tag", e.getMessage());
        }
    }

    public String execute()
    {
        action.setMoveExisting(moveExisting);
        return super.execute();
    }

    public TagPostBuildAction getPostBuildAction()
    {
        return action;
    }
}
