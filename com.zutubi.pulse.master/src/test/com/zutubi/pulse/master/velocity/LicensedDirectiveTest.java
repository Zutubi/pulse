package com.zutubi.pulse.master.velocity;

import com.zutubi.pulse.master.license.LicenseHolder;

import java.util.Arrays;

public class LicensedDirectiveTest extends VelocityDirectiveTestCase
{
    public String[] getUserDirectives()
    {
        return new String[]{"com.zutubi.pulse.master.velocity.LicensedDirective"};
    }

    public void testSingleRequiredAuthorisation() throws Exception
    {
        // configure license.

        LicenseHolder.setAuthorizations(Arrays.asList("canAddProject"));

        assertEquals("", evaluate("#licensed()Licensed#end"));
        assertEquals("Licensed", evaluate("#licensed(\"require=canAddProject\")Licensed#end"));
        assertEquals("", evaluate("#licensed(\"require=canAddUser\")Licensed#end"));
    }
    
    public void testMultipeRequiredAuthorisations() throws Exception
    {
        LicenseHolder.setAuthorizations(Arrays.asList("canAddProject", "canAddUser"));

        assertEquals("Licensed", evaluate("#licensed(\"require=canAddProject, canAddUser\")Licensed#end"));
        assertEquals("", evaluate("#licensed(\"require=canAddUser, canAddAgent\")Licensed#end"));

    }
}
