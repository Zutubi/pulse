package com.zutubi.pulse.web.user;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.Validateable;
import com.zutubi.pulse.condition.NotifyConditionFactory;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.renderer.BuildResultRenderer;
import com.zutubi.pulse.security.AcegiUtils;
import com.zutubi.pulse.web.wizard.BaseWizard;
import com.zutubi.pulse.web.wizard.BaseWizardState;
import com.zutubi.pulse.web.wizard.Wizard;

import java.util.*;

/**
 * <class-comment/>
 */
public class SubscriptionWizard extends BaseWizard
{
    private static final String PERSONAL_BUILDS_STATE = "personal";
    private static final String PROJECT_BUILDS_STATE = "project";

    private long userId;

    private UserManager userManager;
    private ProjectManager projectManager;
    private NotifyConditionFactory notifyConditionFactory;
    private BuildResultRenderer buildResultRenderer;
    private SubscriptionManager subscriptionManager;

    private SelectSubscriptionType selectState;
    private ConfigurePersonal configPersonal;
    private ConfigureProject configProject;

    public SubscriptionWizard()
    {
        selectState = new SelectSubscriptionType(this, "select");
        configPersonal = new ConfigurePersonal(this, PERSONAL_BUILDS_STATE);
        configProject = new ConfigureProject(this, PROJECT_BUILDS_STATE);

        addInitialState(selectState);
        addState(configPersonal);
        addState(configProject);
    }

    public long getUserId()
    {
        return userId;
    }

    public void setUserId(long userId)
    {
        this.userId = userId;
    }

    public User getUser()
    {
        String principle = AcegiUtils.getLoggedInUser();
        if(principle != null)
        {
            return userManager.getUser(principle);
        }
        return null;
    }

