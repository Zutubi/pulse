/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.user;

import com.zutubi.pulse.ProjectNameComparator;
import com.zutubi.pulse.condition.NotifyConditionFactory;
import com.zutubi.pulse.condition.FalseNotifyCondition;
import com.zutubi.pulse.condition.antlr.NotifyConditionLexer;
import com.zutubi.pulse.condition.antlr.NotifyConditionParser;
import com.zutubi.pulse.condition.antlr.NotifyConditionTreeParser;
import com.zutubi.pulse.model.*;

import java.util.*;
import java.io.StringReader;

import antlr.collections.AST;

/**
 *
 *
 */
public class SubscriptionActionSupport extends UserActionSupport
{
    private ProjectManager projectManager;

    protected long projectId;
    protected long contactPointId;
    protected String condition = "true";

    protected Map<String, String> conditions;
    protected List<Project> projects;
    protected Map<Long, String> contactPoints;

    protected User user;
    protected Project project;
    protected ContactPoint contactPoint;
    private NotifyConditionFactory notifyConditionFactory;

    public long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public long getContactPointId()
    {
        return contactPointId;
    }

    public void setContactPointId(long contactPointId)
    {
        this.contactPointId = contactPointId;
    }

    public String getCondition()
    {
        return condition;
    }

    public void setCondition(String condition)
    {
        this.condition = condition;
    }

    public Map getConditions()
    {
        if (conditions == null)
        {
            conditions = new LinkedHashMap<String, String>();
            conditions.put(NotifyConditionFactory.TRUE, getText("condition.allbuilds"));
            conditions.put("not " + NotifyConditionFactory.SUCCESS, getText("condition.allfailed"));
            conditions.put(NotifyConditionFactory.CHANGED, getText("condition.allchanged"));
            conditions.put(NotifyConditionFactory.CHANGED + " or not " + NotifyConditionFactory.SUCCESS,
                    getText("condition.allchangedorfailed"));
            conditions.put(NotifyConditionFactory.CHANGED_BY_ME, getText("condition.changedbyme"));
            conditions.put(NotifyConditionFactory.CHANGED_BY_ME + " and not " + NotifyConditionFactory.SUCCESS,
                    getText("condition.brokenbyme"));
            conditions.put("not " + NotifyConditionFactory.SUCCESS + " or " + NotifyConditionFactory.STATE_CHANGE,
                    getText("condition.allfailedandfirstsuccess"));
            conditions.put(NotifyConditionFactory.STATE_CHANGE, getText("condition.statechange"));
        }
        return conditions;
    }

    public List<Project> getProjects()
    {
        if(projects == null)
        {
            projects = projectManager.getAllProjects();
            Collections.sort(projects, new ProjectNameComparator());
        }
        return projects;
    }

    public Map<Long, String> getContactPoints()
    {
        if(contactPoints == null)
        {
            contactPoints = new TreeMap<Long, String>();
            for (ContactPoint contact : user.getContactPoints())
            {
                contactPoints.put(contact.getId(), contact.getName());
            }
        }
        
        return contactPoints;
    }

    public void setup()
    {
        lookupUser();
        if(hasErrors())
        {
            return;
        }
    }

    protected void lookupUser()
    {
        user = getUser();
        if (user == null)
        {
            addUnknownUserActionError();
        }
    }

    public void validate()
    {
        if (hasErrors())
        {
            return;
        }

        user = getUser();
        if (user == null)
        {
            addUnknownUserActionError();
            return;
        }

        project = projectManager.getProject(projectId);
        if (project == null)
        {
            addActionError("Unknown project [" + projectId + "]");
            return;
        }

        contactPoint = user.getContactPoint(contactPointId);
        if (contactPoint == null)
        {
            addFieldError("contactPointId", "Unknown contact point '" + contactPointId + "' for user '" + user.getName() + "'");
        }

        // Parse the condition
        try
        {
            NotifyConditionLexer lexer = new NotifyConditionLexer(new StringReader(condition));
            NotifyConditionParser parser = new NotifyConditionParser(lexer);
            parser.orexpression();
            AST t = parser.getAST();
            if(t != null)
            {
                NotifyConditionTreeParser tree = new NotifyConditionTreeParser();
                tree.setNotifyConditionFactory(notifyConditionFactory);
                tree.cond(t);
            }
        }
        catch (Exception e)
        {
            addFieldError("condition", e.getMessage());
        }
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setNotifyConditionFactory(NotifyConditionFactory notifyConditionFactory)
    {
        this.notifyConditionFactory = notifyConditionFactory;
    }
}