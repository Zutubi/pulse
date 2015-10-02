package com.zutubi.pulse.master.rest;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.rest.errors.NotFoundException;
import com.zutubi.pulse.master.rest.errors.ValidationException;
import com.zutubi.pulse.master.rest.model.*;
import com.zutubi.pulse.master.rest.model.forms.DropdownFieldModel;
import com.zutubi.pulse.master.rest.model.forms.FormModel;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.handler.OptionProvider;
import com.zutubi.pulse.master.tove.handler.OptionProviderFactory;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.tove.ConventionSupport;
import com.zutubi.tove.actions.ActionManager;
import com.zutubi.tove.actions.ConfigurationAction;
import com.zutubi.tove.actions.ConfigurationActions;
import com.zutubi.tove.annotations.Combobox;
import com.zutubi.tove.config.ConfigurationRefactoringManager;
import com.zutubi.tove.config.ConfigurationReferenceManager;
import com.zutubi.tove.config.ConfigurationSecurityManager;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.api.ActionResult;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.config.api.ConfigurationCheckHandler;
import com.zutubi.tove.config.cleanup.RecordCleanupTask;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.*;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Controller to handle invocation of configuration actions.
 */
@RestController
@RequestMapping("/action")
public class ConfigActionsController
{
    private static final Logger LOG = Logger.getLogger(ConfigActionsController.class);

    @Autowired
    private ConfigurationTemplateManager configurationTemplateManager;
    @Autowired
    private MasterConfigurationRegistry configurationRegistry;
    @Autowired
    private ConfigurationSecurityManager configurationSecurityManager;
    @Autowired
    private ConfigurationReferenceManager configurationReferenceManager;
    @Autowired
    private ConfigurationRefactoringManager configurationRefactoringManager;
    @Autowired
    private ConfigModelBuilder configModelBuilder;
    @Autowired
    private ActionManager actionManager;
    @Autowired
    private TypeRegistry typeRegistry;
    @Autowired
    private FormModelBuilder formModelBuilder;
    @Autowired
    private ObjectFactory objectFactory;

    @RequestMapping(value = "delete/**", method = RequestMethod.GET)
    public ResponseEntity<CleanupTaskModel> delete(HttpServletRequest request) throws Exception
    {
        String configPath = Utils.getConfigPath(request);
        // This is to validate the path, we don't need the type.
        Utils.getType(configPath, configurationTemplateManager);

        configurationSecurityManager.ensurePermission(configPath, AccessManager.ACTION_VIEW);

        RecordCleanupTask task = configurationTemplateManager.getCleanupTasks(configPath);
        return new ResponseEntity<>(configModelBuilder.buildCleanupTask(task), HttpStatus.OK);
    }

