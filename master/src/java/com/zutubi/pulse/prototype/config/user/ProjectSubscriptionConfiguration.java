package com.zutubi.pulse.prototype.config.user;

import antlr.collections.AST;
import com.zutubi.config.annotations.*;
import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.pulse.condition.FalseNotifyCondition;
import com.zutubi.pulse.condition.NotifyCondition;
import com.zutubi.pulse.condition.NotifyConditionFactory;
import com.zutubi.pulse.condition.TrueNotifyCondition;
import com.zutubi.pulse.condition.antlr.NotifyConditionLexer;
import com.zutubi.pulse.condition.antlr.NotifyConditionParser;
import com.zutubi.pulse.condition.antlr.NotifyConditionTreeParser;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.util.logging.Logger;

import java.io.StringReader;
import java.util.List;

/**
 * A subscription to results for project builds.
 */
@SymbolicName("zutubi.projectSubscriptionConfig")
@Form(fieldOrder = {"name", "projects", "contact", "template"})
@Classification(single = "favourite")
@Wire
public class ProjectSubscriptionConfiguration extends SubscriptionConfiguration
{
    private static final Logger LOG = Logger.getLogger(ProjectSubscriptionConfiguration.class);

    /**
     * The projects to which this subscription is associated.  If empty,
     * the subscription is associated with all projects.
     */
    @Reference
    private List<ProjectConfiguration> projects;
    /**
     * Condition to be satisfied before notifying.
     */
    private SubscriptionConditionConfiguration condition;
    @Select(optionProvider = "SubscriptionTemplateOptionProvider")
    private String template;

    /**
     * Cache of the parsed and modelled condition.
     */
    @Transient
    private NotifyCondition notifyCondition = null;

    private ConfigurationProvider configurationProvider;
    private NotifyConditionFactory notifyFactory;

    public List<ProjectConfiguration> getProjects()
    {
        return projects;
    }

    public void setProjects(List<ProjectConfiguration> projects)
    {
        this.projects = projects;
    }

    public SubscriptionConditionConfiguration getCondition()
    {
        return condition;
    }

    public void setCondition(SubscriptionConditionConfiguration condition)
    {
        this.condition = condition;
    }

    public boolean conditionSatisfied(BuildResult result)
    {
        return !result.isPersonal() && getNotifyCondition().satisfied(result, configurationProvider.getAncestorOfType(this, UserConfiguration.class));
    }

    public NotifyCondition getNotifyCondition()
    {
        if(notifyCondition == null)
        {
            // Need to parse our condition.
            try
            {
                NotifyConditionLexer lexer = new NotifyConditionLexer(new StringReader(condition.getExpression()));

                NotifyConditionParser parser = new NotifyConditionParser(lexer);
                parser.condition();
                AST t = parser.getAST();
                if(t == null)
                {
                    // Empty expression evals to true
                    notifyCondition = new TrueNotifyCondition();
                }
                else
                {
                    NotifyConditionTreeParser tree = new NotifyConditionTreeParser();
                    tree.setNotifyConditionFactory(notifyFactory);
                    notifyCondition = tree.cond(t);
                }
            }
            catch (Exception e)
            {
                LOG.severe("Unable to parse subscription condition '" + condition + "'");
                notifyCondition = new FalseNotifyCondition();
            }
        }

        return notifyCondition;
    }

    public String getTemplate()
    {
        return template;
    }

    public void setTemplate(String template)
    {
        this.template = template;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }

    public void setNotifyConditionFactory(NotifyConditionFactory notifyFactory)
    {
        this.notifyFactory = notifyFactory;
    }
}
