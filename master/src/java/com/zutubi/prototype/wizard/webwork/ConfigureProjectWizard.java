package com.zutubi.prototype.wizard.webwork;

import com.zutubi.prototype.FormDescriptor;
import com.zutubi.prototype.FormDescriptorFactory;
import com.zutubi.prototype.config.ConfigurationPersistenceManager;
import com.zutubi.prototype.model.Field;
import com.zutubi.prototype.model.Form;
import com.zutubi.prototype.model.SubmitField;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.prototype.wizard.Wizard;
import com.zutubi.prototype.wizard.WizardState;
import com.zutubi.prototype.wizard.WizardTransition;
import static com.zutubi.prototype.wizard.WizardTransition.*;
import com.zutubi.pulse.license.LicenseException;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectManager;

import java.util.Arrays;
import java.util.List;

/**
 * This wizard walks a user through the project configuration process. During project configuration,
 * a user needs to configure the projects type, scm and general details. 
 *
 */
public class ConfigureProjectWizard implements Wizard
{
    private RecordManager recordManager;
    private ProjectManager projectManager;
    private ConfigurationPersistenceManager configurationPersistenceManager;
    private TypeRegistry typeRegistry;

    private WizardState scmState;
    private WizardState typeState;
    private WizardStepOne selectState;

    private WizardState currentState;

    private long parentId;
    
    private Record inheritedRecord;

    public void initialise()
    {
        try
        {
            Record existingProject = configurationPersistenceManager.getRecord("project/" + parentId);
            if (existingProject != null)
            {
                inheritedRecord = existingProject.clone();
            }

            CompositeType scmType = (CompositeType) typeRegistry.getType("scmConfig");
            CompositeType typeType = (CompositeType) typeRegistry.getType("typeConfig");

            selectState = new WizardStepOne(scmType.getExtensions(), typeType.getExtensions(), inheritedRecord);
            currentState = selectState;
        }
        catch (CloneNotSupportedException e)
        {
            // never going to happen.
        }
    }

    public void setParentId(long parentId)
    {
        this.parentId = parentId;
    }

    public WizardState getCurrentState()
    {
        return currentState;
    }

    public List<WizardTransition> getAvailableActions()
    {
        if (currentState == selectState)
        {
            return Arrays.asList(NEXT, CANCEL);
        }
        if (currentState == scmState)
        {
            return Arrays.asList(PREVIOUS, NEXT, CANCEL);
        }
        if (currentState == typeState)
        {
            return Arrays.asList(PREVIOUS, FINISH, CANCEL);
        }
        // should never get here. If so, something is wrong, so return cancel.
        return Arrays.asList(CANCEL);
    }

    public WizardState doNext()
    {
        try
        {
            if (currentState == selectState)
            {
                Type selectedScmType = typeRegistry.getType(selectState.getScm());
                scmState = new ConfigurationState(selectedScmType);
                currentState = scmState;
            }
            else if (currentState == scmState)
            {
                Type selectedTyeType = typeRegistry.getType(selectState.getType());
                typeState = new ConfigurationState(selectedTyeType);
                currentState = typeState;
            }
            return currentState;
        }
        catch (TypeException e)
        {
            e.printStackTrace();
            return currentState;
        }
    }

    public WizardState doPrevious()
    {
        if (currentState == typeState)
        {
            currentState = scmState;
        }
        else if (currentState == scmState)
        {
            currentState = selectState;
        }
        return currentState;
    }

    public void doFinish()
    {
        // create a new project.
        // use that projects id as the basis for storing the scm and type information.

        try
        {
            Project project = new Project();
            
            project.setName("test" + System.currentTimeMillis());
            project.setDescription("description");
            
            projectManager.create(project);
            long projectId = project.getId();

            String projectPath = "project/" + projectId;

            // need a better way to handle this first part..

            MutableRecord projectRecord = new MutableRecord();
            projectRecord.setSymbolicName("projectConfig");
            recordManager.store(projectPath, projectRecord);

            configurationPersistenceManager.setInstance(projectPath + "/scm", scmState.getData());
            configurationPersistenceManager.setInstance(projectPath + "/type", scmState.getData());
        }
        catch (LicenseException e)
        {
            e.printStackTrace();
        }
        catch (TypeException e)
        {
            e.printStackTrace();
        }
    }

    public WizardState doRestart()
    {
        currentState = selectState;
        return currentState;
    }

    public void doCancel()
    {
        // cleanup any resources.
    }

    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public static class WizardStepOne implements WizardState
    {
        /**
         * The selected scm implementation type
         */
        private String scm;

        /**
         * The selected project implementation type.
         */
        private String type;

        private List<String> scmOptions;
        private List<String> typeOptions;

        private Record record;

        public WizardStepOne(List<String> scmOptions, List<String> typeOptions, Record record)
        {
            this.record = record;
            this.scmOptions = scmOptions;
            this.typeOptions = typeOptions;
        }

        public String getName()
        {
            return getClass().getName();
        }

        public Object getData()
        {
            return this;
        }

        public String getScm()
        {
            return scm;
        }

        public void setScm(String scm)
        {
            this.scm = scm;
        }

        public String getType()
        {
            return type;
        }

        public void setType(String type)
        {
            this.type = type;
        }

        public Form getForm()
        {
            Form form = new Form();
            form.setId(getClass().getName());
            Field scmSelectField = new Field("scm", "scm", "select", scm);
            scmSelectField.addParameter("list", scmOptions);
            scmSelectField.setTabindex(1);
            
            if (record.containsKey("scm"))
            { 
                scmSelectField.setValue(((Record)record.get("scm")).getSymbolicName());
                scmSelectField.addParameter("disabled", true);
            }

            Field typeSelectField = new Field("type", "type", "select", type);
            typeSelectField.addParameter("list", typeOptions);
            typeSelectField.setTabindex(2);
            
            if (record.containsKey("type"))
            {
                typeSelectField.setValue(((Record)record.get("type")).getSymbolicName());
                typeSelectField.addParameter("disabled", true);
            }

            Field hiddenStateField = new Field("state", "state", "hidden", getName());
            form.add(hiddenStateField);

            form.add(scmSelectField);
            form.add(typeSelectField);
            form.add(new SubmitField("next").setTabindex(3));
            form.add(new SubmitField("cancel").setTabindex(4));

            return form;
        }
    }

    public class ConfigurationState implements WizardState
    {
        private Type configType;
        private Object configInstance;
        private String name;

        public ConfigurationState(Type type) throws TypeException
        {
            try
            {
                this.configType = type;
                this.configInstance = type.getClazz().newInstance();
                this.name = type.getClazz().getSimpleName();
            }
            catch (Exception e)
            {
                throw new TypeException(e);
            }
        }

        public String getName()
        {
            return name;
        }

        public Object getData()
        {
            return configInstance;
        }

        public Form getForm()
        {
            FormDescriptorFactory formFactory = new FormDescriptorFactory();
            formFactory.setTypeRegistry(typeRegistry);
            FormDescriptor formDescriptor = formFactory.createDescriptor(configType);

            // where do we get the data from?
            Form form = formDescriptor.instantiate(null);

            Field state = new Field();
            state.addParameter("name", "state");
            state.addParameter("type", "hidden");
            state.addParameter("value", getName());
            form.add(state);

            for (WizardTransition transition : getAvailableActions())
            {
                form.add(new SubmitField(transition.name().toLowerCase()));
            }
            return form;
        }

    }
    
}