    @RequestMapping(value = "options/**", method = RequestMethod.GET)
    public ResponseEntity<List<String>> options(HttpServletRequest request) throws Exception
    {
        String configPath = Utils.getConfigPath(request);
        String propertyName = PathUtils.getBaseName(configPath);
        configPath = PathUtils.getParentPath(configPath);

        ComplexType type = Utils.getType(configPath, configurationTemplateManager);
        if (!(type instanceof CompositeType))
        {
            throw new IllegalArgumentException("Path '" + configPath + "' refers to unexpected type '" + type + "'");
        }

        configurationSecurityManager.ensurePermission(configPath, AccessManager.ACTION_VIEW);

        CompositeType compositeType = (CompositeType) type;
        TypeProperty property = compositeType.getProperty(propertyName);
        if (property == null)
        {
            throw new NotFoundException("Type '" + compositeType + "' does not have a property named '" + propertyName + "'");
        }

        Configuration instance = configurationTemplateManager.getInstance(configPath);
        String parentPath = PathUtils.getParentPath(configPath);

        OptionProvider optionProvider = OptionProviderFactory.build(compositeType, property.getType(), getOptionAnnotation(property), objectFactory);
        @SuppressWarnings("unchecked")
        List<String> list = (List<String>) optionProvider.getOptions(instance, parentPath, property);
        if (configurationTemplateManager.isTemplatedPath(parentPath))
        {
            Object emptyOption = optionProvider.getEmptyOption(instance, parentPath, property);
            if (emptyOption != null)
            {
                list.add(0, (String) emptyOption);
            }
        }

        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    private Annotation getOptionAnnotation(TypeProperty property)
    {
        Combobox annotation = property.getAnnotation(Combobox.class);
        if (annotation == null)
        {
            throw new IllegalArgumentException("Invalid property: not a ComboBox annotation");
        }

        return annotation;
    }

    @RequestMapping(value = "check/**", method = RequestMethod.POST)
    public ResponseEntity<CheckResultModel> check(HttpServletRequest request,
                                      @RequestBody CheckModel check) throws TypeException
    {
        String configPath = Utils.getConfigPath(request);

        // FIXME kendo support getting the type from the CheckModel, path type may not always exist (wizard).
        ComplexType type = Utils.getType(configPath, configurationTemplateManager);
        if (!(type instanceof CompositeType))
        {
            throw new IllegalArgumentException("Path '" + configPath + "' refers to unexpected type '" + type + "'");
        }

        CompositeType compositeType = (CompositeType) type;
        CompositeType checkType = configurationRegistry.getConfigurationCheckType(compositeType);
        if (checkType == null)
        {
            throw new IllegalArgumentException("Path '" + configPath + "' has type '" + type + "' which does not support configuration checking");
        }

        configurationSecurityManager.ensurePermission(configPath, AccessManager.ACTION_WRITE);

        Record existingRecord = configurationTemplateManager.getRecord(configPath);
        MutableRecord record = Utils.convertProperties(compositeType, null, check.getMain().getProperties());
        if (existingRecord != null)
        {
            ToveUtils.unsuppressPasswords(existingRecord, record, compositeType, false);
        }

        MutableRecord checkRecord = Utils.convertProperties(checkType, null, check.getCheck().getProperties());
        String parentPath = PathUtils.getParentPath(configPath);
        String baseName = PathUtils.getBaseName(configPath);
        Configuration checkInstance = configurationTemplateManager.validate(parentPath, baseName, checkRecord, true, false);
        Configuration mainInstance = configurationTemplateManager.validate(parentPath, baseName, record, true, false);
        if (!checkInstance.isValid())
        {
            throw new ValidationException(checkInstance, "check");
        }

        if (!mainInstance.isValid())
        {
            throw new ValidationException(mainInstance, "main");
        }

        SimpleInstantiator instantiator = new SimpleInstantiator(configurationTemplateManager.getTemplateOwnerPath(configPath), configurationReferenceManager, configurationTemplateManager);
        Configuration instance = (Configuration) instantiator.instantiate(type, record);
        instance.setConfigurationPath(configPath);

        @SuppressWarnings("unchecked")
        ConfigurationCheckHandler<Configuration> handler = (ConfigurationCheckHandler<Configuration>) instantiator.instantiate(checkType, checkRecord);
        CheckResultModel result;
        try
        {
            handler.test(instance);
            result = new CheckResultModel();
        }
        catch (Exception e)
        {
            LOG.debug(e);
            result = new CheckResultModel(e);
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "pullUp/**", method = RequestMethod.GET)
    public ResponseEntity<ActionModel> getPullUp(HttpServletRequest request) throws Exception
    {
        String configPath = Utils.getConfigPath(request);
        Utils.getComposite(configPath, configurationTemplateManager);

        configurationSecurityManager.ensurePermission(configPath, AccessManager.ACTION_WRITE);
        if (!configurationRefactoringManager.canPullUp(configPath))
        {
            throw new IllegalArgumentException("Cannot pull up path '" + configPath + "'");
        }

        ActionModel model = new ActionModel(ConfigurationRefactoringManager.ACTION_PULL_UP, "pull up", null, true);
        FormModel form = new FormModel();
        form.addField(new DropdownFieldModel("ancestorKey", "to ancestor", configurationRefactoringManager.getPullUpAncestors(configPath)));
        model.setForm(form);

        return new ResponseEntity<>(model, HttpStatus.OK);
    }

    @RequestMapping(value = "pullUp/**", method = RequestMethod.POST)
    public ResponseEntity<ActionResultModel> postPullUp(HttpServletRequest request, @RequestBody CompositeModel body) throws Exception
    {
        String configPath = Utils.getConfigPath(request);
        Utils.getComposite(configPath, configurationTemplateManager);

        configurationSecurityManager.ensurePermission(configPath, AccessManager.ACTION_WRITE);
        if (!configurationRefactoringManager.canPullUp(configPath))
        {
            throw new IllegalArgumentException("Cannot pull up path '" + configPath + "'");
        }

        Object ancestorKey = body.getProperties().get("ancestorKey");
        if (ancestorKey == null || !(ancestorKey instanceof String) || ((String) ancestorKey).length() == 0)
        {
            ValidationException exception = new ValidationException();
            exception.addFieldError("ancestorKey", "ancestor is required");
            throw exception;
        }

        String ancestor = (String) ancestorKey;
        if (!configurationRefactoringManager.canPullUp(configPath, ancestor))
        {
            ValidationException exception = new ValidationException();
            exception.addFieldError("ancestorKey", "cannot pull up to ancestor '" + ancestor + "'");
            throw exception;
        }

        configurationRefactoringManager.pullUp(configPath, ancestor);

        CompositeModel model = (CompositeModel) configModelBuilder.buildModel(null, configPath, -1);
        ActionResultModel result = new ActionResultModel(new ActionResult(ActionResult.Status.SUCCESS, "pulled up"), model);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "single/**", method = RequestMethod.GET)
    public ResponseEntity<ActionModel> getSingle(HttpServletRequest request) throws Exception
    {
        ActionContext context = createContext(request);

        // Common actions including delete are not supported here.  So we don't need any of the
        // extra logic such as s/delete/hide which can complicate creating action models in
        // general.
        Messages messages = Messages.getInstance(context.type.getClazz());
        String label = messages.format(context.actionName + ConventionSupport.I18N_KEY_SUFFIX_LABEL);
        ActionModel model = new ActionModel(context.actionName, label, null, actionManager.hasArgument(context.actionName, context.type));

        if (context.action.hasArgument())
        {
            CompositeType argumentType = typeRegistry.getType(context.action.getArgumentClass());
            model.setForm(formModelBuilder.createForm(null, null, argumentType, true));

            Configuration defaults = actionManager.prepare(context.actionName, context.instance);
            if (defaults != null)
            {
                MutableRecord record = argumentType.unstantiate(defaults, null);
                model.setFormDefaults(configModelBuilder.getProperties(null, argumentType, record));
            }
        }

        return new ResponseEntity<>(model, HttpStatus.OK);
    }

    @RequestMapping(value = "single/**", method = RequestMethod.POST)
    public ResponseEntity<ActionResultModel> postSingle(HttpServletRequest request, @RequestBody(required = false) ConfigModel body) throws Exception
    {
        ActionContext context = createContext(request);

        Configuration argument = null;
        if (context.action.hasArgument() && body != null)
        {
            if (body instanceof CompositeModel)
            {
                CompositeModel compositeBody = (CompositeModel) body;
                CompositeType argumentType = typeRegistry.getType(context.action.getArgumentClass());
                CompositeType bodyType;

                if (compositeBody.getType() != null && compositeBody.getType().getSymbolicName() != null)
                {
                    bodyType = typeRegistry.getType(compositeBody.getType().getSymbolicName());
                }
                else
                {
                    bodyType = argumentType;
                }

                if (argumentType.isAssignableFrom(bodyType))
                {
                    MutableRecord record = Utils.convertProperties(bodyType, null, compositeBody.getProperties());

                    argument = configurationTemplateManager.validate(null, null, record, true, false);
                    if (!argument.isValid())
                    {
                        throw new ValidationException(argument);
                    }
                }
                else
                {
                    throw new IllegalArgumentException("Action argument has unexpected type '" + bodyType + "' (expected '" + argumentType + "')");
                }
            }
            else
            {
                throw new IllegalArgumentException("Action argument must be a composite (got '" + body.getClass().getSimpleName() + "')");
            }
        }

        ActionResult result = actionManager.execute(context.actionName, context.instance, argument);
        return new ResponseEntity<>(new ActionResultModel(result, (CompositeModel) configModelBuilder.buildModel(null, context.path, -1)), HttpStatus.OK);
    }

    private ActionContext createContext(HttpServletRequest request)
    {
        ActionContext context = new ActionContext();
        String configPath = Utils.getConfigPath(request);
        if (configPath.length() == 0)
        {
            throw new IllegalArgumentException("Action name is required");
        }

        String[] elements = PathUtils.getPathElements(configPath);
        context.actionName = PathUtils.getPath(0, 1, elements);
        context.path = PathUtils.getPath(1, elements);

        configurationSecurityManager.ensurePermission(context.path, AccessManager.ACTION_VIEW);

        context.instance = configurationTemplateManager.getInstance(context.path);
        if (context.instance == null)
        {
            throw new NotFoundException("Path '" + context.path + "' does not refer to a concrete instance");
        }

        context.type = typeRegistry.getType(context.instance.getClass());
        ConfigurationActions configurationActions = actionManager.getConfigurationActions(context.type);
        context.action = configurationActions.getAction(context.actionName);
        if (context.action == null)
        {
            throw new IllegalArgumentException("Action '" + context.actionName + "' not valid for instance at path '" + context.path + "'");
        }

        return context;
    }

    private static class ActionContext
    {
        String actionName;
        String path;
        Configuration instance;
        CompositeType type;
        ConfigurationAction action;
    }
}
