package com.zutubi.pulse.form.ui;

import java.util.Map;

/**
 * The Renderer is something that knows how to render a Renderable object to a string.
 *
 * The purpose of this interface is to abstract the rendering resources. Common details for renderers are that
 * they render a template (defined by the renderable) and they use a Map to provide a context to the template being
 * rendered. 
 */
public interface Renderer
{
    void render(Renderable r);

    void setAdditionalContext(Map<String, Object> context);

    String getRenderedContent();
}
