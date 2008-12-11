package com.zutubi.pulse.core.scm.p4;

import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

public class PerforceWorkspaceTest extends PulseTestCase
{
    public void testParseEmpty()
    {
        failedParseHelper("", "Required field");
    }

    public void testParseEmptyField() throws IOException
    {
        failedParseHelper("Required field");
    }

    public void testParseUnexpectedMultiField() throws IOException
    {
        failedParseHelper("Expected a single value");
    }

    public void testParseSimple() throws ScmException, IOException
    {
        PerforceWorkspace workspace = parseHelper();
        assertEquals("my-client", workspace.getName());
        assertEquals("caeser", workspace.getHost());
        assertEquals(Arrays.asList("Created by p4.", "Munted by Jason."), workspace.getDescription());
        assertEquals("c:\\my\\root path\\has\\a\\space", workspace.getRoot());
        assertEquals(new HashSet<String>(Arrays.asList("noallwrite", "noclobber", "nocompress", "unlocked", "nomodtime", "normdir")), workspace.getOptions());
        assertEquals(Arrays.asList("//spec/... //caeser/spec/...", "//depot/... //caeser/depot/..."), workspace.getView());
        assertEquals(Arrays.asList("local"), workspace.getUnrecognised().get("LineEnd"));
    }

    public void testRename()
    {
        final String NEW_NAME = "newname";

        PerforceWorkspace workspace = new PerforceWorkspace("name", "root", Arrays.asList("//depot/... //name/..."));
        workspace.rename(NEW_NAME);
        assertEquals(NEW_NAME, workspace.getName());
        assertEquals(Arrays.asList("//depot/... //" + NEW_NAME + "/..."), workspace.getView());
    }

    private String loadSpecification()  throws IOException
    {
        File f = getTestDataFile("bundles/com.zutubi.pulse.core.scm.p4", getName(), "txt");
        return IOUtils.fileToString(f);
    }

    private PerforceWorkspace parseHelper() throws ScmException, IOException
    {
        PerforceWorkspace workspace = PerforceWorkspace.parseSpecification(loadSpecification());

        // Round trip through toSpecification to test that too.
        String spec = workspace.toSpecification();
        PerforceWorkspace other = PerforceWorkspace.parseSpecification(spec);
        assertEquals(workspace, other);

        return workspace;
    }

    private void failedParseHelper(String expectedMessage) throws IOException
    {
        failedParseHelper(loadSpecification(), expectedMessage);
    }

    private void failedParseHelper(String specification, String expectedMessage)
    {
        try
        {
            PerforceWorkspace.parseSpecification(specification);
            fail("Expected parsing to fail");
        }
        catch (ScmException e)
        {
            assertTrue("'" + e.getMessage() + "' does not contain '" + expectedMessage + "'", e.getMessage().contains(expectedMessage));
        }
    }
}
