package com.zutubi.pulse.master.rest.actions;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.zutubi.pulse.core.dependency.ivy.IvyStatus;
import com.zutubi.pulse.core.resources.api.ResourcePropertyConfiguration;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmContext;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.master.build.queue.graph.BuildGraphData;
import com.zutubi.pulse.master.build.queue.graph.GraphBuilder;
import com.zutubi.pulse.master.build.queue.graph.GraphFilters;
import com.zutubi.pulse.master.model.NamedManualTriggerBuildReason;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.model.TriggerOptions;
import com.zutubi.pulse.master.rest.errors.ValidationException;
import com.zutubi.pulse.master.scm.ScmClientUtils;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.security.SecurityUtils;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.ManualTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.TriggerUtils;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.api.ActionResult;
import com.zutubi.tove.config.docs.TypeDocs;
import com.zutubi.tove.ui.model.ActionModel;
import com.zutubi.tove.ui.model.forms.CheckboxFieldModel;
import com.zutubi.tove.ui.model.forms.DropdownFieldModel;
import com.zutubi.tove.ui.model.forms.FormModel;
import com.zutubi.tove.ui.model.forms.TextFieldModel;
import com.zutubi.util.StringUtils;
import com.zutubi.util.adt.TreeNode;
import com.zutubi.util.bean.ObjectFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zutubi.pulse.master.scm.ScmClientUtils.withScmClient;
import static com.zutubi.tove.config.docs.PropertyDocs.createFromUnencodedString;

/**
 * Custom action handler for project triggers.
 */
public class ProjectConfigurationTriggerHandler implements ActionHandler
{
    private static final String FIELD_VERSION = "version";
    private static final String LABEL_VERSION = "version";
    private static final String FIELD_MATURITY = "maturity";
    private static final String LABEL_MATURITY = "maturity";
    private static final String FIELD_REBUILD = "rebuild";
    private static final String LABEL_REBUILD = "trigger with dependencies";
    private static final String FIELD_REVISION = "revision";
    private static final String LABEL_REVISION = "revision";
    private static final String FIELD_PRIORITY = "priority";
    private static final String LABEL_PRIORITY = "priority";

    private static final String FIELD_PREFIX_PROJECT = "project_";
    private static final String FIELD_PREFIX_TRIGGER = "project_";

    private ConfigurationProvider configurationProvider;
    private ProjectManager projectManager;
    private ScmManager scmManager;
    private ObjectFactory objectFactory;

