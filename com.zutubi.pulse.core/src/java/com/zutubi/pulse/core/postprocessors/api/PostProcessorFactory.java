package com.zutubi.pulse.core.postprocessors.api;

/**
 * Factory for creating post-processors from configuration.
 */
public interface PostProcessorFactory
{
    /**
     * Create a new post-processor from the given configuration.  The
     * configuration identifies the type of processor to create, and that type
     * should have a single-parameter constructor which will accept the
     * configuration as an argument.
     *
     * @param configuration configuration used to build the processor
     * @return the created processor
     * @throws com.zutubi.pulse.core.engine.api.BuildException on any error
     */
    PostProcessor createProcessor(PostProcessorConfiguration configuration);
}
