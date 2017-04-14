/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.rest.controllers.main;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zutubi.pulse.master.rest.errors.NotFoundException;
import com.zutubi.tove.config.ConfigurationSecurityManager;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.TemplateNode;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.ui.model.TemplateNodeModel;
import com.zutubi.util.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

        TemplateNodeModel model = new TemplateNodeModel(name, configurationTemplateManager.getRecord(path).getHandle(), templateNode.isConcrete());
        addChildren(model, templateNode);
        return new TemplateNodeModel[]{ model };
    }

    private void addChildren(TemplateNodeModel model, TemplateNode node)
    {
        List<TemplateNode> visibleChildren = Lists.newArrayList(Iterables.filter(node.getChildren(), new Predicate<TemplateNode>()
        {
            public boolean apply(TemplateNode templateNode)
            {
                return configurationSecurityManager.hasPermission(templateNode.getPath(), AccessManager.ACTION_VIEW);
            }
        }));

        final Sort.StringComparator comparator = new Sort.StringComparator();
        Collections.sort(visibleChildren, new Comparator<TemplateNode>()
        {
            @Override
            public int compare(TemplateNode o1, TemplateNode o2)
            {
                return comparator.compare(o1.getId(), o2.getId());
            }
        });

        for (TemplateNode childNode: visibleChildren)
        {
            TemplateNodeModel childModel = new TemplateNodeModel(childNode.getId(), configurationTemplateManager.getRecord(childNode.getPath()).getHandle(), childNode.isConcrete());
            model.addChild(childModel);
            addChildren(childModel, childNode);
        }
    }
}
