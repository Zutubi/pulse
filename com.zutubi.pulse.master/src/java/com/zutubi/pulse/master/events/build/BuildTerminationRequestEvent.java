package com.zutubi.pulse.master.events.build;

import com.zutubi.events.Event;
import com.zutubi.util.TimeStamps;

import java.util.Locale;

/**
 * Raised to request that all running builds be forcefully terminated.
 */
public class BuildTerminationRequestEvent extends Event
{
    public static final int ALL_BUILDS = -1;

    /**
     * ID of the build to terminate, or -1 to terminate all builds.
     */
    private long buildId;
    /**
     * If not null, gives a reason for this termination, e.g. "requested by admin".
     */
    private String reason;
    /**
     * Milliseconds since the epoch at the time of this request.
     */
    private long timestamp;

    public BuildTerminationRequestEvent(Object source, long buildId, String reason)
    {
        super(source);
        this.buildId = buildId;
        this.reason = reason;
        timestamp = System.currentTimeMillis();
    }

    public long getBuildId()
    {
        return buildId;
    }

    public String getReason()
    {
        return reason;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public String getMessage()
    {
        String message = "Forceful termination ";
        if (reason != null)
        {
            message += reason + " ";
        }

        message += "at " + TimeStamps.getPrettyDate(timestamp, Locale.getDefault());
        return message;
    }

    public String toString()
    {
        return String.format("Build Termination Request Event[buildId: %s, message: %s]", buildId, getMessage());
    }
}
