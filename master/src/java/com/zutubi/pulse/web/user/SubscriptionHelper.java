package com.zutubi.pulse.web.user;

import antlr.MismatchedTokenException;
import antlr.collections.AST;
import com.opensymphony.xwork.TextProvider;
import com.opensymphony.xwork.ValidationAware;
import com.zutubi.pulse.condition.NotifyConditionFactory;
import com.zutubi.pulse.condition.antlr.NotifyConditionLexer;
import com.zutubi.pulse.condition.antlr.NotifyConditionParser;
import com.zutubi.pulse.condition.antlr.NotifyConditionTreeParser;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.renderer.BuildResultRenderer;
import com.zutubi.pulse.renderer.TemplateInfo;

import java.io.StringReader;
import java.util.*;

/**
 */
public class SubscriptionHelper
{
    private User user;
    private ContactPoint contactPoint;
    private String template;
    private Map<String, String> availableTemplates;
    private Map<String, String> conditions;
    private Map<Long, String> allProjects;
    protected List<Long> projects = new LinkedList<Long>();

    private ProjectManager projectManager;
    private NotifyConditionFactory notifyConditionFactory;
    private TextProvider textProvider;


    public SubscriptionHelper(User user, ContactPoint contactPoint, ProjectManager projectManager, NotifyConditionFactory notifyConditionFactory, TextProvider textProvider, BuildResultRenderer buildResultRenderer)
    {
        this.user = user;
        this.contactPoint = contactPoint;
        this.projectManager = projectManager;
        this.notifyConditionFactory = notifyConditionFactory;
        this.textProvider = textProvider;
        availableTemplates = new TreeMap<String, String>();

        List<TemplateInfo> templates = buildResultRenderer.getAvailableTemplates();
        for(TemplateInfo info: templates)
        {
            availableTemplates.put(info.getTemplate(), info.getDisplay());
        }
        template = getDefaultTemplate(availableTemplates);
    }

    public String getTemplate()
    {
        return template;
    }

    public void setTemplate(String template)
    {
        this.template = template;
    }

    public List<Long> getProjects()
    {
        return projects;
    }

    public void setProjects(List<Long> projects)
    {
        this.projects = projects;
    }

    public Map<String, String> getAvailableTemplates()
    {
        return availableTemplates;
    }

    public Map getConditions()
    {
        if (conditions == null)
        {
            conditions = new LinkedHashMap<String, String>();
            conditions.put(NotifyConditionFactory.TRUE, textProvider.getText("condition.allbuilds"));
            conditions.put("not " + NotifyConditionFactory.SUCCESS, textProvider.getText("condition.allfailed"));
            conditions.put(NotifyConditionFactory.CHANGED, textProvider.getText("condition.allchanged"));
            conditions.put(NotifyConditionFactory.CHANGED + " or not " + NotifyConditionFactory.SUCCESS,
                    textProvider.getText("condition.allchangedorfailed"));
            conditions.put(NotifyConditionFactory.CHANGED_BY_ME, textProvider.getText("condition.changedbyme"));
            conditions.put(NotifyConditionFactory.CHANGED_BY_ME + " and not " + NotifyConditionFactory.SUCCESS,
                    textProvider.getText("condition.brokenbyme"));
            conditions.put("not " + NotifyConditionFactory.SUCCESS + " or " + NotifyConditionFactory.STATE_CHANGE,
                    textProvider.getText("condition.allfailedandfirstsuccess"));
            conditions.put(NotifyConditionFactory.STATE_CHANGE, textProvider.getText("condition.statechange"));
        }
        return conditions;
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

    public void updateProjects(ProjectBuildSubscription subscription)
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

    private String getDefaultTemplate(Map<String, String> availableTemplates)
    {
        String defaultTemplate = availableTemplates.keySet().iterator().next();
        String candidate = contactPoint.getDefaultTemplate();
        if(availableTemplates.containsKey(candidate))
        {
            defaultTemplate = candidate;
        }

        return defaultTemplate;
    }

    public void validateCondition(String condition, ValidationAware action)
    {
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
                action.addFieldError("condition", "line " + mte.getLine() + ":" + mte.getColumn() + ": end of input when expecting " + NotifyConditionParser._tokenNames[mte.expecting]);
            }
            else
            {
                action.addFieldError("condition", mte.toString());
            }
        }
        catch (Exception e)
        {
            action.addFieldError("condition", e.toString());
        }

    }
}
