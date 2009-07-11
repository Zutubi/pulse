package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The decorated artifact page for the command environment artifact.
 */
public class EnvironmentArtifactPage extends CommandArtifactPage
{
    private static final String ARTIFACT_PATH = "environment/env.txt";

    public EnvironmentArtifactPage(SeleniumBrowser browser, Urls urls, String projectName, long buildId, String stageName, String commandName)
    {
        super(browser, urls, projectName, buildId, stageName, commandName, ARTIFACT_PATH);
    }

    public boolean isPropertyPresent(String property)
    {
        return browser.isTextPresent(getPropertyPrefix(property));
    }

    public boolean isPropertyPresentWithValue(String property, String value)
    {
        return browser.isTextPresent(getPropertyPrefix(property) + value);
    }

    private String getPropertyPrefix(String property)
    {
        return "PULSE_" + property.toUpperCase().replace('.', '_') + "=";
    }
}