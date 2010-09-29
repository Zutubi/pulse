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
        return browser.isRegexPresent("(\\d)+" + property + "=");
    }

    public boolean isPropertyPresentWithValue(String property, String value)
    {
        return browser.isRegexPresent("(\\d)+" + property + "=" + value);
    }

    public boolean isPulsePropertyPresent(String property)
    {
        return browser.isRegexPresent("(\\d)+" + prefixedProperty(property));
    }

    public boolean isPulsePropertyPresentWithValue(String property, String value)
    {
        return browser.isRegexPresent("(\\d)+" + prefixedProperty(property) + value);
    }

    private String prefixedProperty(String property)
    {
        return "PULSE_" + normaliseProperty(property) + "=";
    }

    private String normaliseProperty(String property)
    {
        return property.toUpperCase().replace('.', '_');
    }
}