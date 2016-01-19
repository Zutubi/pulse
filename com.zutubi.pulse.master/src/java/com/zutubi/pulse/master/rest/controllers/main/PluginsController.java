package com.zutubi.pulse.master.rest.controllers.main;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zutubi.pulse.core.plugins.Plugin;
import com.zutubi.pulse.core.plugins.PluginException;
import com.zutubi.pulse.core.plugins.PluginManager;
import com.zutubi.pulse.master.rest.errors.NotFoundException;
import com.zutubi.pulse.master.rest.model.plugins.PluginModel;
import com.zutubi.tove.security.AccessManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
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
        return new ResponseEntity<>(new PluginModel(getPlugin(id)), HttpStatus.OK);
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ResponseEntity<PluginModel> install(@RequestParam("file") MultipartFile file) throws IOException, PluginException
    {
        try (InputStream is = file.getInputStream())
        {
            Plugin plugin = pluginManager.install(is, file.getOriginalFilename());
            return new ResponseEntity<>(new PluginModel(plugin), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<PluginModel> uninstall(@PathVariable String id) throws PluginException
    {
        accessManager.ensurePermission(AccessManager.ACTION_ADMINISTER, null);

        Plugin plugin = getPlugin(id);
        if (!plugin.canUninstall())
        {
            throw new IllegalArgumentException("Plugin '" + id + "' is not in a state that allows it to be uninstalled");
        }

        plugin.uninstall();

        return new ResponseEntity<>(new PluginModel(plugin), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/{action}", method = RequestMethod.POST)
    public ResponseEntity<PluginModel> action(@PathVariable String id, @PathVariable String action) throws PluginException
    {
        accessManager.ensurePermission(AccessManager.ACTION_ADMINISTER, null);

        Plugin plugin = getPlugin(id);
        switch (action)
        {
            case "enable":
                if (!plugin.canEnable())
                {
                    throw new IllegalArgumentException("Plugin '" + id + "' is not in a state that allows it to be enabled");
                }

                plugin.enable();
                break;
            case "disable":
                if (!plugin.canDisable())
                {
                    throw new IllegalArgumentException("Plugin '" + id + "' is not in a state that allows it to be disabled");
                }

                plugin.disable();
                break;
            default:
                throw new IllegalArgumentException("Invalid plugin action '" + action + "'");
        }

        return new ResponseEntity<>(new PluginModel(plugin), HttpStatus.OK);
    }


    private Plugin getPlugin(@PathVariable String id)
    {
        Plugin plugin = pluginManager.getPlugin(id);
        if (plugin == null)
        {
            throw new NotFoundException("Plugin id '" + id + "' not recognised.");
        }
        return plugin;
    }

}
