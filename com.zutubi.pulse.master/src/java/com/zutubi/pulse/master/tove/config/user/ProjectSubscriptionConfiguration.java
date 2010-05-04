package com.zutubi.pulse.master.tove.config.user;

import antlr.collections.AST;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.notifications.condition.FalseNotifyCondition;
import com.zutubi.pulse.master.notifications.condition.NotifyCondition;
import com.zutubi.pulse.master.notifications.condition.NotifyConditionFactory;
import com.zutubi.pulse.master.notifications.condition.TrueNotifyCondition;
import com.zutubi.pulse.master.notifications.condition.antlr.NotifyConditionLexer;
import com.zutubi.pulse.master.notifications.condition.antlr.NotifyConditionParser;
import com.zutubi.pulse.master.notifications.condition.antlr.NotifyConditionTreeParser;
import com.zutubi.pulse.master.tove.config.LabelConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.annotations.*;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.logging.Logger;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

/**
 * A subscription to results for project builds.
 */
@SymbolicName("zutubi.projectSubscriptionConfig")
@Form(fieldOrder = {"name", "allProjects", "projects", "labels", "contact", "template"})
@Classification(single = "favourite")
@Wire
public class ProjectSubscriptionConfiguration extends SubscriptionConfiguration
{
    private static final Logger LOG = Logger.getLogger(ProjectSubscriptionConfiguration.class);

    @ControllingCheckbox(uncheckedFields = {"projects", "labels"})
    boolean allProjects = false;
    /**
     * The projects to which this subscription is associated.  If empty,
     * the subscription is associated with all projects.
     */
    @Reference
    private List<ProjectConfiguration> projects = new LinkedList<ProjectConfiguration>();
    @ItemPicker(optionProvider = "com.zutubi.pulse.master.tove.config.project.ProjectLabelOptionProvider")
    private List<String> labels = new LinkedList<String>();
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

    public boolean isAllProjects()
    {
        return allProjects;
    }

    public void setAllProjects(boolean allProjects)
    {
        this.allProjects = allProjects;
    }

    public List<ProjectConfiguration> getProjects()
    {
        return projects;
    }

    public void setProjects(List<ProjectConfiguration> projects)
    {
        this.projects = projects;
    }

    public List<String> getLabels()
    {
        return labels;
    }

    public void setLabels(List<String> labels)
    {
        this.labels = labels;
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
        return !result.isPersonal() && projectMatches(result.getProject()) && getNotifyCondition().satisfied(result, configurationProvider.getAncestorOfType(this, UserConfiguration.class));
    }

    private boolean projectMatches(Project project)
    {
        if (allProjects)
        {
            return true;
        }
        else
        {
            for (ProjectConfiguration projectConfig : projects)
            {
                if (projectConfig.getProjectId() == project.getId())
                {
                    return true;
                }
            }

            List<String> projectLabels = CollectionUtils.map(project.getConfig().getLabels(), new Mapping<LabelConfiguration, String>()
            {
                public String map(LabelConfiguration labelConfiguration)
                {
                    return labelConfiguration.getLabel();
                }
            });

            for (String label : labels)
            {
                if (projectLabels.contains(label))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public NotifyCondition getNotifyCondition()
    {
        if (notifyCondition == null)
        {
            if (condition == null)
            {
                notifyCondition = new TrueNotifyCondition();
            }
            else
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
                    LOG.severe("Unable to parse subscription condition '" + condition.getExpression() + "'");
                    notifyCondition = new FalseNotifyCondition();
                }
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