    @Override
    public ActionModel getModel(String path, final String variant)
    {
        ProjectConfiguration project = getProjectConfig(path);
        ManualTriggerConfiguration trigger = getTriggerConfig(project, variant);

        ActionModel model = new ActionModel("trigger", variant, variant, trigger.isPrompt());
        if (trigger.isPrompt())
        {
            FormModel form = new FormModel();
            Map<String, Object> formDefaults = new HashMap<>();
            TypeDocs typeDocs = new TypeDocs();
            typeDocs.setBrief("The following properties can be configured for the build.");

            form.addField(new TextFieldModel(FIELD_VERSION, LABEL_VERSION));
            formDefaults.put(FIELD_VERSION, project.getDependencies().getVersion());
            typeDocs.addProperty(createFromUnencodedString(FIELD_VERSION, LABEL_VERSION, "The build version number, e.g. 1.0.4."));

            form.addField(new DropdownFieldModel(FIELD_MATURITY, LABEL_MATURITY, IvyStatus.getStatuses()));
            formDefaults.put(FIELD_MATURITY, project.getDependencies().getStatus());
            typeDocs.addProperty(createFromUnencodedString(FIELD_MATURITY, LABEL_MATURITY, "The build maturity status, indicating how stable it is."));

            if (hasDependencyOfBuildableStatus(project))
            {
                form.addField(new CheckboxFieldModel(FIELD_REBUILD, LABEL_REBUILD));
                formDefaults.put(FIELD_REBUILD, trigger.isRebuildUpstreamDependencies());
                typeDocs.addProperty(createFromUnencodedString(FIELD_REBUILD, LABEL_REBUILD, "If checked, all upstream dependencies will be rebuilt first."));
            }

            form.addField(new TextFieldModel(FIELD_REVISION, "revision"));
            typeDocs.addProperty(createFromUnencodedString(FIELD_REVISION, LABEL_REVISION, "The build revision, leave blank to build the latest revision at build activation time."));

            form.addField(new TextFieldModel(FIELD_PRIORITY, "priority"));
            formDefaults.put(FIELD_PRIORITY, "0");
            typeDocs.addProperty(createFromUnencodedString(FIELD_PRIORITY, LABEL_PRIORITY, "If set to non-zero, the priority of the build (higher priority builds go first)."));

            Map<String, ResourcePropertyConfiguration> triggerProperties = trigger.getProperties();
            for (ResourcePropertyConfiguration property: project.getProperties().values())
            {
                if (!triggerProperties.containsKey(property.getName()))
                {
                    addPropertyField(form, formDefaults, typeDocs, property, FIELD_PREFIX_PROJECT);
                }
            }

            for (ResourcePropertyConfiguration property : triggerProperties.values())
            {
                addPropertyField(form, formDefaults, typeDocs, property, FIELD_PREFIX_TRIGGER);
            }

            model.setForm(form);
            model.setFormDefaults(formDefaults);
            model.setDocs(typeDocs);
        }

        return model;
    }

    private void addPropertyField(FormModel form, Map<String, Object> formDefaults, TypeDocs typeDocs, ResourcePropertyConfiguration property, String prefix)
    {
        String fieldName = prefix + property.getName();
        TextFieldModel field = new TextFieldModel(fieldName, property.getName());
        form.addField(field);
        formDefaults.put(fieldName, property.getValue());
        typeDocs.addProperty(createFromUnencodedString(fieldName, property.getName(), property.getDescription()));
    }

    private boolean hasDependencyOfBuildableStatus(ProjectConfiguration project)
    {
        if (project.hasDependencies())
        {
            String ourStatus = project.getDependencies().getStatus();
            GraphBuilder builder = objectFactory.buildBean(GraphBuilder.class);
            GraphFilters filters = objectFactory.buildBean(GraphFilters.class);
            TreeNode<BuildGraphData> upstream = builder.buildUpstreamGraph(project,
                    filters.status(ourStatus),
                    filters.transitive(),
                    filters.duplicate());
            return upstream.getChildren().size() > 0;
        }

        return false;
    }

