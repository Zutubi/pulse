package com.zutubi.pulse.acceptance;

/**
 * Acceptance tests that verify operation of the configuration UI by trying
 * some real cases against a running server.
 */
public class ConfigUIAcceptanceTest extends SeleniumTestBase
{
    public void testEmptyOptionsAddedForSelects() throws Exception
    {
        // When configuring a template and a single select is shown, that
        // single select should have an empty option added.
        loginAsAdmin();
        goTo(Navigation.LOCATION_PROJECT_CONFIG);
        addProject("project " + random);
    }

}
