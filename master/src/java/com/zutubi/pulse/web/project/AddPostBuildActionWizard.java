package com.zutubi.pulse.web.project;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.Validateable;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.Scope;
import com.zutubi.pulse.core.model.ResourceProperty;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.web.wizard.BaseWizard;
import com.zutubi.pulse.web.wizard.BaseWizardState;
import com.zutubi.pulse.web.wizard.Wizard;
import com.zutubi.pulse.renderer.TemplateInfo;
import com.zutubi.pulse.renderer.BuildResultRenderer;

import java.util.*;

/**
 * <class-comment/>
 */
public class AddPostBuildActionWizard extends BaseWizard
{
    private static final String EMAIL_STATE = "email";
    private static final String EXE_STATE = "exe";
    private static final String TAG_STATE = "tag";

    private long projectId;
    private long specId = 0;
    private long nodeId = 0;

    private ProjectManager projectManager;
    private BuildManager buildManager;
    private MasterConfigurationManager configurationManager;
    private BuildResultRenderer buildResultRenderer;

    private SelectActionType selectState;
    private ConfigureEmail configEmail;
    private ConfigureExe configExe;
    private ConfigureTag configTag;

    public AddPostBuildActionWizard()
    {
        selectState = new SelectActionType(this, "select");
        configEmail = new ConfigureEmail(this, EMAIL_STATE);
        configExe = new ConfigureExe(this, EXE_STATE);
        configTag = new ConfigureTag(this, TAG_STATE);

        addInitialState(selectState);
        addState(configEmail);
        addState(configExe);
        addState(configTag);
    }

    public long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public long getSpecId()
    {
        return specId;
    }

    public void setSpecId(long specId)
    {
        this.specId = specId;
    }

    public long getNodeId()
    {
        return nodeId;
    }

    public void setNodeId(long nodeId)
    {
        this.nodeId = nodeId;
    }

    public Project getProject()
    {
        return projectManager.getProject(projectId);
    }

    public boolean isStage()
    {
        return specId > 0;
    }
    
    public boolean isExe()
    {
        return EXE_STATE.equals(selectState.getType());
    }

    public void process()
    {
        Project project = getProject();
        if(project == null)
        {
            return;
        }

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
        else if (EMAIL_STATE.equals(selectState.getType()))
        {
            action = new EmailCommittersPostBuildAction(selectState.getName(),
                    project.lookupBuildSpecifications(selectState.getSpecIds()),
                    ResultState.getStatesList(selectState.getStateNames()),
                    selectState.getFailOnError(),
                    configEmail.getEmailDomain(),
                    configEmail.getTemplate(),
                    configEmail.getIgnorePulseUsers());
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

        if(specId > 0)
        {
            BuildSpecification spec = project.getBuildSpecification(specId);
            if(spec == null)
            {
                return;
            }

            BuildSpecificationNode node = spec.getNode(nodeId);
            if(node == null)
            {
                return;
            }

            node.addPostAction(action);
        }
        else
        {
            project.addPostBuildAction(action);
        }

        projectManager.save(project);
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setBuildResultRenderer(BuildResultRenderer buildResultRenderer)
    {
        this.buildResultRenderer = buildResultRenderer;
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

            if(specId > 0)
            {
                BuildSpecification spec = project.getBuildSpecification(specId);
                if(spec == null)
                {
                    return;
                }

                BuildSpecificationNode node = spec.getNode(nodeId);
                if(node == null)
                {
                    return;
                }

                if(node.getPostAction(name) != null)
                {
                    addFieldError("name", "This stage already has a post stage action with name '" + name + "'");
                }
            }
            else
            {
                if(project.getPostBuildAction(name) != null)
                {
                    addFieldError("name", "This project already has a post build action with name '" + name + "'");
                }
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

            if (specId == 0)
            {
                specs = new LinkedHashMap<Long, String>();
                List<BuildSpecification> buildSpecifications = project.getBuildSpecifications();
                Collections.sort(buildSpecifications, new NamedEntityComparator());
                for (BuildSpecification spec : buildSpecifications)
                {
                    specs.put(spec.getId(), spec.getName());
                }
            }

            states = ResultState.getCompletedStatesMap();

            if (types == null)
            {
                types = new LinkedHashMap<String, String>();
                types.put(TAG_STATE, "apply tag");
                types.put(EMAIL_STATE, "email committers");
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

    public class ConfigureEmail extends BaseWizardState
    {
        private String emailDomain;
        private String template;
        private boolean ignorePulseUsers = false;
        private Map<String, String> availableTemplates;

        public ConfigureEmail(Wizard wizard, String stateName)
        {
            super(wizard, stateName);
        }

        public void initialise()
        {
            super.initialise();
            availableTemplates = new TreeMap<String, String>();

            List<TemplateInfo> templates = buildResultRenderer.getAvailableTemplates(false);
            for(TemplateInfo info: templates)
            {
                availableTemplates.put(info.getTemplate(), info.getDisplay());
            }
            
            template = "html-email";
        }

        public String getEmailDomain()
        {
            return emailDomain;
        }

        public void setEmailDomain(String emailDomain)
        {
            this.emailDomain = emailDomain;
        }

        public String getTemplate()
        {
            return template;
        }

        public void setTemplate(String template)
        {
            this.template = template;
        }

        public Map<String, String> getAvailableTemplates()
        {
            return availableTemplates;
        }

        public boolean getIgnorePulseUsers()
        {
            return ignorePulseUsers;
        }

        public void setIgnorePulseUsers(boolean ignorePulseUsers)
        {
            this.ignorePulseUsers = ignorePulseUsers;
        }

        public String getNextStateName()
        {
            return "success";
        }
    }

    public class ConfigureExe extends BaseWizardState
    {
        private String command;
        private String arguments;
        private Scope exampleScope;

        public ConfigureExe(Wizard wizard, String stateName)
        {
            super(wizard, stateName);
        }

        public void initialise()
        {
            // TODO: different for post-stage actions
            super.initialise();

            List<BuildResult> lastBuild = buildManager.queryBuilds(new Project[] { getProject() }, new ResultState[] { ResultState.SUCCESS }, null, -1, -1, null, 0, 1, true);
            if(!lastBuild.isEmpty())
            {
                BuildResult result = lastBuild.get(0);
                List<RecipeResultNode> stages = result.getRoot().getChildren();
                RecipeResultNode node = null;

                if(isStage() && stages.size() > 0)
                {
                    node = stages.get(0);
                }

                exampleScope = RunExecutablePostBuildAction.getScope(result, node, new LinkedList<ResourceProperty>(), configurationManager);
            }
        }

        public Scope getExampleScope()
        {
            return exampleScope;
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
    }

    public class ConfigureTag extends BaseWizardState
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
    }
}
