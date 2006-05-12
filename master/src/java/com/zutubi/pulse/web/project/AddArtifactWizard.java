/********************************************************************************
  @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.*;
import com.zutubi.pulse.scheduling.*;
import com.zutubi.pulse.scheduling.tasks.BuildProjectTask;
import com.zutubi.pulse.scm.SCMChangeEvent;
import com.zutubi.pulse.util.logging.Logger;
import com.zutubi.pulse.web.wizard.BaseWizard;
import com.zutubi.pulse.web.wizard.BaseWizardState;
import com.zutubi.pulse.web.wizard.Wizard;
import com.zutubi.pulse.web.wizard.WizardCompleteState;
import com.zutubi.pulse.PostProcessorManager;
import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.Validateable;

import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import java.util.LinkedList;

import org.apache.tools.ant.DirectoryScanner;

/**
 * <class-comment/>
 */
public class AddArtifactWizard extends BaseWizard
{
    private static final String DIRECTORY_STATE = "dir";
    private static final String FILE_STATE = "file";
    private static final String PROCESSORS_STATE = "processors";

    private static final Logger LOG = Logger.getLogger(AddArtifactWizard.class);

    private long projectId;
    private Project project;
    private ProjectManager projectManager;
    private PostProcessorManager postProcessorManager;

    private SelectArtifactType selectState;
    private ConfigureDirectoryArtifact configDir;
    private ConfigureFileArtifact configFile;
    private ConfigureProcessors configProcessors;

    public AddArtifactWizard()
    {
        selectState = new SelectArtifactType(this, "select");
        configDir = new ConfigureDirectoryArtifact(this, DIRECTORY_STATE);
        configFile = new ConfigureFileArtifact(this, FILE_STATE);
        configProcessors = new ConfigureProcessors(this);

        addInitialState("select", selectState);
        addState(configDir);
        addState(configFile);
        addState(configProcessors);
        addFinalState("success", new WizardCompleteState(this, "success"));
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
        if(project == null)
        {
            project = projectManager.getProject(projectId);
        }
        return project;
    }

    public void process()
    {
        Project project = projectManager.getProject(getProjectId());

        Capture capture;

        if (DIRECTORY_STATE.equals(selectState.getType()))
        {
            capture = configDir.getCapture();
        }
        else
        {
            capture = configFile.getCapture();
        }

        for(String processor: configProcessors.getProcessors())
        {
            capture.addProcessor(processor);
        }

        capture.clearFields();

        TemplatePulseFileDetails details = (TemplatePulseFileDetails) project.getPulseFileDetails();
        details.addCapture(capture);
        projectManager.save(project);
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setPostProcessorManager(PostProcessorManager postProcessorManager)
    {
        this.postProcessorManager = postProcessorManager;
    }

    public class SelectArtifactType extends BaseWizardState implements Validateable
    {
        private Map<String, String> types;
        private String name;
        private String type;

        public SelectArtifactType(Wizard wizard, String name)
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

        public void validate()
        {
            if (!TextUtils.stringSet(type) || !types.containsKey(type))
            {
                addFieldError("type", "Invalid type '" + type + "' specified.");
                return;
            }

            // Ensure that the selected name is not already in use for this project.
            TemplatePulseFileDetails details = (TemplatePulseFileDetails) getProject().getPulseFileDetails();
            if (details.getCapture(name) != null)
            {
                addFieldError("name", "the name " + name + " is already being used.");
            }
        }

        @Override
        public void initialise()
        {
            super.initialise();

            if (getProject() == null)
            {
                addActionError("Unknown project [" + projectId + "]");
                return;
            }

            if (types == null)
            {
                types = new TreeMap<String, String>();
                types.put(DIRECTORY_STATE, "directory artifact");
                types.put(FILE_STATE, "file artifact");
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

    public class ConfigureDirectoryArtifact extends BaseWizardState
    {
        private DirectoryCapture capture;

        public ConfigureDirectoryArtifact(Wizard wizard, String stateName)
        {
            super(wizard, stateName);
        }

        public DirectoryCapture getCapture()
        {
            if(capture == null)
            {
                capture = new DirectoryCapture(selectState.getName());
            }

            return capture;
        }

        public String getNextStateName()
        {
            return PROCESSORS_STATE;
        }
    }

    public class ConfigureFileArtifact extends BaseWizardState
    {
        private FileCapture capture;

        public ConfigureFileArtifact(Wizard wizard, String stateName)
        {
            super(wizard, stateName);
        }

        public FileCapture getCapture()
        {
            if(capture == null)
            {
                capture = new FileCapture(selectState.getName(), null);
            }

            return capture;
        }

        public String getNextStateName()
        {
            return PROCESSORS_STATE;
        }
    }

    public class ConfigureProcessors extends BaseWizardState
    {
        private Map<String, String> processorList;
        private List<String> processors = new LinkedList<String>();

        public ConfigureProcessors(Wizard wizard)
        {
            super(wizard, PROCESSORS_STATE);
        }

        public Map<String, String> getProcessorList()
        {
            if(processorList == null)
            {
                processorList = postProcessorManager.getAvailableProcessors();
            }
            return processorList;
        }

        public List<String> getProcessors()
        {
            return processors;
        }

        public void setProcessors(List<String> processors)
        {
            this.processors = processors;
        }

        public String getNextStateName()
        {
            return "success";
        }
    }
}
