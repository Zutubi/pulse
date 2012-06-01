package com.zutubi.pulse.acceptance.support.jython;

import com.zutubi.pulse.acceptance.support.Pulse;
import com.zutubi.pulse.acceptance.support.PulsePackage;
import com.zutubi.pulse.acceptance.support.PulseTestFactory;
import com.zutubi.util.io.IOUtils;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;

/**
 * Implementation of PulseTestFactory backed by embedded Jython.
 */
public class JythonPulseTestFactory implements PulseTestFactory
{
    private Invocable invocableEngine;

    public JythonPulseTestFactory() throws ScriptException, FileNotFoundException
    {
        ScriptEngine jythonEngine = new ScriptEngineManager().getEngineByName("python");

        invocableEngine = (Invocable) jythonEngine;

        // initialise the engine by reading the necessary python scripts.
        InputStream script = null;
        try
        {
            script = getClass().getResourceAsStream("jythonPulseTestFactory.jy");
            if (script == null)
            {
                throw new IllegalStateException("Unable to locate the jythonPulseTestFactory.jy resource.");
            }

            Reader scriptReader = new InputStreamReader(script);
            jythonEngine.eval(scriptReader);
        }
        finally
        {
            IOUtils.close(script);
        }
    }

    public PulsePackage createPackage(File pkg)
    {
        try
        {
            return (PulsePackage) invocableEngine.invokeFunction("createPackage", pkg.getCanonicalPath());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public Pulse createPulse(String pulseHome)
    {
        try
        {
            return (Pulse) invocableEngine.invokeFunction("createPulse", pulseHome);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
