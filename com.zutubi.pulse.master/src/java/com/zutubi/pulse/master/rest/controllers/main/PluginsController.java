package com.zutubi.pulse.master.rest.controllers.main;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zutubi.pulse.core.plugins.Plugin;
import com.zutubi.pulse.core.plugins.PluginManager;
import com.zutubi.pulse.master.rest.errors.NotFoundException;
import com.zutubi.pulse.master.rest.model.plugins.PluginModel;
import com.zutubi.tove.security.AccessManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * REStish API controller for managing plugins.
 */
@RestController
@RequestMapping("/plugins")
public class PluginsController
{
    @Autowired
    private PluginManager pluginManager;
    @Autowired
    private AccessManager accessManager;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ResponseEntity<PluginModel[]> getAll()
    {
        accessManager.ensurePermission(AccessManager.ACTION_ADMINISTER, null);

        List<Plugin> plugins = pluginManager.getPlugins();

        final Collator collator = Collator.getInstance();
        Collections.sort(plugins, new Comparator<Plugin>()
        {
            public int compare(Plugin o1, Plugin o2)
            {
                return collator.compare(o1.getName(), o2.getName());
            }
        });

        List<PluginModel> models = Lists.transform(plugins, new Function<Plugin, PluginModel>()
        {
            @Override
            public PluginModel apply(Plugin input)
            {
                return new PluginModel(input);
            }
        });

        return new ResponseEntity<>(Iterables.toArray(models, PluginModel.class), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<PluginModel> getSingle(@PathVariable String id)
    {
        accessManager.ensurePermission(AccessManager.ACTION_ADMINISTER, null);

        Plugin plugin = pluginManager.getPlugin(id);
        if (plugin == null)
        {
            throw new NotFoundException("Plugin id '" + id + "' not recognised.");
        }

        return new ResponseEntity<>(new PluginModel(plugin), HttpStatus.OK);
    }
}
