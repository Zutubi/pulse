package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.model.Entity;
import com.zutubi.util.EnumUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a slave server that builds may be farmed out to.
 */
public class AgentState extends Entity implements CommentContainer
{
    /**
     * Persistent agents states.  The minimum we need to remember even across
     * restarts to ensure disabled slaves stay that way.
     */
    public enum EnableState
    {
        ENABLED,
        DISABLED,
        DISABLING;

        /**
         * Returns a human-readble version of this constant.
         *
         * @return a pretty version of this constant
         */
        public String getPrettyString()
        {
            return EnumUtils.toPrettyString(this);
        }
    }

    private EnableState enableState = EnableState.ENABLED;
    /**
     * Descriptive comments left by users on this agent.
     */
    private List<Comment> comments = new LinkedList<Comment>();

    public AgentState()
    {

    }

    public boolean isDisabled()
    {
        return enableState == EnableState.DISABLED;
    }

    public boolean isDisabling()
    {
        return enableState == EnableState.DISABLING;
    }

    public boolean isEnabled()
    {
        return enableState == EnableState.ENABLED || enableState == EnableState.DISABLING;
    }

    public EnableState getEnableState()
    {
        return enableState;
    }

    public void setEnableState(EnableState enableState)
    {
        this.enableState = enableState;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private String getEnableStateName()
    {
        return enableState.toString();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private void setEnableStateName(String name)
    {
        this.enableState = EnableState.valueOf(name);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public List<Comment> getComments()
    {
        return comments;
    }

    public void addComment(Comment comment)
    {
        comments.add(comment);
    }

    public boolean removeComment(Comment comment)
    {
        return comments.remove(comment);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setComments(List<Comment> comments)
    {
        this.comments = comments;
    }
}
