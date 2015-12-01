package com.zutubi.pulse.master.rest.controllers.main;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.zutubi.pulse.master.rest.errors.NotFoundException;
import com.zutubi.pulse.master.rest.model.TemplateNodeModel;
import com.zutubi.tove.config.ConfigurationSecurityManager;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.TemplateNode;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.record.PathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring web controller for the /template subset of the RESTish API.
 */
@RestController
@RequestMapping("/template")
public class TemplateController
{
    @Autowired
    private ConfigurationTemplateManager configurationTemplateManager;
    @Autowired
    private ConfigurationSecurityManager configurationSecurityManager;

    @RequestMapping(value = "/{scope}", method = RequestMethod.GET)
    public TemplateNodeModel[] getScope(@PathVariable String scope)
    {
        return get(scope, null);
    }

    @RequestMapping(value = "/{scope}/{name}", method = RequestMethod.GET)
    public TemplateNodeModel[] get(@PathVariable String scope, @PathVariable String name)
    {
        if (!configurationTemplateManager.getTemplateScopes().contains(scope))
        {
            throw new NotFoundException("'" + scope + "' is not a known templated scope");
        }

        if (name == null)
        {
            name = configurationTemplateManager.getTemplateHierarchy(scope).getRoot().getId();
        }

        String path = PathUtils.getPath(scope, name);
        configurationSecurityManager.ensurePermission(path, AccessManager.ACTION_VIEW);

        TemplateNode templateNode = configurationTemplateManager.getTemplateNode(path);
        if (templateNode == null)
        {
            throw new NotFoundException("Scope '" + scope + "' has no item named '" + name + "'");
        }

        TemplateNodeModel model = new TemplateNodeModel(name, templateNode.isConcrete());
        addChildren(model, templateNode);
        return new TemplateNodeModel[]{ model };
    }

    private void addChildren(TemplateNodeModel model, TemplateNode node)
    {
        Iterable<TemplateNode> visibleChildren = Iterables.filter(node.getChildren(), new Predicate<TemplateNode>()
        {
            public boolean apply(TemplateNode templateNode)
            {
                return configurationSecurityManager.hasPermission(templateNode.getPath(), AccessManager.ACTION_VIEW);
            }
        });

        for (TemplateNode childNode: visibleChildren)
        {
            TemplateNodeModel childModel = new TemplateNodeModel(childNode.getId(), childNode.isConcrete());
            model.addChild(childModel);
            addChildren(childModel, childNode);
        }
    }
}
