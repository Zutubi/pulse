/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.core.scm.cvs;

import com.zutubi.pulse.core.engine.api.ResourceProperty;
import com.zutubi.pulse.core.scm.api.Changelist;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmException;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CvsClientTest extends AbstractCvsClientTestCase
{
    private String cvsRoot = null;

    private String password;

    public void setUp() throws Exception
    {
        super.setUp();

        // test repository root.
        cvsRoot = ":ext:cvs-1.12.9@zutubi.com:/cvsroots/default";
        password = getPassword("cvs-1.12.9");
    }

    /*
     * Retrieve the changes between two revisions.
     */
    public void testGetChangesBetween() throws ParseException, ScmException
    {
        CvsClient cvsClient = new CvsClient(cvsRoot, "unit-test/CvsClientTest/testGetChangesBetween", password, null);
        Revision from = new Revision(localTime("2006-05-08 11:07:00 GMT"));
        Revision to = new Revision(localTime("2006-05-08 11:08:00 GMT"));
        assertEquals(1, cvsClient.getChanges(scmContext, from, to).size());
    }

    /*
     * When requesting the changes between a revision and itself, nothing
     * should be returned.
     */
    public void testGetChangesBetweenRevisionAndItself() throws ParseException, ScmException
    {
        CvsClient cvsClient = new CvsClient(cvsRoot, "unit-test/CvsClientTest/testGetChangesBetween", password, null);
        Revision from = new Revision("daniel::" + localTime("2006-05-08 11:07:15 GMT"));
        List<Changelist> changes = cvsClient.getChanges(scmContext, from, from);
        assertNotNull(changes);
        assertEquals(0, changes.size());
    }

    /*
     * Verify that the upper bound is included, and the lower bound is not.
     */
    public void testGetChangesBetweenTwoRevisions() throws ParseException, ScmException
    {
        CvsClient cvsClient = new CvsClient(cvsRoot, "unit-test/CvsClientTest/testGetChangesBetweenTwoRevisions", password, null);
        Revision from = new Revision("daniel::" + localTime("2006-05-08 11:12:24 GMT"));
        Revision to = new Revision("daniel::" + localTime("2006-05-08 11:16:16 GMT"));
        List<Changelist> changes = cvsClient.getChanges(scmContext, from, to);
        assertEquals(1, changes.size());
    }

    public void testGetChangesCorrectlyFiltersResults() throws ParseException, ScmException
    {
        CvsClient cvsClient = new CvsClient(cvsRoot, "unit-test/CvsClientTest/testGetChangesCorrectlyFiltersResults", password, null);
        Revision from = new Revision(localTime("2006-06-19 00:00:00 GMT"));
        Revision to = new Revision(localTime("2006-06-21 00:00:00 GMT"));

        // filter nothing.
        List<Changelist> changes = cvsClient.getChanges(scmContext, from, to);
        assertEquals(1, changes.size());
        assertEquals(6, changes.get(0).getChanges().size());

        // filter all .txt files
        cvsClient.setFilterPaths(Collections.<String>emptyList(), Arrays.asList("**/*.txt"));
        changes = cvsClient.getChanges(scmContext, from, to);
        assertEquals(1, changes.size());
        assertEquals(4, changes.get(0).getChanges().size());

        // filter the file.txt files in the subdirectory.
        cvsClient.setFilterPaths(Collections.<String>emptyList(), Arrays.asList("**/directory/file.txt"));
        changes = cvsClient.getChanges(scmContext, from, to);
        assertEquals(1, changes.size());
        assertEquals(5, changes.get(0).getChanges().size());

        // filter .txt and everything from the directory subdirectory.
        cvsClient.setFilterPaths(Collections.<String>emptyList(), Arrays.asList("**/*.txt", "**/directory/*"));
        changes = cvsClient.getChanges(null, from, to);
        assertEquals(1, changes.size());
        assertEquals(2, changes.get(0).getChanges().size());

        // filter everything.
        cvsClient.setFilterPaths(Collections.<String>emptyList(), Arrays.asList("**/*"));
        changes = cvsClient.getChanges(null, from, to);
        assertEquals(0, changes.size());
    }

    public void testTestConnection()
    {
        assertTestConnectionSucceeds(cvsRoot, "unit-test/moduleA", password, null);
    }

    public void testTestConnectionMultipleValidModules()
    {
        assertTestConnectionSucceeds(cvsRoot, "unit-test/moduleA, unit-test/moduleB", password, null);
    }

    public void testTestConnectionInvalidModule()
    {
        assertTestConnectionFails(cvsRoot, "invalid-moduleA", password, null);
    }

    public void testTestConnectionMultipleInvalidModules()
    {
        assertTestConnectionFails(cvsRoot, "invalid-moduleA, invalid-moduleB", password, null);
    }

    private void assertTestConnectionSucceeds(String cvsRoot, String modules, String password, String branch)
    {
        CvsClient cvsClient = new CvsClient(cvsRoot, modules, password, branch);
        try
        {
            cvsClient.testConnection();
        }
        catch (ScmException e)
        {
            fail("Unexpected exception: " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private void assertTestConnectionFails(String cvsRoot, String modules, String password, String branch)
    {
        CvsClient cvsClient = new CvsClient(cvsRoot, modules, password, branch);
        try
        {
            cvsClient.testConnection();
            fail();
        }
        catch (ScmException e)
        {
            // expected.
        }
    }

    public void testCheckoutMultipleModules() throws ScmException
    {
        String module = "unit-test/moduleA, unit-test/moduleB";
        CvsClient client = new CvsClient(cvsRoot, module, password, null);
        assertEquals(module, client.getModule());

        client.checkout(exeContext, Revision.HEAD, null);

        assertFileExists("unit-test/moduleA");
        assertFileExists("unit-test/moduleB");
    }

    public void testGetRevisionsSince() throws ParseException, ScmException
    {
        CvsClient cvsClient = new CvsClient(cvsRoot, "unit-test/CvsClientTest/testGetChangesBetween", password, null);
        Revision from = new Revision(localTime("2006-05-08 11:07:00 GMT"));
        List<Revision> revisions = cvsClient.getRevisions(null, from, null);
        assertNotNull(revisions);
        assertEquals(1, revisions.size());
    }

    public void testContextProperties() throws ScmException
    {
        CvsClient cvsClient = new CvsClient(cvsRoot, "unit-test/moduleA", password, null);
        List<ResourceProperty> properties = cvsClient.getProperties(exeContext);
        assertEquals(2, properties.size());
        assertProperty(properties.get(0), CvsClient.PROPERTY_CVS_ROOT, cvsRoot);
        assertProperty(properties.get(1), CvsClient.PROPERTY_CVS_MODULE, "unit-test/moduleA");
    }

    public void testContextPropertiesWithMultipleModules() throws ScmException
    {
        CvsClient cvsClient = new CvsClient(cvsRoot, "unit-test/moduleA, unit-test/moduleB", password, "branch");
        List<ResourceProperty> properties = cvsClient.getProperties(exeContext);
        assertEquals(6, properties.size());
        assertProperty(properties.get(0), CvsClient.PROPERTY_CVS_ROOT, cvsRoot);
        assertProperty(properties.get(1), CvsClient.PROPERTY_CVS_BRANCH, "branch");
        assertProperty(properties.get(2), CvsClient.PROPERTY_CVS_MODULE, "unit-test/moduleA, unit-test/moduleB");
        assertProperty(properties.get(3), CvsClient.PROPERTY_CVS_MODULE_COUNT, "2");
        assertProperty(properties.get(4), CvsClient.PREFIX_CVS_MODULE + "1", "unit-test/moduleA");
        assertProperty(properties.get(5), CvsClient.PREFIX_CVS_MODULE + "2", "unit-test/moduleB");
    }

    private void assertProperty(ResourceProperty property, String name, Object value)
    {
        assertEquals(name, property.getName());
        assertEquals(value, property.getValue());
    }

    public void testGetPreviousFileRevision() throws ScmException
    {
        assertPreviousRevision(null, "1.1");
        assertPreviousRevision("1.3", "1.4");
        assertPreviousRevision("1.8.4.1", "1.8.4.2");
        assertPreviousRevision("1.8", "1.8.4.1");
    }

    private void assertPreviousRevision(String expected, String current) throws ScmException
    {
        CvsClient client = new CvsClient(cvsRoot, "moduleA", password, "");
        Revision revision = client.getPreviousRevision(null, new Revision(current), true);
        assertEquals(expected, (revision != null) ? revision.getRevisionString() : null);
    }
}