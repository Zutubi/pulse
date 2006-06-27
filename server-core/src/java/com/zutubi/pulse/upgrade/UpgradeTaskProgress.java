package com.zutubi.pulse.upgrade;

/**
 * <class-comment/>
 */
public class UpgradeTaskProgress
{
    public static final String PENDING = "pending";
    public static final String IN_PROGRESS = "in progress";
    public static final String COMPLETE = "complete";
    public static final String FAILED = "failed";
    public static final String ABORTED = "aborted";

    private String name;
    private String desc;
    private String status;

    public UpgradeTaskProgress(UpgradeTask task)
    {
        this.name = task.getName();
        this.desc = task.getDescription();
        this.status = PENDING;
    }

    protected void setStatus(String str)
    {
        this.status = str;
    }

    public String getName()
    {
        return name;
    }

    public String getDesc()
    {
        return desc;
    }

    public String getStatus()
    {
        return status;
    }
}
