package com.zutubi.pulse.master.rest.controllers.main;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.rest.Utils;
import com.zutubi.pulse.master.rest.actions.*;
import com.zutubi.pulse.master.rest.errors.NotFoundException;
import com.zutubi.pulse.master.rest.errors.ValidationException;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.tove.ConventionSupport;
import com.zutubi.tove.annotations.Combobox;
import com.zutubi.tove.config.ConfigurationReferenceManager;
import com.zutubi.tove.config.ConfigurationSecurityManager;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.api.ActionResult;
import com.zutubi.tove.config.api.ActionVariant;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.config.api.ConfigurationCheckHandler;
import com.zutubi.tove.config.cleanup.RecordCleanupTask;
import com.zutubi.tove.config.docs.ConfigurationDocsManager;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.*;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.ui.ConfigModelBuilder;
import com.zutubi.tove.ui.FormModelBuilder;
import com.zutubi.tove.ui.ToveUiUtils;
import com.zutubi.tove.ui.actions.ActionManager;
import com.zutubi.tove.ui.actions.ConfigurationAction;
import com.zutubi.tove.ui.actions.ConfigurationActions;
import com.zutubi.tove.ui.handler.FormContext;
import com.zutubi.tove.ui.handler.OptionProvider;
import com.zutubi.tove.ui.handler.OptionProviderFactory;
import com.zutubi.tove.ui.model.*;
import com.zutubi.util.StringUtils;
import com.zutubi.util.bean.ObjectFactory;
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
import java.util.Map;

/**
 * Controller to handle invocation of configuration actions.
 */
@RestController
@RequestMapping("/action")
public class ConfigActionsController
{
    @Autowired
    private ConfigurationTemplateManager configurationTemplateManager;
    @Autowired
    private MasterConfigurationRegistry configurationRegistry;
    @Autowired
    private ConfigurationSecurityManager configurationSecurityManager;
    @Autowired
    private ConfigurationReferenceManager configurationReferenceManager;
    @Autowired
    private ConfigurationDocsManager configurationDocsManager;
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
        String configPath = Utils.getRequestedPath(request);
        // This is to validate the path, we don't need the type.
        Utils.getType(configPath, configurationTemplateManager);

        configurationSecurityManager.ensurePermission(configPath, AccessManager.ACTION_VIEW);

