package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.pulse.master.notifications.renderer.BuildResultRenderer;
import com.zutubi.pulse.master.notifications.renderer.TemplateInfo;
import com.zutubi.pulse.master.tove.handler.FormContext;
import com.zutubi.pulse.master.tove.handler.MapOptionProvider;
import com.zutubi.tove.type.TypeProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides the list of available templates for build subscriptions.
 */
public class SubscriptionTemplateOptionProvider extends MapOptionProvider
{
    private BuildResultRenderer buildResultRenderer;

    public Option getEmptyOption(TypeProperty property, FormContext context)
    {
        return new Option("", "");
    }

    protected Map<String, String> getMap(TypeProperty property, FormContext context)
    {
        Map<String, String> options = new HashMap<>();
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
