package com.zutubi.pulse.model;

import antlr.collections.AST;
import com.zutubi.pulse.condition.FalseNotifyCondition;
import com.zutubi.pulse.condition.NotifyCondition;
import com.zutubi.pulse.condition.NotifyConditionFactory;
import com.zutubi.pulse.condition.TrueNotifyCondition;
import com.zutubi.pulse.condition.antlr.NotifyConditionLexer;
import com.zutubi.pulse.condition.antlr.NotifyConditionParser;
import com.zutubi.pulse.condition.antlr.NotifyConditionTreeParser;
import com.zutubi.pulse.util.logging.Logger;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

/**
 * A subscription to build results for a set of projects.
 */
public class ProjectBuildSubscription extends Subscription
{
    private static final Logger LOG = Logger.getLogger(ProjectBuildSubscription.class);

    /**
     * Condition to be satisfied before notifying.  Not currently used for
     * personal builds.
     */
    private String condition;

    /**
     * The projects to which this subscription is associated.  If empty,
     * the subscription is associated with all projects.
     */
    private List<Project> projects = new LinkedList<Project>();

    /**
     * Cache of the parsed and modelled condition.
     */
    private NotifyCondition notifyCondition = null;

    /**
     * A reference to the systems notify condition factory, used for instantiating
     * the subscriptions condition.
     */
    private NotifyConditionFactory notifyFactory;

    public ProjectBuildSubscription()
    {
        super();
    }

    /**
     * Constructs a new subscription connection the given event with the given
     * contact point.
     *
     * @param contactPoint the contact point to notify on the event
     * @param template name of the template to use to render builds for this
     *                 subscription
     * @param condition the condition to satisfy before this subscription fires
     */
    public ProjectBuildSubscription(ContactPoint contactPoint, String template, String condition)
    {
        super(contactPoint, template);
        this.condition = condition;
    }

    public ProjectBuildSubscription(ContactPoint contactPoint, String template, List<Project> projects, String condition)
    {
        this(contactPoint, template, condition);
        this.projects = projects;
    }

    public List<Project> getProjects()
    {
        return projects;
    }

    public void setProjects(List<Project> projects)
    {
        this.projects = projects;
    }

    /**
     * Sets the given condition as that which must be satisfied before the
     * contact point should be notified.
     *
     * @param condition the condition to set.
     */
    public void setCondition(String condition)
    {
        this.condition = condition;
    }

    public String getCondition()
    {
        return this.condition;
    }

    public boolean conditionSatisfied(BuildResult result)
    {
        return !result.isPersonal() && getNotifyCondition().satisfied(result, getContactPoint().getUser());
    }

    public NotifyCondition getNotifyCondition()
    {
        if(notifyCondition == null)
        {
            // Need to parse our condition.
            try
            {
                NotifyConditionLexer lexer = new NotifyConditionLexer(new StringReader(condition));

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

    public void setNotifyConditionFactory(NotifyConditionFactory notifyFactory)
    {
        this.notifyFactory = notifyFactory;
    }
}
