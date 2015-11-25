package com.zutubi.pulse.core.events;

/**
 * Event sent from slave to master when a command (simple exe, not in recipe) finishes.
 */
public class SlaveCommandCompletedEvent extends SlaveCommandEvent
{
    private boolean success;
    private String message;

    public SlaveCommandCompletedEvent(Object source, long commandId, boolean success, String message)
    {
        super(source, commandId);
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public String getMessage()
    {
        return message;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        SlaveCommandCompletedEvent that = (SlaveCommandCompletedEvent) o;

        if (success != that.success)
        {
            return false;
        }
        return !(message != null ? !message.equals(that.message) : that.message != null);

    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (success ? 1 : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        String s = "Slave Command Completed Event: " + (success ? "success" : "failure");
        if (message != null)
        {
            s+= ": " + message;
        }
        return s;
    }
}
