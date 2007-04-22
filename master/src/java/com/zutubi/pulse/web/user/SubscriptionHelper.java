package com.zutubi.pulse.web.user;

import antlr.MismatchedTokenException;
import antlr.NoViableAltException;
import antlr.collections.AST;
import com.opensymphony.xwork.TextProvider;
import com.opensymphony.xwork.ValidationAware;
import com.zutubi.pulse.condition.NotifyCondition;
import com.zutubi.pulse.condition.NotifyConditionFactory;
import com.zutubi.pulse.condition.antlr.NotifyConditionLexer;
import com.zutubi.pulse.condition.antlr.NotifyConditionParser;
import com.zutubi.pulse.condition.antlr.NotifyConditionTreeParser;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.renderer.BuildResultRenderer;
import com.zutubi.pulse.renderer.TemplateInfo;

import java.io.StringReader;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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


    public SubscriptionHelper(boolean personal, User user, ContactPoint contactPoint, ProjectManager projectManager, NotifyConditionFactory notifyConditionFactory, TextProvider textProvider, BuildResultRenderer buildResultRenderer)
    {
        this.user = user;
        this.contactPoint = contactPoint;
        this.projectManager = projectManager;
        this.notifyConditionFactory = notifyConditionFactory;
        this.textProvider = textProvider;
        availableTemplates = new TreeMap<String, String>();

        List<TemplateInfo> templates = buildResultRenderer.getAvailableTemplates(personal);
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

    public Map<String, String> getConditions()
    {
        if (conditions == null)
        {
            conditions = new LinkedHashMap<String, String>();
            conditions.put("not " + NotifyConditionFactory.SUCCESS, textProvider.getText("condition.unsuccessful"));
            conditions.put(NotifyConditionFactory.CHANGED, textProvider.getText("condition.allchanged"));
            conditions.put(NotifyConditionFactory.CHANGED_BY_ME, textProvider.getText("condition.changedbyme"));
            conditions.put(NotifyConditionFactory.STATE_CHANGE, textProvider.getText("condition.statechange"));
        }
        return conditions;
    }

    public Map<Pattern, String> getConditionToTypeMap()
    {
        Map<Pattern, String> conditionToType = new HashMap<Pattern, String>();
        for(Object o: getConditions().keySet())
        {
            String condition = (String) o;
            conditionToType.put(Pattern.compile(Pattern.quote(condition)), "selected");
        }

        conditionToType.put(Pattern.compile(NotifyConditionFactory.TRUE), "all");
        conditionToType.put(Pattern.compile("unsuccessful.count.builds == ([0-9]+)"), "repeated");
        conditionToType.put(Pattern.compile("unsuccessful.count.days >= ([0-9]+) and unsuccessful.count.days(previous) < \\1"), "repeated");
        return conditionToType;
    }

    public String getConditionType(String condition)
    {
        for(Map.Entry<Pattern, String> entry: getConditionToTypeMap().entrySet())
        {
            Matcher matcher = entry.getKey().matcher(condition);
            if(matcher.matches())
            {
                return entry.getValue();
            }
        }

        return "advanced";
    }

    public Map<Long, String> getAllProjects()
    {
        if(allProjects == null)
        {
            allProjects = new LinkedHashMap<Long, String>();
            List<Project> all = projectManager.getNameToConfig();
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

    public void validateCondition(String type, List<String> simpleConditions, int repeatedX, String repeatedUnits, String expression, ValidationAware action)
    {
        if(type.equals("advanced"))
        {
            validateCondition(expression, action, notifyConditionFactory);
        }
        else if(type.equals("repeated"))
        {
            if(repeatedX <= 0)
            {
                action.addFieldError("repeatedX", "value must be a positive integer");
            }
        }

    }

    public static NotifyCondition validateCondition(String condition, ValidationAware action, NotifyConditionFactory notifyConditionFactory)
    {
        try
        {
            NotifyConditionLexer lexer = new NotifyConditionLexer(new StringReader(condition));
            NotifyConditionParser parser = new NotifyConditionParser(lexer);
            parser.condition();
            AST t = parser.getAST();
            if(t != null)
            {
                NotifyConditionTreeParser tree = new NotifyConditionTreeParser();
                tree.setNotifyConditionFactory(notifyConditionFactory);
                return tree.cond(t);
            }
        }
        catch(MismatchedTokenException mte)
        {
            if(mte.token.getText() == null)
            {
                action.addFieldError("expression", "line " + mte.getLine() + ":" + mte.getColumn() + ": end of input when expecting " + NotifyConditionParser._tokenNames[mte.expecting]);
            }
            else
            {
                action.addFieldError("expression", mte.toString());
            }
        }
        catch(NoViableAltException nvae)
        {
            if(nvae.token.getText() == null)
            {
                action.addFieldError("expression", "line " + nvae.getLine() + ":" + nvae.getColumn() + ": unexpected end of input");
            }
            else
            {
                action.addFieldError("expression", nvae.toString());
            }
        }
        catch (Exception e)
        {
            action.addFieldError("expression", e.toString());
        }

        return null;
    }

    public ProjectBuildCondition createCondition(String type, List<String> simpleConditions, int repeatedX, String repeatedUnits, String expression)
    {
        if(type.equals("all"))
        {
           return new AllProjectBuildCondition();
        }
        else if(type.equals("simple"))
        {
            return new SimpleProjectBuildCondition(simpleConditions);
        }
        else if(type.equals("repeated"))
        {
            return new RepeatedFailuresProjectBuildCondition(repeatedX, repeatedUnits);
        }
        else
        {
            return new AdvancedProjectBuildCondition(expression);
        }
    }
}
