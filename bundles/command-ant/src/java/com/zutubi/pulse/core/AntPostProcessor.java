package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.util.SystemUtils;

import java.util.regex.Pattern;

/**
 * A post-processor for ant output.  Attempts to capture features from Ant
 * itself (e.g. "BUILD FAILED") and from commonly-used tasks (e.g. javac).
 */
public class AntPostProcessor extends RegexPostProcessor
{
    public AntPostProcessor()
    {
        // Add our built-in patterns.

        // Ant's own build failed messages
        // Ant gives different failure messages in different cases, for
        // example:
        //
        // jsankey@shiny:~/svn/pulse/trunk$ ant -f nosuchfile
        // Buildfile: nosuchfile does not exist!
        // Build failed
        //
        // versus:
        //
        // jsankey@shiny:~/svn/pulse/trunk$ ant nosuchtarget
        // Buildfile: build.xml
        //
        // BUILD FAILED
        // Target `nosuchtarget' does not exist in this project.
        //
        // Total time: 0 seconds
        RegexPattern pattern = createPattern();
        pattern.setPattern(Pattern.compile("^Build failed|^BUILD FAILED"));
        pattern.setCategory(Feature.Level.ERROR);

        // javac task compiler messages
        pattern = createPattern();
        pattern.setPattern(Pattern.compile("\\[javac\\] .*:[0-9]+:"));
        ExpressionElement exclude = pattern.createExclude();
        Pattern warningPattern = Pattern.compile("\\[javac\\] .*:[0-9]+: warning");
        exclude.setPattern(warningPattern);
        pattern.setCategory(Feature.Level.ERROR);

        pattern = createPattern();
        pattern.setPattern(warningPattern);
        pattern.setCategory(Feature.Level.WARNING);
        
        // Unfortunately the ant.bat file on windows does not exit with
        // a non-zero code on failure.  Thus, we need to rely on the output
        // to see if ant is reporting failure.
        if (!SystemUtils.IS_WINDOWS)
        {
            // By default, prefer the exit code!
            setFailOnError(false);
        }

        setLeadingContext(5);
        setTrailingContext(5);
    }

    public AntPostProcessor(String name)
    {
        this();
        setName(name);
    }
}
