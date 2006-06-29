package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.util.logging.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A CommitMessageTransformer is used to search for a pattern in SCM commit
 * messages and replace it with a different string when shown in the UI.  The
 * primary use case is to link to external tools (e.g. link "bug 123" to the
 * web UI for the bug tracker).
 */
public class CommitMessageTransformer extends Entity implements NamedEntity
{
    private static final Logger LOG = Logger.getLogger(CommitMessageTransformer.class);

    private String name;
    private List<Long> projects = new LinkedList<Long>();
    /**
     * The regular expression to search for in commit messages.
     */
    private String expression;
    /**
     * Replacement string (may refer to groups in the expression.
     */
    private String replacement;
    private Pattern pattern = null;

    public CommitMessageTransformer()
    {
    }

    public CommitMessageTransformer(String name, String expression, String replacement)
    {
        this.name = name;
        this.expression = expression;
        this.replacement = replacement;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<Long> getProjects()
    {
        return projects;
    }

    public void setProjects(List<Long> projects)
    {
        this.projects = projects;
    }

    public String getExpression()
    {
        return expression;
    }

    public void setExpression(String expression)
    {
        this.expression = expression;
    }

    public String getReplacement()
    {
        return replacement;
    }

    public void setReplacement(String replacement)
    {
        this.replacement = replacement;
    }

    public boolean appliesToChangelist(Changelist changelist)
    {
        if(projects == null || projects.size() == 0)
        {
            return true;
        }

        for(Long id: projects)
        {
            if(changelist.getProjectIds().contains(id))
            {
                return true;
            }
        }

        return false;
    }

    public String transform(String message)
    {
        if(pattern == null)
        {
            pattern = Pattern.compile(expression);
        }

        Matcher matcher = pattern.matcher(message);
        String r = "<a href='" + replacement + "'>$0</a>";

        try
        {
            return matcher.replaceAll(r);
        }
        catch (IndexOutOfBoundsException e)
        {
            LOG.warning("Unable to apply commit message link '" + name + "': " + e.getMessage(), e);
            return message;
        }
    }

}
