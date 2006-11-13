package com.zutubi.pulse.velocity;

import junit.framework.TestCase;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.VelocityContext;

import java.io.StringWriter;

/**
 * <class-comment/>
 */
public abstract class VelocityDirectiveTestCase extends TestCase
{
    private VelocityEngine velocity;

    protected VelocityDirectiveTestCase()
    {
    }

    protected VelocityDirectiveTestCase(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        // initialise velocity.
        velocity = new VelocityEngine();

        // register the user directives.
        StringBuffer directives = new StringBuffer();
        String sep = "";
        for (String directive : getUserDirectives())
        {
            directives.append(sep);
            directives.append(directive);
            sep = ",";
        }
        velocity.addProperty("userdirective", directives.toString());

        // initiaise the velocity system.
        velocity.init();
    }

    protected String evaluate(String template) throws Exception
    {
        StringWriter writer = new StringWriter();
        velocity.evaluate(new VelocityContext(), writer, "", template);
        return writer.toString();
    }


    public abstract String[] getUserDirectives();

}
