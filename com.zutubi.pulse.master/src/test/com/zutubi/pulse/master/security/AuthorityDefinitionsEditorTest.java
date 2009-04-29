package com.zutubi.pulse.master.security;

import com.zutubi.pulse.core.test.api.PulseTestCase;

import java.util.List;

public class AuthorityDefinitionsEditorTest extends PulseTestCase
{
    private AuthorityDefinitionsEditor editor;

    public void setUp() throws Exception
    {
        super.setUp();

        editor = new AuthorityDefinitionsEditor();
    }

    public void testSinglePrivilege()
    {
        assertPrivileges("/, ROLE_USER, GET",
                new String[]{"/", "ROLE_USER", "GET"}
        );
    }

    public void testSinglePrivilegeWithMultipleMethods()
    {
        assertPrivileges("/, ROLE_USER, GET, PUT, POST",
                new String[]{"/", "ROLE_USER", "GET", "PUT", "POST"}
        );
    }

    public void testMultiplePrivileges()
    {
        assertPrivileges("/, ROLE_ANONYMOUS, GET\n" +
                "/, ROLE_USER, POST",
                new String[]{"/", "ROLE_ANONYMOUS", "GET"},
                new String[]{"/", "ROLE_USER", "POST"}
        );
    }

    private void assertPrivileges(String inputText, String[]... privileges)
    {
        editor.setAsText(inputText);

        AuthorityDefinitions definitions = (AuthorityDefinitions)editor.getValue();
        List<Privilege> configuredPrivileges = definitions.getPrivileges();
        assertEquals(privileges.length, configuredPrivileges.size());

        for (int i = 0; i < privileges.length; i++)
        {
            Privilege configuredPrivilege = configuredPrivileges.get(i);
            String[] expectedPrivilege = privileges[i];
            assertEquals(expectedPrivilege[0], configuredPrivilege.getPath());
            assertEquals(expectedPrivilege[1], configuredPrivilege.getRole());

            String[] methods = new String[expectedPrivilege.length - 2];
            System.arraycopy(expectedPrivilege, 2, methods, 0, methods.length);

            for (String method : methods)
            {
                assertTrue(configuredPrivilege.getMethods().contains(method));
            }
        }
    }
}

