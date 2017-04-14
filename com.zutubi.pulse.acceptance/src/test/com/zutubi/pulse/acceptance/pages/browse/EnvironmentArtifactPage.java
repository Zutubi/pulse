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

package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The decorated artifact page for the command environment artifact.
 */
public class EnvironmentArtifactPage extends CommandArtifactPage
{
    private static final String ARTIFACT_PATH = "environment/env.txt";
    private static final String LINE_PREFIX = "\\d+\\s*";

    public EnvironmentArtifactPage(SeleniumBrowser browser, Urls urls, String projectName, long buildId, String stageName, String commandName)
    {
        super(browser, urls, projectName, buildId, stageName, commandName, ARTIFACT_PATH);
    }

    public boolean isPropertyPresent(String property)
    {
        return browser.isRegexPresent(LINE_PREFIX + property + "=");
    }

    public boolean isPropertyPresentWithValue(String property, String value)
    {
        return browser.isRegexPresent(LINE_PREFIX + property + "=" + value);
    }

    public boolean isPulsePropertyPresent(String property)
    {
        return browser.isRegexPresent(LINE_PREFIX + prefixedProperty(property));
    }

    public boolean isPulsePropertyPresentWithValue(String property, String value)
    {
        return browser.isRegexPresent(LINE_PREFIX + prefixedProperty(property) + value);
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