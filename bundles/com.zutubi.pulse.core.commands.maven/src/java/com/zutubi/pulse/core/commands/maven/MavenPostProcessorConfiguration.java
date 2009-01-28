package com.zutubi.pulse.core.commands.maven;

import com.zutubi.pulse.core.commands.core.JUnitSummaryPostProcessorConfiguration;
import com.zutubi.pulse.core.commands.core.PostProcessorGroupConfiguration;
import com.zutubi.pulse.core.commands.core.RegexPostProcessorConfiguration;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.util.SystemUtils;

/**
 * Post-processor to extract comon maven build messages.
 */
@SymbolicName("zutubi.mavenPostProcessorConfig")
public class MavenPostProcessorConfiguration extends PostProcessorGroupConfiguration
{
    private static final String[] errorRegexs = new String[]{
            ".*BUILD FAILED.*",
            "Basedir.*does not exist",
            ".*The build cannot continue because of the following unsatisfied dependencies:.*"
    };

    public MavenPostProcessorConfiguration()
    {
        // Add a JUnit summary post processor.  It comes first as the output
        // appears as the tests are run.
        getProcessors().add(new JUnitSummaryPostProcessorConfiguration());

        // Regex for error patterns from maven itself
        RegexPostProcessorConfiguration maven = new RegexPostProcessorConfiguration();
        maven.addErrorRegexs(errorRegexs);

        if (!SystemUtils.IS_WINDOWS)
        {
            maven.setFailOnError(false);
        }

        maven.setLeadingContext(1);
        maven.setTrailingContext(6);
        getProcessors().add(maven);
    }

    public MavenPostProcessorConfiguration(String name)
    {
        this();
        setName(name);
    }
}
