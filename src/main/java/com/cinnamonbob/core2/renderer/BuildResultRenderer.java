package com.cinnamonbob.core2.renderer;

import com.cinnamonbob.core2.BuildResult;
import com.cinnamonbob.model.Project;

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

    public void render(Project project, BuildResult result, String type, Writer writer);
}