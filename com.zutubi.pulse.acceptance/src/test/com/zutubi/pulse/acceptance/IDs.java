package com.zutubi.pulse.acceptance;

/**
 */
public class IDs
{
    public static final String ID_LOGIN         = "login";
    public static final String ID_DASHBOARD_TAB = "tab.dashboard";
    public static final String ID_PREFERENCES   = "prefs";
    public static final String ID_LOGOUT        = "logout";

    public static String COLLECTION_TABLE = "config-table";
    public static String LINKS_BOX = "config-links";
    public static String STATUS_MESSAGE = "status-message";

    private static String buildTab(String name)
    {
        return "tab.build." + name;
    }

    public static String buildDetailsTab()
    {
        return buildTab("details");
    }

    public static String buildChangesTab()
    {
        return buildTab("changes");
    }

    public static String buildTestsTab()
    {
        return buildTab("tests");
    }

    public static String buildFileTab()
    {
        return buildTab("file");
    }

    public static String buildArtifactsTab()
    {
        return buildTab("artifacts");
    }

    public static String buildWorkingCopyTab()
    {
        return buildTab("wc");
    }

    private static String buildCell(String project, long number)
    {
        return project + ".build." + Long.toString(number);
    }

    public static String buildStatusCell(String project, long number)
    {
        return buildCell(project, number) + ".status";
    }

    private static String stageCell(String project, long number, String stage)
    {
        return project + ".build." + Long.toString(number) + ".stage." + stage;
    }

    public static String stageAgentCell(String project, long number, String stage)
    {
        return stageCell(project, number, stage) + ".agent";
    }
}
