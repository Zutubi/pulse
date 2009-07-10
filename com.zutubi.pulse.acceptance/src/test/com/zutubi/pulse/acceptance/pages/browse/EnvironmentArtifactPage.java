package com.zutubi.pulse.acceptance.pages.browse;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The decorated artifact page for the command environment artifact.
 */
public class EnvironmentArtifactPage extends CommandArtifactPage
{
    private static final String ARTIFACT_PATH = "environment/env.txt";

    public EnvironmentArtifactPage(Selenium selenium, Urls urls, String projectName, long buildId, String stageName, String commandName)
    {
        super(selenium, urls, projectName, buildId, stageName, commandName, ARTIFACT_PATH);
    }

    public boolean isPropertyPresent(String property)
    {
        return selenium.isTextPresent(getPropertyPrefix(property));
    }

    public boolean isPropertyPresentWithValue(String property, String value)
    {
        return selenium.isTextPresent(getPropertyPrefix(property) + value);
    }

    private String getPropertyPrefix(String property)
    {
        return "PULSE_" + property.toUpperCase().replace('.', '_') + "=";
    }
}