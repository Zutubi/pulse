package com.zutubi.pulse.master.notifications.renderer;

import com.zutubi.pulse.master.model.BuildResult;

import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 * A BuildResultRenderer converts a build model into a displayable form, based
 * on a specified template.
 *
 * @author jsankey
 */
public interface BuildResultRenderer
{
    void render(BuildResult result, Map<String, Object> dataMap, String templateName, Writer writer);
    boolean hasTemplate(String template, boolean personal);
    List<TemplateInfo> getAvailableTemplates(boolean personal);
    TemplateInfo getTemplateInfo(String templateName, boolean personal);
}