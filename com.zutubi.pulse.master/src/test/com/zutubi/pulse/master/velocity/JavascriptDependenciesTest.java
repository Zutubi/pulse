package com.zutubi.pulse.master.velocity;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import static com.zutubi.pulse.master.velocity.JavascriptDependencies.expandAndSortPaths;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.Mapping;
import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class JavascriptDependenciesTest extends PulseTestCase
{
    private File jsRoot;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        jsRoot = new File(JavascriptDependenciesTest.class.getResource("js").toURI());
    }

    public void testEmptyFile() throws IOException
    {
        assertExpected(asList("empty.js"), asList("empty.js"));
    }

    public void testOnlyDependencies() throws IOException
    {
        assertExpected(asList("a.js", "b.js", "onlyDependencies.js"), asList("onlyDependencies.js"));
    }
    
    public void testNoDependencyHeader() throws IOException
    {
        assertExpected(asList("noDependency.js"), asList("noDependency.js"));
    }

    public void testSingleDependencyHeader() throws IOException
    {
        assertExpected(asList("noDependency.js", "singleDependency.js"), asList("singleDependency.js"));
    }

    public void testTransitiveDependency() throws IOException
    {
        assertExpected(asList("noDependency.js", "singleDependency.js", "transitiveDependency.js"), asList("transitiveDependency.js"));
    }

    public void testDirectoryPath() throws IOException
    {
        assertExpected(asList("base/1.js", "base/2.js", "base/relativeDependency.js"), asList("base"));
    }

    public void testRelativeDependency() throws IOException
    {
        assertExpected(asList("base/1.js", "base/relativeDependency.js"), asList("base/relativeDependency.js"));
    }

    public void testParentRelativeDependency() throws IOException
    {
        assertExpected(asList("base/1.js", "base/nested/relativeDependency.js"), asList("base/nested/relativeDependency.js"));
    }

    public void testUnknownDependency() throws IOException
    {
        try
        {
            assertExpected(asList("unknownDependency.js"), asList("unknownDependency.js"));
            fail("expected an exception");
        }
        catch (IOException e)
        {

        }
    }

    private void assertExpected(List<String> expected, List<String> paths) throws IOException
    {
        assertEquals(normalise(expected), normalise(expandAndSortPaths(jsRoot, paths)));
    }

    private List<String> normalise(List<String> paths)
    {
        return CollectionUtils.map(paths, new Mapping<String, String>()
        {
            public String map(String s)
            {
                return FileSystemUtils.localiseSeparators(s);
            }
        });
    }

}
