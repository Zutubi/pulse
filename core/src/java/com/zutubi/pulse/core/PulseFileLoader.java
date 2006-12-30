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
        register("property", Property.class);
        register("recipe", Recipe.class);
        register("def", ComponentDefinition.class);
        register("post-processor", PostProcessorGroup.class);
        register("command", CommandGroup.class);
        register("ant.pp", AntPostProcessor.class);
        register("cppunit.pp", CppUnitReportPostProcessor.class);
        register("junit.pp", JUnitReportPostProcessor.class);
        register("junit.summary.pp", JUnitSummaryPostProcessor.class);
        register("ocunit.pp", OCUnitReportPostProcessor.class);
        register("regex-test.pp", RegexTestPostProcessor.class);
        register("make.pp", MakePostProcessor.class);
        register("maven.pp", MavenPostProcessor.class);
        register("maven2.pp", Maven2PostProcessor.class);
        register("regex.pp", RegexPostProcessor.class);
        register("xcodebuild.pp", XCodePostProcessor.class);
        register("ant", AntCommand.class);
        register("maven", MavenCommand.class);
        register("maven2", Maven2Command.class);
        register("executable", ExecutableCommand.class);
        register("print", PrintCommand.class);
        register("sleep", SleepCommand.class);
        register("make", MakeCommand.class);
        register("xcodebuild", XCodeCommand.class);
        register("resource", ResourceReference.class);
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
