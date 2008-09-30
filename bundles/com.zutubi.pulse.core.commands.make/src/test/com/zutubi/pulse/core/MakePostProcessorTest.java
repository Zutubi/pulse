package com.zutubi.pulse.core;

import static com.zutubi.pulse.core.BuildProperties.NAMESPACE_INTERNAL;
import static com.zutubi.pulse.core.BuildProperties.PROPERTY_OUTPUT_DIR;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.core.model.PlainFeature;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.test.PulseTestCase;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

/**
 */
public class MakePostProcessorTest extends PulseTestCase
{
    public void testCommandError() throws URISyntaxException
    {
        List<Feature> features = getFeatures("commanderror");
        assertEquals(1, features.size());
        assertFeature(features.get(0), Feature.Level.ERROR,
                        "gcc foo.c\n" +
                        "gcc: foo.c: No such file or directory\n" +
                        "gcc: no input files\n" +
                        "make: *** [gcc] Error 1");
    }

    public void testNoSuchCommand() throws URISyntaxException
    {
        List<Feature> features = getFeatures("nosuchcommand");
        assertEquals(1, features.size());
        assertFeature(features.get(0), Feature.Level.ERROR,
                        "nosuchcommand\n" +
                        "make: nosuchcommand: Command not found\n" +
                        "make: *** [default] Error 127");
    }

    public void testRecursiveError() throws URISyntaxException
    {
        List<Feature> features = getFeatures("recursiveerror");
        assertEquals(1, features.size());
        assertFeature(features.get(0), Feature.Level.ERROR,
                        "make[1]: Entering directory `/home/jason/mt/sm'\n" +
                        "myownerror\n" +
                        "make[1]: myownerror: Command not found\n" +
                        "make[1]: *** [default] Error 127\n" +
                        "make[1]: Leaving directory `/home/jason/mt/sm'\n" +
                        "make: *** [nested] Error 2");
    }

    public void testNoSuchMakefile() throws URISyntaxException
    {
        List<Feature> features = getFeatures("nosuchmakefile");
        assertEquals(1, features.size());
        assertFeature(features.get(0), Feature.Level.ERROR,
                        "make: NoMakefile: No such file or directory\n" +
                        "make: *** No rule to make target `NoMakefile'.  Stop.");
    }

    private void assertFeature(Feature feature, Feature.Level level, String summary)
    {
        assertTrue(feature instanceof PlainFeature);
        PlainFeature pf = (PlainFeature) feature;
        assertEquals(level, pf.getLevel());
        assertEquals(summary, pf.getSummary());
    }

    private List<Feature> getFeatures(String name) throws URISyntaxException
    {
        MakePostProcessor pp = new MakePostProcessor();
        URL url = getInputURL(name, "txt");
        File file = new File(url.toURI());
        StoredFileArtifact artifact = new StoredFileArtifact(file.getName());

        ExecutionContext context = new ExecutionContext();
        context.addString(NAMESPACE_INTERNAL, PROPERTY_OUTPUT_DIR, file.getParentFile().getAbsolutePath());

        pp.process(artifact, new CommandResult("w00t"), context);
        return artifact.getFeatures();
    }

}