    @Override
    public ActionResult doAction(String path, String variant, Map<String, Object> input)
    {
        ProjectConfiguration projectConfig = getProjectConfig(path);
        Project project = projectManager.getProject(projectConfig.getProjectId(), false);
        if (project == null)
        {
            throw new IllegalArgumentException("No project state for '" + projectConfig.getName() + "', or project is invalid");
        }

        ManualTriggerConfiguration triggerConfig = getTriggerConfig(projectConfig, variant);

        Revision revision = null;
        TriggerOptions options = new TriggerOptions(new NamedManualTriggerBuildReason(triggerConfig.getName(), SecurityUtils.getLoggedInUsername()), ProjectManager.TRIGGER_CATEGORY_MANUAL);
        if (triggerConfig.isPrompt())
        {
            final String revisionString = getStringProperty(input, FIELD_REVISION);
            if (StringUtils.stringSet(revisionString))
            {
                try
                {
                    revision = withScmClient(projectConfig, project.getState(), scmManager, new ScmClientUtils.ScmContextualAction<Revision>()
                    {
                        public Revision process(ScmClient client, ScmContext context) throws ScmException
                        {
                            return client.parseRevision(context, revisionString);
                        }
                    });
                }
                catch (ScmException e)
                {
                    throwValidationError(e, FIELD_REVISION, "Unable to verify revision: " + e.getMessage());
                }
            }

            // We apply properties to the configuration used for the build so they will be accessed
            // and applied in the same way regardless of whether they came from a prompt.  This
            // maintains property precedence and avoids duplication of the properties in the scope
            // (which can cause issues like CIB-3090).
            projectConfig = configurationProvider.deepClone(projectConfig);
            mapProperties(input, projectConfig, triggerConfig);

            options.setStatus(getStringProperty(input, FIELD_MATURITY));
            options.setVersion(getStringProperty(input, FIELD_VERSION));
            Object rebuild = input.get(FIELD_REBUILD);
            options.setRebuild(rebuild != null && rebuild instanceof Boolean && ((Boolean) rebuild));

            String priority = getStringProperty(input, FIELD_PRIORITY);
            if (StringUtils.stringSet(priority))
            {
                try
                {
                    options.setPriority(Integer.valueOf(priority));
                }
                catch (NumberFormatException e)
                {
                    throwValidationError(e, FIELD_PRIORITY, "Priority must be an integer");
                }
            }
        }
        else
        {
            options.setRebuild(triggerConfig.isRebuildUpstreamDependencies());
        }

        options.setProperties(triggerConfig.getProperties().values());

        try
        {
            projectManager.triggerBuild(projectConfig, options, revision);
        }
        catch (Exception e)
        {
            return new ActionResult(ActionResult.Status.FAILURE, e.getMessage());
        }

        return new ActionResult(ActionResult.Status.SUCCESS, "trigger '" + triggerConfig.getName() + "' fired");
    }

    private String getStringProperty(Map<String, Object> input, String name)
    {
        Object value = input.get(name);
        if (value instanceof String)
        {
            return ((String) value).trim();
        }

        return null;
    }

    private void mapProperties(Map<String, Object> input, ProjectConfiguration projectConfig, ManualTriggerConfiguration triggerConfig)
    {
        for(String name: input.keySet())
        {
            if (name.startsWith(FIELD_PREFIX_PROJECT))
            {
                String propertyName = name.substring(FIELD_PREFIX_PROJECT.length());
                updateProperty(input, projectConfig.getProperty(propertyName), name);
            }
            else if (name.startsWith(FIELD_PREFIX_TRIGGER))
            {
                String propertyName = name.substring(FIELD_PREFIX_TRIGGER.length());
                updateProperty(input, triggerConfig.getProperties().get(propertyName), name);
            }
        }
    }

    private void updateProperty(Map<String, Object> input, ResourcePropertyConfiguration property, String name)
    {
        if (property != null)
        {
            Object value = input.get(name);
            if(value instanceof String)
            {
                property.setValue((String) value);
            }
        }
    }

    private void throwValidationError(Exception cause, String field, String message)
    {
        ValidationException validationException = new ValidationException();
        validationException.initCause(cause);
        validationException.addFieldError(field, message);
        throw validationException;
    }

    private ProjectConfiguration getProjectConfig(String path)
    {
        ProjectConfiguration project = configurationProvider.get(path, ProjectConfiguration.class);
        if (project == null)
        {
            throw new IllegalArgumentException("Path '" + path + "' does not exist");
        }
        return project;
    }

    private ManualTriggerConfiguration getTriggerConfig(ProjectConfiguration project, final String name)
    {
        List<ManualTriggerConfiguration> triggers = TriggerUtils.getTriggers(project, ManualTriggerConfiguration.class);
        Optional<ManualTriggerConfiguration> oTrigger = Iterables.tryFind(triggers, new Predicate<ManualTriggerConfiguration>()
        {
            @Override
            public boolean apply(ManualTriggerConfiguration input)
            {
                return input.getName().equals(name);
            }
        });

        if (!oTrigger.isPresent())
        {
            throw new IllegalArgumentException("Project '" + project.getName() + "' has no manual trigger named '" + name + "'");
        }

        return oTrigger.get();
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }
}
