package com.zutubi.pulse.core.commands.maven3;

import com.google.common.base.Predicate;
import com.zutubi.pulse.core.resources.api.*;
import com.zutubi.util.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Locates the maven3 home directory on this machine, if any.
 */
public class Maven3HomeDirectoryLocator implements FileLocator
{
    private static final String BINARY_NAME = "mvn";
    private static final Pattern VERSION_PATTERN = Pattern.compile("^.*Maven\\s(3[.0-9]+)\\s.*$", Pattern.MULTILINE);
    
    private String capturedVersion;

    public Collection<File> locate()
    {
        // Our search strategy needs to disambiguate maven 2 and 3.  To do so,
        // we find candidate scripts and run them with -version.  If they
        // produce a line matching .*Maven 3.*, we say we've found maven 3.
        //
        // Search order:
        //   - M2_HOME (the standard env var), verified by running $M2_HOME/bin/mvn{.bat}
        //   - M2, verified by running $M2
        //   - MAVEN3_HOME, our non-standard version of M2_HOME
        //   - mvn on the path
        Predicate<File> scriptFilePredicate = new Predicate<File>()
        {
            public boolean apply(File file)
            {
                return isScriptRightVersion(file);
            }
        };
        
        FirstNonEmptyFileLocator locator = new FirstNonEmptyFileLocator(
                new StandardHomeDirectoryFileLocator("M2_HOME", BINARY_NAME, true, scriptFilePredicate),
                new ParentsFileLocator(new FilteringFileLocator(new EnvironmentVariableFileLocator("M2"), scriptFilePredicate)),
                new StandardHomeDirectoryFileLocator("MAVEN3_HOME", BINARY_NAME, true, scriptFilePredicate),
                new ParentsFileLocator(new FilteringFileLocator(new BinaryInPathFileLocator(BINARY_NAME), scriptFilePredicate))
        );
    
        return locator.locate();
    }

    public String getCapturedVersion()
    {
        return capturedVersion;
    }

    private boolean isScriptRightVersion(File script)
    {
        if (script.isFile())
        {
            try
            {
                String output = SystemUtils.runCommand(script.getAbsolutePath(), "-version");
                Matcher matcher = VERSION_PATTERN.matcher(output);
                if (matcher.find())
                {
                    capturedVersion = matcher.group(1);
                    return true;
                }
            }
            catch (IOException e)
            {
                // Fall through.
            }
        }
        
        return false;
    }
}
