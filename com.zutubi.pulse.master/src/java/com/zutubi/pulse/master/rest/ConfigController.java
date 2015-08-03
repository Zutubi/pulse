package com.zutubi.pulse.master.rest;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.rest.errors.NotFoundException;
import com.zutubi.pulse.master.rest.model.ActionModel;
import com.zutubi.pulse.master.rest.model.CollectionModel;
import com.zutubi.pulse.master.rest.model.CompositeModel;
import com.zutubi.pulse.master.rest.model.ConfigModel;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.pulse.servercore.bootstrap.SystemPaths;
import com.zutubi.tove.actions.ActionManager;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.CollectionType;
import com.zutubi.tove.type.ComplexType;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.MapType;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Spring web controller for the /config subset of the RESTish API.
 */
@RestController
@RequestMapping("/config")
public class ConfigController
{
    @Autowired
    private ActionManager actionManager;
    @Autowired
    private ConfigurationTemplateManager configurationTemplateManager;
    @Autowired
    private FormModelBuilder formModelBuilder;
    @Autowired
    private SystemPaths systemPaths;

    @RequestMapping(value = "/**", method = RequestMethod.GET)
    public ResponseEntity<ConfigModel> get(HttpServletRequest request)
    {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        AntPathMatcher apm = new AntPathMatcher();
        String configPath = apm.extractPathWithinPattern(bestMatchPattern, path);

        // We can model anything with a type, even if it is not an existing path yet.
        ComplexType type = configurationTemplateManager.getType(configPath);
        Configuration instance = configurationTemplateManager.getInstance(configPath);
        ConfigModel model;
        if (type instanceof CollectionType)
        {
            model = new CollectionModel((CollectionType) type, instance);
        }
        else
        {
            model = createCompositeModel(configPath, (CompositeType) type, instance);
        }

        if (instance == null)
        {
            throw new NotFoundException("Configuration path '" + configPath + "' not found");
        }

        return new ResponseEntity<>(model, HttpStatus.OK);
    }

    private CompositeModel createCompositeModel(String path, CompositeType type, Configuration instance)
    {
        CompositeModel model = new CompositeModel(type, instance);
        model.setForm(formModelBuilder.createForm(PathUtils.getParentPath(path), PathUtils.getBaseName(path), type, instance.isConcrete(), "form"));
        addActions(model, path, type, instance);
        return model;
    }

    private void addActions(CompositeModel model, String path, CompositeType type, Configuration instance)
    {
        final Messages messages = Messages.getInstance(type.getClazz());
        List<String> actionNames = actionManager.getActions(instance, true, true);

        String key = null;
        Record parentRecord = null;
        String parentPath = PathUtils.getParentPath(path);
        ComplexType parentType = configurationTemplateManager.getType(parentPath);
        if (parentType != null && parentType instanceof MapType)
        {
            parentRecord = configurationTemplateManager.getRecord(parentPath);
            key = PathUtils.getBaseName(path);
        }

        for (String actionName: actionNames)
        {
            List<String> variants = null;
            if (instance != null)
            {
                variants = actionManager.getVariants(actionName, instance);
            }

            if (variants == null)
            {
                model.addAction(new ActionModel(ToveUtils.getActionLink(actionName, parentRecord, key, messages, systemPaths)));
            }
            else
            {
                for (String variant: variants)
                {
                    model.addAction(new ActionModel(actionName, variant, ToveUtils.getActionIconName(actionName, systemPaths.getContentRoot()), variant));
                }
            }
        }
    }

    public void setActionManager(ActionManager actionManager)
    {
        this.actionManager = actionManager;
    }
}
