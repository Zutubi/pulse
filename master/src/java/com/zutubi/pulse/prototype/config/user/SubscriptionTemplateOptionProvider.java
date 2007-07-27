package com.zutubi.pulse.prototype.config.user;

import com.zutubi.prototype.MapOptionProvider;
import com.zutubi.prototype.MapOption;
import com.zutubi.prototype.type.TypeProperty;
import com.zutubi.pulse.renderer.BuildResultRenderer;
import com.zutubi.pulse.renderer.TemplateInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides the list of available templates for build subscriptions.
 */
public class SubscriptionTemplateOptionProvider extends MapOptionProvider
{
    private BuildResultRenderer buildResultRenderer;

    public MapOption getEmptyOption(Object instance, String parentPath, TypeProperty property)
    {
        return new MapOption("", "");
    }

    protected Map<String, String> getMap(Object instance, String parentPath, TypeProperty property)
    {
        Map<String, String> options = new HashMap<String, String>();
        for (TemplateInfo template : buildResultRenderer.getAvailableTemplates(false))
        {
            options.put(template.getTemplate(), template.getDisplay());
        }
        return options;
    }

    public void setBuildResultRenderer(BuildResultRenderer buildResultRenderer)
    {
        this.buildResultRenderer = buildResultRenderer;
    }

}
