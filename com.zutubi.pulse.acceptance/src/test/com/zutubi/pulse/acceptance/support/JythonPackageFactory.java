package com.zutubi.pulse.acceptance.support;

import com.sun.script.jython.JythonScriptEngine;
import com.zutubi.util.io.IOUtils;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.*;

/**
 *
 *
 */
public class JythonPackageFactory implements PackageFactory
{
    private Invocable invocableEngine;

    public JythonPackageFactory() throws ScriptException, FileNotFoundException
    {
        ScriptEngine jythonEngine = new JythonScriptEngine();

        invocableEngine = (Invocable) jythonEngine;

        // initialise the engine by reading the necessary python scripts.
        InputStream script = null;
        try
        {
            script = getClass().getResourceAsStream("jythonPackageFactory.jy");
            if (script == null)
            {
                throw new IllegalStateException("Unable to locate the jythonPackageFactory.jy resource.");
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
    
    public void close()
    {

    }
}
