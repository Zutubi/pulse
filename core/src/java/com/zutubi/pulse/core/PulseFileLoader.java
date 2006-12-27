package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.Property;
import com.zutubi.pulse.model.ResourceRequirement;

import java.io.ByteArrayInputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Convenience class for creating loaders for pulse files with types registered.
 */
public class PulseFileLoader extends FileLoader
{
    public PulseFileLoader()
    {
        ComponentRegistry registry = new ComponentRegistry();
        registry.register("property", Property.class);
        registry.register("recipe", Recipe.class);
        registry.register("def", ComponentDefinition.class);
        registry.register("post-processor", PostProcessorGroup.class);
        registry.register("command", CommandGroup.class);
        registry.register("ant.pp", AntPostProcessor.class);
        registry.register("cppunit.pp", CppUnitReportPostProcessor.class);
        registry.register("junit.pp", JUnitReportPostProcessor.class);
        registry.register("junit.summary.pp", JUnitSummaryPostProcessor.class);
        registry.register("ocunit.pp", OCUnitReportPostProcessor.class);
        registry.register("regex-test.pp", RegexTestPostProcessor.class);
        registry.register("make.pp", MakePostProcessor.class);
        registry.register("maven.pp", MavenPostProcessor.class);
        registry.register("maven2.pp", Maven2PostProcessor.class);
        registry.register("regex.pp", RegexPostProcessor.class);
        registry.register("xcodebuild.pp", XCodePostProcessor.class);
        registry.register("ant", AntCommand.class);
        registry.register("maven", MavenCommand.class);
        registry.register("maven2", Maven2Command.class);
        registry.register("executable", ExecutableCommand.class);
        registry.register("print", PrintCommand.class);
        registry.register("sleep", SleepCommand.class);
        registry.register("make", MakeCommand.class);
        registry.register("xcodebuild", XCodeCommand.class);
        registry.register("resource", ResourceReference.class);

        setRegistry(registry);
    }

    public List<ResourceRequirement> loadRequiredResources(String pulseFile, String recipe) throws PulseException
    {
        List<ResourceRequirement> requirements = new LinkedList<ResourceRequirement>();

        PulseFile file = new PulseFile();
        ResourceRequirementsPredicate predicate = new ResourceRequirementsPredicate(file, recipe);
        load(new ByteArrayInputStream(pulseFile.getBytes()), file, new Scope(), new FileResourceRepository(), predicate);

        for(ResourceReference reference: predicate.getReferences())
        {
            if(reference.isRequired())
            {
                requirements.add(new ResourceRequirement(reference.getName(), reference.getVersion()));
            }
        }

        return requirements;
    }
}