    public void process()
    {
        ContactPoint contactPoint = getUser().getContactPoint(selectState.getContactId());
        Subscription subscription;
        if(selectState.getType().equals(PERSONAL_BUILDS_STATE))
        {
            subscription = new PersonalBuildSubscription(contactPoint, configPersonal.getTemplate());
        }
        else
        {
            subscription = new ProjectBuildSubscription(contactPoint, configProject.getTemplate(), configProject.condition);
            configProject.helper.updateProjects((ProjectBuildSubscription) subscription);
        }

        subscriptionManager.save(subscription);
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public void setBuildResultRenderer(BuildResultRenderer buildResultRenderer)
    {
        this.buildResultRenderer = buildResultRenderer;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setNotifyConditionFactory(NotifyConditionFactory notifyConditionFactory)
    {
        this.notifyConditionFactory = notifyConditionFactory;
    }

    public void setSubscriptionManager(SubscriptionManager subscriptionManager)
    {
        this.subscriptionManager = subscriptionManager;
    }

    public class SelectSubscriptionType extends BaseWizardState implements Validateable
    {
        private Map<String, String> types;
        private Map<Long, String> contacts;

        private String type;
        private Long contactId;

        public SelectSubscriptionType(Wizard wizard, String name)
        {
            super(wizard, name);
        }

        public String getType()
        {
            return type;
        }

        public void setType(String type)
        {
            this.type = type;
        }

        public Map<String, String> getTypes()
        {
            return types;
        }

        public Map<Long, String> getContacts()
        {
            return contacts;
        }

        public Long getContactId()
        {
            return contactId;
        }

        public void setContactId(Long contactId)
        {
            this.contactId = contactId;
        }

        public void validate()
        {
            if (!TextUtils.stringSet(type) || !types.containsKey(type))
            {
                addFieldError("type", "invalid type '" + type + "' specified. ");
            }

            if(getUser().getContactPoint(contactId) == null)
            {
                addFieldError("contactId", "Unknown contact point [" + contactId + "]");
            }
        }

        @Override
        public void initialise()
        {
            super.initialise();

            User user = getUser();
            if (user == null)
            {
                addActionError("Unknown user [" + userId + "]");
                return;
            }

            contacts = new LinkedHashMap<Long, String>();
            List<ContactPoint> contactPoints = user.getContactPoints();
            Collections.sort(contactPoints, new NamedEntityComparator());
            for (ContactPoint contact : contactPoints)
            {
                contacts.put(contact.getId(), contact.getName());
            }

            if (types == null)
            {
                types = new LinkedHashMap<String, String>();
                types.put(PERSONAL_BUILDS_STATE, "personal builds");
                types.put(PROJECT_BUILDS_STATE, "project builds");
            }
        }

        public String getNextStateName()
        {
            if (TextUtils.stringSet(type))
            {
                return type;
            }
            return super.getStateName();
        }
    }


    public class ConfigurePersonal extends BaseWizardState
    {
        private SubscriptionHelper helper;

        public ConfigurePersonal(Wizard wizard, String stateName)
        {
            super(wizard, stateName);
        }

        public String getTemplate()
        {
            return helper.getTemplate();
        }

        public void setTemplate(String template)
        {
            helper.setTemplate(template);
        }

        public Map<String, String> getAvailableTemplates()
        {
            return helper.getAvailableTemplates();
        }

        public void initialise()
        {
            Long contactId = selectState.getContactId();
            helper = new SubscriptionHelper(true, getUser(), getUser().getContactPoint(contactId), projectManager, notifyConditionFactory, getTextProvider(), buildResultRenderer);
        }

        public String getNextStateName()
        {
            return "success";
        }
    }

    public class ConfigureProject extends BaseWizardState implements Validateable
    {
        private SubscriptionHelper helper;
        private String conditionType;
        private List<String> selectedConditions = new LinkedList<String>();
        private int repeatedX = 5;
        private String repeatedUnits;
        private String expression;
        private ProjectBuildCondition condition = new AllProjectBuildCondition();

        public ConfigureProject(Wizard wizard, String stateName)
        {
            super(wizard, stateName);
        }

        public String getTemplate()
        {
            return helper.getTemplate();
        }

        public void setTemplate(String template)
        {
            helper.setTemplate(template);
        }

        public List<Long> getProjects()
        {
            return helper.getProjects();
        }

        public void setProjects(List<Long> projects)
        {
            helper.setProjects(projects);
        }

        public String getConditionType()
        {
            return conditionType;
        }

        public void setConditionType(String conditionType)
        {
            this.conditionType = conditionType;
        }

        public List<String> getSelectedConditions()
        {
            return selectedConditions;
        }

        public void setSelectedConditions(List<String> selectedConditions)
        {
            this.selectedConditions = selectedConditions;
        }

        public int getRepeatedX()
        {
            return repeatedX;
        }

        public void setRepeatedX(int repeatedX)
        {
            this.repeatedX = repeatedX;
        }

        public String getRepeatedUnits()
        {
            return repeatedUnits;
        }

        public void setRepeatedUnits(String repeatedUnits)
        {
            this.repeatedUnits = repeatedUnits;
        }

        public String getExpression()
        {
            return expression;
        }

        public void setExpression(String expression)
        {
            this.expression = expression;
        }

        public ProjectBuildCondition getCondition()
        {
            return condition;
        }

        public void setCondition(ProjectBuildCondition condition)
        {
            this.condition = condition;
        }

        public Map<String, String> getAvailableTemplates()
        {
            return helper.getAvailableTemplates();
        }

        public Map getSelectedOptions()
        {
            return helper.getConditions();
        }

        public Map<Long, String> getAllProjects()
        {
            return helper.getAllProjects();
        }

        public void initialise()
        {
            Long contactId = selectState.getContactId();
            helper = new SubscriptionHelper(false, getUser(), getUser().getContactPoint(contactId), projectManager, notifyConditionFactory, getTextProvider(), buildResultRenderer);
            conditionType = condition.getType();
            expression = condition.getExpression();
        }

        public String getNextStateName()
        {
            return "success";
        }

        public void validate()
        {
            helper.validateCondition(conditionType, selectedConditions, repeatedX, repeatedUnits, expression, this);
        }

        public void execute()
        {
            condition = helper.createCondition(conditionType, selectedConditions, repeatedX, repeatedUnits, expression);
        }
    }
}
