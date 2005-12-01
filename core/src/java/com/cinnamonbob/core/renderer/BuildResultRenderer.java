package com.cinnamonbob.core.renderer;

import com.cinnamonbob.core.model.RecipeResult;

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

    public void render(String hostUrl, String project, long projectId, RecipeResult result, String type, Writer writer);
}