package com.cinnamonbob.renderer;

import com.cinnamonbob.model.BuildResult;

import java.io.Writer;

/**
 * A BuildResultRenderer converts a build model into a displayable form, based
 * on a specified content type.
 *
 * @author jsankey
 */
public interface BuildResultRenderer
{
    public static final String TYPE_PLAIN = "plain";
    public static final String TYPE_HTML = "html";

    public void render(String hostUrl, BuildResult result, String type, Writer writer);
}