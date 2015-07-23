package com.zutubi.pulse.master.rest;

import com.zutubi.pulse.master.rest.errors.NotFoundException;
import com.zutubi.pulse.master.rest.model.ConfigModel;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.api.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * Spring web controller for the /config subset of the RESTish API.
 */
@RestController
@RequestMapping("/config")
public class ConfigController
{
    @Autowired
    private ConfigurationTemplateManager configurationTemplateManager;

    @RequestMapping(value = "/**", method = RequestMethod.GET)
    public ResponseEntity<ConfigModel> get(HttpServletRequest request)
    {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        AntPathMatcher apm = new AntPathMatcher();
        String configPath = apm.extractPathWithinPattern(bestMatchPattern, path);

        Configuration instance = configurationTemplateManager.getInstance(configPath);
        if (instance == null)
        {
            throw new NotFoundException("Configuration path '" + configPath + "' not found");
        }

        return new ResponseEntity<>(new ConfigModel(instance, configurationTemplateManager.getType(configPath)), HttpStatus.OK);
    }
}