        RecordCleanupTask task = configurationTemplateManager.getCleanupTasks(configPath);
        return new ResponseEntity<>(configModelBuilder.buildCleanupTask(task), HttpStatus.OK);
    }

    @RequestMapping(value = "options/**", method = RequestMethod.POST)
    public ResponseEntity<List<String>> options(HttpServletRequest request, @RequestBody OptionsModel body) throws Exception
    {
        String parentPath = Utils.getRequestedPath(request);

        CompositeType type = typeRegistry.getType(body.getSymbolicName());
        if (type == null)
        {
            throw new IllegalArgumentException("Unrecognised type '" + body.getSymbolicName() + "'");
        }

        configurationSecurityManager.ensurePermission(parentPath, AccessManager.ACTION_VIEW);

        TypeProperty property = type.getProperty(body.getPropertyName());
        if (property == null)
        {
            throw new NotFoundException("Type '" + type + "' does not have a property named '" + body.getPropertyName() + "'");
        }

        FormContext context;
        if (StringUtils.stringSet(body.getBaseName()))
        {
            context = new FormContext(configurationTemplateManager.getInstance(PathUtils.getPath(parentPath, body.getBaseName())));
        }
        else
        {
            context = new FormContext(parentPath);
        }

        OptionProvider optionProvider = OptionProviderFactory.build(type, property.getType(), getOptionAnnotation(property), objectFactory);
        @SuppressWarnings("unchecked")
        List<String> list = (List<String>) optionProvider.getOptions(property, context);
        if (configurationTemplateManager.isTemplatedPath(parentPath))
        {
            Object emptyOption = optionProvider.getEmptyOption(property, context);
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

    @RequestMapping(value = "validate/**", method = RequestMethod.POST)
    public ResponseEntity<String> validate(HttpServletRequest request, @RequestBody ValidateModel model) throws TypeException
    {
        String parentPath = Utils.getRequestedPath(request);

        String symbolicName = model.getComposite().getType().getSymbolicName();
        CompositeType type = typeRegistry.getType(symbolicName);
        if (type == null)
        {
            throw new IllegalArgumentException("Unrecognised symbolic name '" + symbolicName + "'");
        }

        MutableRecord record = ToveUiUtils.convertProperties(type, null, model.getComposite().getProperties());
        Configuration instance = configurationTemplateManager.validate(parentPath, model.getBaseName(), record, model.isConcrete(), false, model.getIgnoredFields());
        if (!instance.isValid())
        {
            throw new ValidationException(instance);
        }

        return new ResponseEntity<>(parentPath, HttpStatus.OK);
    }

    @RequestMapping(value = "check/**", method = RequestMethod.POST)
    public ResponseEntity<CheckResultModel> check(HttpServletRequest request,
                                                  @RequestBody CheckModel check) throws TypeException
    {
        String configPath = Utils.getRequestedPath(request);

        CompositeType compositeType;
        String symbolicName = check.getMain().getType().getSymbolicName();
        if (StringUtils.stringSet(symbolicName))
        {
            compositeType = typeRegistry.getType(symbolicName);
            if (compositeType == null)
            {
                throw new IllegalArgumentException("Unrecognised symbolic name '" + symbolicName + "'");
            }
        }
        else
        {
            ComplexType type = Utils.getType(configPath, configurationTemplateManager);
            if (!(type instanceof CompositeType))
            {
                throw new IllegalArgumentException("Path '" + configPath + "' refers to unexpected type '" + type + "'");
            }

            compositeType = (CompositeType) type;
        }

        CompositeType checkType = configurationRegistry.getConfigurationCheckType(compositeType);
        if (checkType == null)
        {
            throw new IllegalArgumentException("Path '" + configPath + "' has type '" + compositeType + "' which does not support configuration checking");
        }

        configurationSecurityManager.ensurePermission(configPath, AccessManager.ACTION_WRITE);

        Record existingRecord = configurationTemplateManager.getRecord(configPath);
        MutableRecord record = ToveUiUtils.convertProperties(compositeType, null, check.getMain().getProperties());
        if (existingRecord != null)
        {
            ToveUiUtils.unsuppressPasswords(existingRecord, record, compositeType, false);
        }

        MutableRecord checkRecord = ToveUiUtils.convertProperties(checkType, null, check.getCheck().getProperties());
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
        Configuration instance = (Configuration) instantiator.instantiate(compositeType, record);
        instance.setConfigurationPath(configPath);

        @SuppressWarnings("unchecked")
        ConfigurationCheckHandler<Configuration> handler = (ConfigurationCheckHandler<Configuration>) instantiator.instantiate(checkType, checkRecord);
        CheckResultModel result = Utils.runCheck(handler, instance);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "clone/**", method = RequestMethod.GET)
    public ResponseEntity<ActionModel> getClone(HttpServletRequest request) throws Exception
    {
        return getWithHandler(request, CloneHandler.class);
    }

    @RequestMapping(value = "clone/**", method = RequestMethod.POST)
    public ResponseEntity<ActionResultModel> postClone(HttpServletRequest request, @RequestBody CompositeModel body) throws Exception
    {
        return postWithHandler(request, body, CloneHandler.class);
    }

    @RequestMapping(value = "pullUp/**", method = RequestMethod.GET)
    public ResponseEntity<ActionModel> getPullUp(HttpServletRequest request) throws Exception
    {
        return getWithHandler(request, PullUpHandler.class);
    }

    @RequestMapping(value = "pullUp/**", method = RequestMethod.POST)
    public ResponseEntity<ActionResultModel> postPullUp(HttpServletRequest request, @RequestBody CompositeModel body) throws Exception
    {
        return postWithHandler(request, body, PullUpHandler.class);
    }

    @RequestMapping(value = "pushDown/**", method = RequestMethod.GET)
    public ResponseEntity<ActionModel> getPushDown(HttpServletRequest request) throws Exception
    {
        return getWithHandler(request, PushDownHandler.class);
    }

    @RequestMapping(value = "pushDown/**", method = RequestMethod.POST)
    public ResponseEntity<ActionResultModel> postPushDown(HttpServletRequest request, @RequestBody CompositeModel body) throws Exception
    {
        return postWithHandler(request, body, PushDownHandler.class);
    }

    @RequestMapping(value = "introduceParent/**", method = RequestMethod.GET)
    public ResponseEntity<ActionModel> getIntroduceParent(HttpServletRequest request) throws Exception
    {
        return getWithHandler(request, IntroduceParentHandler.class);
    }

    @RequestMapping(value = "introduceParent/**", method = RequestMethod.POST)
    public ResponseEntity<ActionResultModel> postIntroduceParent(HttpServletRequest request, @RequestBody CompositeModel body) throws Exception
    {
        return postWithHandler(request, body, IntroduceParentHandler.class);
    }

    @RequestMapping(value = "smartClone/**", method = RequestMethod.GET)
    public ResponseEntity<ActionModel> getSmartClone(HttpServletRequest request) throws Exception
    {
        return getWithHandler(request, SmartCloneHandler.class);
    }

    @RequestMapping(value = "smartClone/**", method = RequestMethod.POST)
    public ResponseEntity<ActionResultModel> postSmartClone(HttpServletRequest request, @RequestBody CompositeModel body) throws Exception
    {
        return postWithHandler(request, body, SmartCloneHandler.class);
    }

    @RequestMapping(value = "restore/**", method = RequestMethod.POST)
    public ResponseEntity<CollectionModel> postRestore(HttpServletRequest request) throws TypeException
    {
        String configPath = Utils.getRequestedPath(request);
        String parentPath = PathUtils.getParentPath(configPath);
        configurationTemplateManager.restore(configPath);
        return new ResponseEntity<>((CollectionModel) configModelBuilder.buildModel(null, parentPath, -1), HttpStatus.OK);
    }

    private ResponseEntity<ActionModel> getWithHandler(HttpServletRequest request, Class<? extends ActionHandler> handlerClass)
    {
        String configPath = Utils.getRequestedPath(request);
        // This is a validation step to check a composite record exists.
        Utils.getComposite(configPath, configurationTemplateManager);

        configurationSecurityManager.ensurePermission(configPath, AccessManager.ACTION_WRITE);

        ActionHandler handler = objectFactory.buildBean(handlerClass);
        return new ResponseEntity<>(handler.getModel(configPath, null), HttpStatus.OK);
    }

    private ResponseEntity<ActionResultModel> postWithHandler(HttpServletRequest request, @RequestBody CompositeModel body, Class<? extends ActionHandler> handlerClass) throws TypeException
    {
        String configPath = Utils.getRequestedPath(request);
        Utils.getComposite(configPath, configurationTemplateManager);

        configurationSecurityManager.ensurePermission(configPath, AccessManager.ACTION_WRITE);

        ActionHandler handler = objectFactory.buildBean(handlerClass);
        ActionResult actionResult = handler.doAction(configPath, null, body.getProperties());

        String newPath = null;
        ConfigModel model = null;
        if (actionResult.getCreatedPath() != null)
        {
            newPath = actionResult.getCreatedPath();
            model = configModelBuilder.buildModel(null, newPath, -1);
        }
        else if (Utils.hasModellableType(configPath, configurationTemplateManager))
        {
            model = configModelBuilder.buildModel(null, configPath, -1);
        }

        return new ResponseEntity<>(new ActionResultModel(actionResult, newPath, model), HttpStatus.OK);
    }

    @RequestMapping(value = "single/**", method = RequestMethod.GET)
    public ResponseEntity<ActionModel> getSingle(HttpServletRequest request) throws Exception
    {
        ActionContext context = createContext(request, true);
        ActionModel model;

        if (context.handler == null)
        {
            // Common actions including delete are not supported here.  So we don't need any of the
            // extra logic such as s/delete/hide which can complicate creating action models in
            // general.
            Messages messages = Messages.getInstance(context.type.getClazz());
            String label = messages.format(context.actionName + ConventionSupport.I18N_KEY_SUFFIX_LABEL);
            model = new ActionModel(context.actionName, label, null, actionManager.hasArgument(context.actionName, context.type));

            if (context.action.hasArgument())
            {
                CompositeType argumentType = typeRegistry.getType(context.action.getArgumentClass());
                model.setForm(formModelBuilder.createForm(argumentType));
                model.setDocs(configurationDocsManager.getDocs(argumentType));

                Configuration defaults = actionManager.prepare(context.actionName, context.instance);
                if (defaults != null)
                {
                    MutableRecord record = argumentType.unstantiate(defaults, null);
                    model.setFormDefaults(configModelBuilder.getProperties(null, argumentType, record));
                }
            }
        }
        else
        {
            model = context.handler.getModel(context.path, context.variant);
        }

        return new ResponseEntity<>(model, HttpStatus.OK);
    }

    @RequestMapping(value = "single/**", method = RequestMethod.POST)
    public ResponseEntity<ActionResultModel> postSingle(HttpServletRequest request, @RequestBody(required = false) ConfigModel body) throws Exception
    {
        ActionContext context = createContext(request, true);

        ActionResult result;
        if (context.handler == null)
        {
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
                        MutableRecord record = ToveUiUtils.convertProperties(bodyType, null, compositeBody.getProperties());

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

            result = actionManager.execute(context.actionName, context.instance, argument);
        }
        else
        {
            Map<String, Object> input = body != null && body instanceof CompositeModel ? ((CompositeModel) body).getProperties() : null;
            result = context.handler.doAction(context.path, context.variant, input);
        }

        return new ResponseEntity<>(new ActionResultModel(result, null, configModelBuilder.buildModel(null, context.path, -1)), HttpStatus.OK);
    }

    @RequestMapping(value = "descendant/**", method = RequestMethod.POST)
    public ResponseEntity<ActionResultModel> postDescendant(HttpServletRequest request) throws Exception
    {
        ActionContext context = createContext(request, false);

        Map<String, ActionResult> results = actionManager.executeOnDescendants(context.actionName, context.path);

        int failureCount = 0;
        for (ActionResult result: results.values())
        {
            if (result.getStatus() != ActionResult.Status.SUCCESS)
            {
                failureCount++;
            }
        }

        String message = "Executed on " + results.size() + " descendant" + (results.size() == 1 ? "" : "s");
        if (failureCount > 0)
        {
            message += ", failed on " + failureCount;
        }

        return new ResponseEntity<>(new ActionResultModel(failureCount == 0, message, configModelBuilder.buildModel(null, context.path, -1)), HttpStatus.OK);
    }

    private ActionContext createContext(HttpServletRequest request, boolean single) throws Exception
    {
        final ActionContext context = new ActionContext();
        String configPath = Utils.getRequestedPath(request);
        if (configPath.length() == 0)
        {
            throw new IllegalArgumentException("Action name is required");
        }

        String[] elements = PathUtils.getPathElements(configPath);
        context.actionName = PathUtils.getPath(0, 1, elements);
        context.path = PathUtils.getPath(1, elements);

        int splitIndex = context.actionName.indexOf(":");
        if (splitIndex > 0 && splitIndex < context.actionName.length() - 1)
        {
            context.variant = context.actionName.substring(splitIndex + 1);
            context.actionName = context.actionName.substring(0, splitIndex);
        }

        configurationSecurityManager.ensurePermission(context.path, AccessManager.ACTION_VIEW);

        if (single)
        {
            context.instance = configurationTemplateManager.getInstance(context.path);
            if (context.instance == null)
            {
                throw new NotFoundException("Path '" + context.path + "' does not refer to a concrete instance");
            }

            context.type = typeRegistry.getType(context.instance.getClass());
            ConfigurationActions configurationActions = actionManager.getConfigurationActions(context.type);
            if (StringUtils.stringSet(context.variant))
            {
                List<ActionVariant> variants = configurationActions.getVariants(context.actionName, context.instance);
                final Optional<ActionVariant> variant = Iterables.tryFind(variants, new Predicate<ActionVariant>()
                {
                    @Override
                    public boolean apply(ActionVariant input)
                    {
                        return input.getName().equals(context.variant);
                    }
                });

                if (!variant.isPresent())
                {
                    throw new IllegalArgumentException("Invalid variant '" + context.variant + "' for action '" + context.actionName + "' on path '" + context.path + "'");
                }
            }
            else
            {
                context.action = configurationActions.getAction(context.actionName);
                if (context.action == null)
                {
                    throw new IllegalArgumentException("Action '" + context.actionName + "' not valid for instance at path '" + context.path + "'");
                }
            }

            Class<? extends ActionHandler> handlerClass = ConventionSupport.loadClass(context.type, StringUtils.capitalise(context.actionName) + "Handler", ActionHandler.class);
            if (handlerClass != null)
            {
                context.handler = objectFactory.buildBean(handlerClass);
            }
        }

        return context;
    }

    private static class ActionContext
    {
        String actionName;
        String variant;
        String path;
        Configuration instance;
        CompositeType type;
        // Only present for single, non-custom-handler actions
        ConfigurationAction action;
        // Only present for single custom-handler actions.
        ActionHandler handler;
    }
}
