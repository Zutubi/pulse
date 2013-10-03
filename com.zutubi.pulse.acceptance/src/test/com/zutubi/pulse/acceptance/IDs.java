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
