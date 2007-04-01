package com.zutubi.prototype.wizard.webwork;

import com.zutubi.prototype.config.ConfigurationPersistenceManager;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.TypeProperty;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.MutableRecordImpl;
import com.zutubi.prototype.type.record.TemplateRecord;
import com.zutubi.prototype.wizard.WizardState;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.util.logging.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * This wizard walks a user through the project configuration process. During project configuration,
 * a user needs to configure the projects type, scm and general details.
 */
public class ConfigureProjectWizard extends AbstractTypeWizard
{
    private static final Logger LOG = Logger.getLogger(ConfigureProjectWizard.class);

    private static final String[] PATHS = new String[]{"general", "scm", "type"};

    private ConfigurationPersistenceManager configurationPersistenceManager;

    // TODO: record id of parent project template.
    private long parentId;

    // TODO: create actual project in the database.
    private ProjectManager projectManager;

    private static final TemplateRecord EMPTY_RECORD = new TemplateRecord("empty", null, new MutableRecordImpl());

    private Map<String, WizardState> recordStates = new HashMap<String, WizardState>();
    private String path;
    private CompositeType projectType;

    public void initialise()
    {
        // load template information.
        LOG.warning("TODO: load template record for project wizard, currently using empty template record. Parent ID: " + parentId);

        TemplateRecord templateRecord = EMPTY_RECORD;

        // to create a project, we need to specify 3 sets of data.
        // a) general info, in particular, the name, which must be unique.
        // b) the scm type
        // c) and the project type.

        // the path at which we are storing this data, the path that triggers this wizard, is project
        String basePath = "project";

        Map<String, CompositeType> wizardTypes = new HashMap<String, CompositeType>();

        projectType = configurationPersistenceManager.getTargetType(basePath, CompositeType.class);

        // the types associated with the paths that require configuration are determined as follows:
        for (String propertyPath : PATHS)
        {
            TypeProperty property = projectType.getProperty(propertyPath);
            wizardTypes.put(propertyPath, (CompositeType) property.getType());
        }

        // these wizard types now define the UI forms that we will be seeing.

        // - general info represents one page, with some nice simple data.
        // - scm represents two pages, one to select the scm, the next to configure it.
        // - type represents two pages, one to select the type, the next to configure it.

        // each one of these represents a single piece of data, even if they take more than one step.
        // they contain small wizards themselves.

        // So, now we want to initialise this wizard with the information that we have.
        wizardStates = new LinkedList<WizardState>();
        for (String propertyPath : PATHS)
        {
            CompositeType propertyType = wizardTypes.get(propertyPath);

            TemplateRecord stateTemplateRecord = (TemplateRecord) templateRecord.get(propertyPath);

            // convert the type into wizard state(s).
            recordStates.put(propertyPath, addWizardStates(wizardStates, propertyType, stateTemplateRecord));
        }

        currentState = wizardStates.getFirst();
    }

    public void setParentId(long parentId)
    {
        this.parentId = parentId;
    }

    public void doFinish()
    {
        // here it gets a little interesting, and potentially too much to automatically handle without further
        // assistance. We need to store the records that we have from the states. We have the relative paths
        // at which to store them, but we do not know at this stage exactly where to store them.  That is, in this case,
        // we do not know the project id that will be used to reference this project.  Maybe we should leave that up to
        // the dude that understands the project/ scope and how it relates to the external world.  If that is the case,
        // then we just store this baby.
        MutableRecord record = projectType.createNewRecord();

        for (Map.Entry<String, WizardState> entry : recordStates.entrySet())
        {
            record.put(entry.getKey(), entry.getValue().getRecord());
        }

        successPath = configurationPersistenceManager.insertRecord("project", record);
    }

    /**
     * Required resource.
     *
     * @param configurationPersistenceManager
     *         instance
     */
    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }

    /**
     * Required resource.
     *
     * @param typeRegistry instance
     */
    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    /**
     * Required resource
     *
     * @param projectManager instance
     */
    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
