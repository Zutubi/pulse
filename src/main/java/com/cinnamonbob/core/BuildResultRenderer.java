package com.cinnamonbob.core;

import java.io.Writer;

/**
 * A BuildResultRenderer converts a build result into a displayable form, based
 * on a specified content type.
 * 
 * @author jsankey
 */
public interface BuildResultRenderer
{
    public static final String TYPE_PLAIN = "plain";
    public static final String TYPE_HTML  = "html";

    public void render(BuildResult result, String type, Writer writer);
}
