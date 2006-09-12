package com.zutubi.pulse.form.ui;

import java.util.Map;

/**
 * <class-comment/>
 */
public interface Renderable
{
    /**
     * The render method provides support for a visitor pattern style approach to rendering this object.
     *
     * @param r
     */
    void render(Renderer r);

    Map<String, Object> getContext();

    /**
     * The name of the template that will be used to render this object.
     * 
     * @return
     */
    String getTemplateName();
}
