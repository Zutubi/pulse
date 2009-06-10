package com.zutubi.pulse.acceptance.support.jython;

import com.sun.script.jython.JythonScriptEngine;
import com.zutubi.pulse.acceptance.support.PackageFactory;
import com.zutubi.pulse.acceptance.support.PulsePackage;
import com.zutubi.util.io.IOUtils;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.*;

/**
 * A jython implementation of the PackageFactory interface that returns handles that
 * are implemented using the jython scripting system.
 */
public class JythonPackageFactory implements PackageFactory
{
    private Invocable invocableEngine;

    private boolean verbose;

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
            return (PulsePackage) invocableEngine.invokeFunction("createPackage", pkg.getCanonicalPath(), verbose);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setVerbose(boolean verbose)
    {
        this.verbose = verbose;
    }

    public void close()
    {

    }
}
