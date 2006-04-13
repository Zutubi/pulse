/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.renderer;

import com.zutubi.pulse.model.BuildResult;

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