package com.zutubi.pulse.acceptance;

/**
 */
public class IDs
{
    public static String COLLECTION_TABLE = "config.table";

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
