package com.zutubi.pulse.master.rest;

import com.zutubi.pulse.master.rest.errors.NotFoundException;
import com.zutubi.pulse.master.rest.errors.ValidationException;
import com.zutubi.pulse.master.rest.model.CheckModel;
import com.zutubi.pulse.master.rest.model.CheckResultModel;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.handler.OptionProvider;
import com.zutubi.pulse.master.tove.handler.OptionProviderFactory;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.tove.annotations.Combobox;
import com.zutubi.tove.config.ConfigurationReferenceManager;
import com.zutubi.tove.config.ConfigurationSecurityManager;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.config.api.ConfigurationCheckHandler;
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
    private ObjectFactory objectFactory;

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
}
