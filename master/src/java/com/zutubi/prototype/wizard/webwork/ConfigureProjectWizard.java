package com.zutubi.prototype.wizard.webwork;

import com.zutubi.prototype.config.ConfigurationPersistenceManager;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.MutableRecordImpl;
import com.zutubi.prototype.type.record.TemplateRecord;
import com.zutubi.prototype.wizard.WizardState;
import com.zutubi.util.logging.Logger;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.model.AntPulseFileDetails;

import java.util.LinkedList;

/**
 * This wizard walks a user through the project configuration process. During project configuration,
 * a user needs to configure the projects type, scm and general details.
 */
public class ConfigureProjectWizard extends AbstractTypeWizard
{
    private static final Logger LOG = Logger.getLogger(ConfigureProjectWizard.class);

    private static final TemplateRecord EMPTY_RECORD = new TemplateRecord("empty", null, new MutableRecordImpl());

    private CompositeType projectType;
    private ProjectManager projectManager;

    public void initialise()
    {
        //todo: load template information.

        TemplateRecord templateRecord = EMPTY_RECORD;

        projectType = (CompositeType) configurationPersistenceManager.getType("project").getTargetType();
        
        CompositeType scmType = (CompositeType) projectType.getProperty("scm").getType();
        CompositeType typeType = (CompositeType) projectType.getProperty("type").getType();

        wizardStates = new LinkedList<WizardState>();
        addWizardStates(wizardStates, projectType, templateRecord);
        addWizardStates(wizardStates, scmType, (TemplateRecord) templateRecord.get("scm"));
        addWizardStates(wizardStates, typeType, (TemplateRecord) templateRecord.get("type"));

        currentState = wizardStates.getFirst();
    }

    public void doFinish()
    {
        MutableRecord record = projectType.createNewRecord();
        record.update(wizardStates.get(0).getRecord());
        record.put("scm", wizardStates.get(2).getRecord());
        record.put("type", wizardStates.get(4).getRecord());

        // FIXME: Pulse file details temporary for testing
        AntPulseFileDetails pulseFileDetails = new AntPulseFileDetails();
        pulseFileDetails.setBuildFile("build.xml");
        Project project = new Project((String) record.get("name"), (String) record.get("description"), pulseFileDetails);
        projectManager.save(project);
        record.put("projectId", Long.toString(project.getId()));

        successPath = configurationPersistenceManager.insertRecord("project", record);
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
