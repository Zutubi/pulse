package com.zutubi.pulse.master.rest.controllers.main;

import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.core.resources.api.ResourceVersionConfiguration;
import com.zutubi.pulse.master.model.ResourceManager;
import com.zutubi.pulse.master.rest.model.ResourceModel;
import com.zutubi.pulse.master.rest.model.ResourceVersionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * RESTish APi controller for resources.
 */
@RestController
@RequestMapping("/resources")
public class ResourcesController
{
    @Autowired
    private ResourceManager resourceManager;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ResponseEntity<Collection<ResourceModel>> getAll()
    {
        Map<String, ResourceModel> resourcesByName = new TreeMap<>();
        Map<String, List<ResourceConfiguration>> resourcesByAgent = resourceManager.findAllVisible();
        for (Map.Entry<String, List<ResourceConfiguration>> entry: resourcesByAgent.entrySet())
        {
            for (ResourceConfiguration resourceConfig: entry.getValue())
            {
                String name = resourceConfig.getName();
                ResourceModel resourceModel = resourcesByName.get(name);
                if (resourceModel == null)
                {
                    resourceModel = new ResourceModel(name);
                    resourcesByName.put(name, resourceModel);
                }

                ResourceVersionModel versionModel = resourceModel.getOrAddVersion("");
                versionModel.addAgent(entry.getKey());

                for (ResourceVersionConfiguration versionConfig: resourceConfig.getVersions().values())
                {
                    versionModel = resourceModel.getOrAddVersion(versionConfig.getValue());
                    versionModel.addAgent(entry.getKey());
                }
            }
        }

        return new ResponseEntity<>(resourcesByName.values(), HttpStatus.OK);
    }

}
