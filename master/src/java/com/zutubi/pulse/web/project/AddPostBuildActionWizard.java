package com.zutubi.pulse.web.project;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.Validateable;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.web.wizard.BaseWizard;
import com.zutubi.pulse.web.wizard.BaseWizardState;
import com.zutubi.pulse.web.wizard.Wizard;
import com.zutubi.pulse.web.wizard.WizardCompleteState;

import java.util.*;

/**
 * <class-comment/>
 */
public class AddPostBuildActionWizard extends BaseWizard
{
    private static final String EXE_STATE = "exe";
    private static final String TAG_STATE = "tag";


    private long projectId;

    private ProjectManager projectManager;

    private SelectActionType selectState;
    private ConfigureTag configTag;
    private ConfigureExe configExe;

    public AddPostBuildActionWizard()
    {
        selectState = new SelectActionType(this, "select");
        configTag = new ConfigureTag(this, TAG_STATE);
        configExe = new ConfigureExe(this, EXE_STATE);

        addInitialState(selectState);
        addState(configTag);
        addState(configExe);
    }

    public long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public Project getProject()
    {
        return projectManager.getProject(projectId);
    }

    public void process()
    {
        Project project = getProject();

        PostBuildAction action = null;
        if (TAG_STATE.equals(selectState.getType()))
        {
            action = new TagPostBuildAction(selectState.getName(),
                    project.lookupBuildSpecifications(selectState.getSpecIds()),
                    ResultState.getStatesList(selectState.getStateNames()),
                    selectState.getFailOnError(),
                    configTag.getTagName(),
                    configTag.getMoveExisting());
        }
        else if (EXE_STATE.equals(selectState.getType()))
        {
            action = new RunExecutablePostBuildAction(selectState.getName(),
                    project.lookupBuildSpecifications(selectState.getSpecIds()),
                    ResultState.getStatesList(selectState.getStateNames()),
                    selectState.getFailOnError(),
                    configExe.getCommand(),
                    configExe.getArguments());
        }


        project.addPostBuildAction(action);
        projectManager.save(project);
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public class SelectActionType extends BaseWizardState implements Validateable
    {
        private Map<String, String> types;
        private Map<Long, String> specs;
        private Map<String, String> states;

        private String name;
        private String type;
        private List<Long> specIds = new LinkedList<Long>();
        private List<String> stateNames = new LinkedList<String>();
        private boolean failOnError = false;

        public SelectActionType(Wizard wizard, String name)
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

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public Map<String, String> getTypes()
        {
            return types;
        }

        public Map<Long, String> getSpecs()
        {
            return specs;
        }

        public List<Long> getSpecIds()
        {
            return specIds;
        }

        public void setSpecIds(List<Long> specIds)
        {
            this.specIds = specIds;
        }

        public Map<String, String> getStates()
        {
            return states;
        }

        public List<String> getStateNames()
        {
            return stateNames;
        }

        public void setStateNames(List<String> stateNames)
        {
            this.stateNames = stateNames;
        }

        public boolean getFailOnError()
        {
            return failOnError;
        }

        public void setFailOnError(boolean failOnError)
        {
            this.failOnError = failOnError;
        }

        public void validate()
        {
            if (!TextUtils.stringSet(type) || !types.containsKey(type))
            {
                addFieldError("type", "invalid type '" + type + "' specified. ");
                return;
            }

            // ensure that the selected name is not already in use for this project.
            Project project = getProject();
            if(project == null)
            {
                return;
            }

            if(project.getPostBuildAction(name) != null)
            {
                addFieldError("name", "This project already has a post build action with name '" + name + "'");
            }
        }

        @Override
        public void initialise()
        {
            super.initialise();

            Project project = getProject();
            if (project == null)
            {
                addActionError("Unknown project [" + projectId + "]");
                return;
            }

            specs = new LinkedHashMap<Long, String>();
            List<BuildSpecification> buildSpecifications = project.getBuildSpecifications();
            Collections.sort(buildSpecifications, new NamedEntityComparator());
            for (BuildSpecification spec : buildSpecifications)
            {
                specs.put(spec.getId(), spec.getName());
            }

            states = ResultState.getCompletedStatesMap();

            if (types == null)
            {
                types = new LinkedHashMap<String, String>();
                types.put(TAG_STATE, "apply tag");
                types.put(EXE_STATE, "run executable");
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

    public class ConfigureExe extends BaseWizardState implements Validateable
    {
        private String command;
        private String arguments;

        public ConfigureExe(Wizard wizard, String stateName)
        {
            super(wizard, stateName);
        }

        public String getCommand()
        {
            return command;
        }

        public void setCommand(String command)
        {
            this.command = command;
        }

        public String getArguments()
        {
            return arguments;
        }

        public void setArguments(String arguments)
        {
            this.arguments = arguments;
        }

        public String getNextStateName()
        {
            return "success";
        }

        public void validate()
        {
            try
            {
                RunExecutablePostBuildAction.validateArguments(arguments);
            }
            catch (Exception e)
            {
                addFieldError("arguments", e.getMessage());
            }
        }
    }

    public class ConfigureTag extends BaseWizardState implements Validateable
    {
        private String tagName;
        private boolean moveExisting = false;

        public ConfigureTag(Wizard wizard, String stateName)
        {
            super(wizard, stateName);
        }

        public String getTagName()
        {
            return tagName;
        }

        public void setTagName(String tagName)
        {
            this.tagName = tagName;
        }

        public boolean getMoveExisting()
        {
            return moveExisting;
        }

        public void setMoveExisting(boolean moveExisting)
        {
            this.moveExisting = moveExisting;
        }

        public String getNextStateName()
        {
            return "success";
        }

        public void validate()
        {
            try
            {
                TagPostBuildAction.validateTag(tagName);
            }
            catch (Exception e)
            {
                addFieldError("tagName", e.getMessage());
            }
        }
    }
}
