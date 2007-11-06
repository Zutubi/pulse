package com.zutubi.pulse.upgrade;

/**
 * @deprecated
 */
public class UpgradeTaskProgress
{
    //TODO: I18N: These status values should be i18n keys.
    public static final String PENDING = "pending";
    public static final String IN_PROGRESS = "in progress";
    public static final String COMPLETE = "complete";
    public static final String FAILED = "failed";
    public static final String ABORTED = "aborted";

    private String name;
    private String desc;
    private String status;
    private String message;

    public UpgradeTaskProgress(UpgradeTask task)
    {
        this.name = task.getName();
        this.desc = task.getDescription();
        this.status = PENDING;
    }

    public boolean hasFailed()
    {
        return this.status.equals(FAILED);
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
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
