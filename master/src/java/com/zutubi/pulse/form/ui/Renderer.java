package com.zutubi.pulse.form.ui;

import java.util.Map;

/**
 * <class-comment/>
 */
public interface Renderer
{
    void render(Renderable r);

    void setAdditionalContext(Map<String, Object> context);

    String getRenderedContent();
}
