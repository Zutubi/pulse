package com.zutubi.pulse.core.commands.maven;

import com.zutubi.pulse.core.commands.core.JUnitSummaryPostProcessorConfiguration;
import com.zutubi.pulse.core.commands.core.PostProcessorGroupConfiguration;
import com.zutubi.pulse.core.commands.core.RegexPostProcessorConfiguration;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfigurationSupport;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.util.SystemUtils;

/**
 * Post-processor to extract comon maven build messages.
 */
@SymbolicName("zutubi.mavenPostProcessorConfig")
public class MavenPostProcessorConfiguration extends PostProcessorConfigurationSupport
{
    private static final String JUNIT_PROCESSOR_NAME = "junit.summary";
    private static final String MAVEN_ERRORS_PROCESSOR_NAME = "maven.errors";

    private static final String[] errorRegexs = new String[]{
            ".*BUILD FAILED.*",
            "Basedir.*does not exist",
            ".*The build cannot continue because of the following unsatisfied dependencies:.*"
    };

    public MavenPostProcessorConfiguration()
    {
        super(MavenPostProcessor.class);
    }

    public MavenPostProcessorConfiguration(String name)
    {
        this();
        setName(name);
    }

    public PostProcessorGroupConfiguration asGroup()
    {
        PostProcessorGroupConfiguration group = new PostProcessorGroupConfiguration();
        group.setFailOnError(isFailOnError());
        group.setFailOnWarning(isFailOnWarning());
        
        // Add a JUnit summary post processor.  It comes first as the output
        // appears as the tests are run.
        group.getProcessors().put(JUNIT_PROCESSOR_NAME, new JUnitSummaryPostProcessorConfiguration(JUNIT_PROCESSOR_NAME));

        // Regex for error patterns from maven itself
        RegexPostProcessorConfiguration maven = new RegexPostProcessorConfiguration(MAVEN_ERRORS_PROCESSOR_NAME);
        maven.addErrorRegexes(errorRegexs);

        if (!SystemUtils.IS_WINDOWS)
        {
            maven.setFailOnError(false);
        }

        maven.setLeadingContext(1);
        maven.setTrailingContext(6);
        group.getProcessors().put(MAVEN_ERRORS_PROCESSOR_NAME, maven);

        return group;
    }
}
