package com.zutubi.pulse.form.ui;

import java.util.Map;

/**
 * <class-comment/>
 */
public interface Renderable
{
    void render(Renderer r);

    Map<String, Object> getContext();

    String getTemplateName();

}
