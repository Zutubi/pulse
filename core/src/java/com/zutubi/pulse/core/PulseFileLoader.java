package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.Property;

/**
 * Convenience class for creating loaders for pulse files with types registered.
 */
public class PulseFileLoader extends FileLoader
{
    public PulseFileLoader(ObjectFactory factory, ResourceRepository repository)
    {
        super(factory, repository);
        register("property", Property.class);
        register("recipe", Recipe.class);
        register("def", ComponentDefinition.class);
        register("post-processor", PostProcessorGroup.class);
        register("command", CommandGroup.class);
        register("ant.pp", AntPostProcessor.class);
        register("junit.pp", JUnitReportPostProcessor.class);
        register("make.pp", MakePostProcessor.class);
        register("regex.pp", RegexPostProcessor.class);
        register("ant", AntCommand.class);
        register("maven", MavenCommand.class);
        register("executable", ExecutableCommand.class);
        register("print", PrintCommand.class);
        register("make", MakeCommand.class);
        register("resource", ResourceReference.class);
    }
}
