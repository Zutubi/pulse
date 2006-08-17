package com.zutubi.pulse.web.user;

import antlr.MismatchedTokenException;
import antlr.collections.AST;
import com.zutubi.pulse.condition.NotifyConditionFactory;
import com.zutubi.pulse.condition.antlr.NotifyConditionLexer;
import com.zutubi.pulse.condition.antlr.NotifyConditionParser;
import com.zutubi.pulse.condition.antlr.NotifyConditionTreeParser;
import com.zutubi.pulse.model.*;

import java.io.StringReader;
import java.util.*;

/**
 *
 *
 */
public class SubscriptionActionSupport extends UserActionSupport
{
    private ProjectManager projectManager;

    protected long contactPointId;
    protected String condition = "true";

    protected Map<String, String> conditions;
    protected Map<Long, String> allProjects;
    protected Map<Long, String> contactPoints;

    protected User user;
    protected ContactPoint contactPoint;
    protected List<Long> projects = new LinkedList<Long>();
;
    private NotifyConditionFactory notifyConditionFactory;

    public long getContactPointId()
    {
        return contactPointId;
    }

    public void setContactPointId(long contactPointId)
    {
        this.contactPointId = contactPointId;
    }

    public List<Long> getProjects()
    {
        return projects;
    }

    public void setProjects(List<Long> projects)
    {
        this.projects = projects;
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

    public Map<Long, String> getAllProjects()
    {
        if(allProjects == null)
        {
            allProjects = new LinkedHashMap<Long, String>();
            List<Project> all = projectManager.getAllProjects();
            Collections.sort(all, new NamedEntityComparator());
            for(Project p: all)
            {
                allProjects.put(p.getId(), p.getName());
            }
        }
        return allProjects;
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
        catch(MismatchedTokenException mte)
        {
            if(mte.token.getText() == null)
            {
                addFieldError("condition", "line " + mte.getLine() + ":" + mte.getColumn() + ": end of input when expecting " + NotifyConditionParser._tokenNames[mte.expecting]);
            }
            else
            {
                addFieldError("condition", mte.toString());
            }
        }
        catch (Exception e)
        {
            addFieldError("condition", e.toString());
        }
    }

    protected void populateProjects(Subscription subscription)
    {
        for(Project p: subscription.getProjects())
        {
            projects.add(p.getId());
        }
    }

    protected void updateProjects(Subscription subscription)
    {
        List<Project> subscriptionProjects = subscription.getProjects();
        subscriptionProjects.clear();
        for(Long id: projects)
        {
            Project p = projectManager.getProject(id);
            if(p != null)
            {
                subscriptionProjects.add(p);
            }
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