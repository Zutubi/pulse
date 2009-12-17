package com.zutubi.pulse.master.notifications.renderer;

import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;

import java.util.Map;

/**
 * Abstracts services that can render a build result for notifications.  This
 * service sits on top of a {@link BuildResultRenderer}.
 */
public interface RenderService
{
    /**
     * Returns the data map that would be used to render the given buildResult.
     *
     * @param buildResult  the result to be rendered
     * @param baseUrl      the configured base url for the pulse instance
     * @param buildManager required resource
     * @param renderer     low-level renderer
     * @return the data map that would be used for rendering a template for
     *         this build result
     */
    Map<String, Object> getDataMap(BuildResult buildResult, String baseUrl, BuildManager buildManager, BuildResultRenderer renderer);

    /**
     * Renders the given build result using the given details.
     *
     * @param buildResult         the result to be rendered
     * @param baseUrl             configured base url for the pulse instance
     * @param buildManager        required resource
     * @param buildResultRenderer low-level renderer used to do the grunt work
     * @param template            name of the template to use
     * @return a rendered result
     */
    RenderedResult renderResult(BuildResult buildResult, String baseUrl, BuildManager buildManager, BuildResultRenderer buildResultRenderer, String template);
}
