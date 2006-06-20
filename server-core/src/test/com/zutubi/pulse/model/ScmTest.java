/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.scm.SCMServer;
import com.zutubi.pulse.scm.SCMException;

import java.util.Arrays;
import java.util.List;

/**
 * <class-comment/>
 */
public class ScmTest extends PulseTestCase
{
    private Scm scm;

    protected void setUp() throws Exception
    {
        super.setUp();

        scm = new MockScm();
    }

    public void testFilterEnabled()
    {
        assertFalse(scm.isFilterEnabled());
        scm.setFilteredPaths(Arrays.asList("path/to/filter"));
        assertTrue(scm.isFilterEnabled());
    }

    public void testSetGetFilteredPaths()
    {
        List<String> expectedPaths = Arrays.asList("path/to/filter/a", "path/to/filter/b", "path/to/filter/c");
        scm.setFilteredPaths(expectedPaths);
        assertObjectEquals(expectedPaths, scm.getFilteredPaths());
    }

    public void testAddFilteredPaths()
    {
        List<String> expectedPaths = Arrays.asList("path/to/filter/a", "path/to/filter/b", "path/to/filter/c");
        for (String path : expectedPaths)
        {
            scm.addExcludedPath(path);
        }
        assertObjectEquals(expectedPaths, scm.getFilteredPaths());
    }

    public void testReOrderingFitleredPaths()
    {
        List<String> expectedPaths = Arrays.asList("path/to/filter/a", "path/to/filter/b", "path/to/filter/c");
        scm.setFilteredPaths(expectedPaths);
        assertObjectEquals(expectedPaths, scm.getFilteredPaths());

        expectedPaths = Arrays.asList("path/to/filter/a", "path/to/filter/c", "path/to/filter/b");
        scm.setFilteredPaths(expectedPaths);
        assertObjectEquals(expectedPaths, scm.getFilteredPaths());
    }

    private class MockScm extends Scm
    {
        public SCMServer createServer() throws SCMException
        {
            return null;
        }
    }
}
