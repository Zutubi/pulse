package com.zutubi.pulse.master.velocity;

import com.zutubi.pulse.Version;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;

/**
 * Generates a ver=x.x.xx string.  This version string can be appended to src
 * references to ensure that caches do not serve files that do not match the
 * version of Pulse.
 */
public class VersionDirective extends AbstractDirective
{
    private static final String version = Version.getVersion().getVersionNumber();

    public String getName()
    {
        return "version";
    }

    public int getType()
    {
        return LINE;
    }

    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException
    {
        writer.write("ver=" + version);
        return true;
    }
}
