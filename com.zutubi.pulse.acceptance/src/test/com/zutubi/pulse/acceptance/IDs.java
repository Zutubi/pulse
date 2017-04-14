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

package com.zutubi.pulse.acceptance;

import org.openqa.selenium.By;

/**
 */
public class IDs
{
    public static final String ID_LOGIN         = "login";
    public static final String ID_DASHBOARD_TAB = "tab.dashboard";
    public static final String ID_PREFERENCES   = "prefs";
    public static final String ID_LOGOUT        = "logout-text";

    public static String COLLECTION_TABLE = "config-table";
    public static String LINKS_BOX = "config-links";
    public static String STATUS_MESSAGE = "status-message";
    public static String GENERIC_ERROR = "generic-error";
    public static String ACTION_ERRORS = "action-errors";

    private static By buildTab(String name)
    {
        return By.id("tab.build." + name);
    }

    public static By buildLogsTab()
    {
        return buildTab("logs");
    }

    public static By buildDetailsTab()
    {
        return buildTab("details");
    }

    public static By buildChangesTab()
    {
        return buildTab("changes");
    }

    public static By buildTestsTab()
    {
        return buildTab("tests");
    }

    public static By buildFileTab()
    {
        return buildTab("file");
    }

    public static By buildArtifactsTab()
    {
        return buildTab("artifacts");
    }
}
