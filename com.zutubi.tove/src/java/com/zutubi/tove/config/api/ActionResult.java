package com.zutubi.tove.config.api;

/**
 * Holds the result of executing a configuration action, used for giving
 * feedback to the user.
 * <p/>
 * Note this class is designed to be immutable.
 */
public class ActionResult
{
    /**
     * Indicates the success or otherwise of executing the action.
     */
    public enum Status
    {
        /**
         * The action failed, check the message for error details.
         */
        FAILURE,
        /**
         * The action succeeded, the message is feedback on what happened.
         */
        SUCCESS
    }

    /**
     * Indicates if the execution succeeded or not.
     */
    private Status status;
    /**
     * An error or feedback message for the user.  Should be brief (preferably
     * a short phrase).  May include HTML: it will not be escaped when
     * displayed.
     */
    private String message;
    /**
     * If executing the action created a new path, that path is set here.
     */
    private String createdPath;

    /**
     * Creates a new result with the given status and message.
     *
     * @param status  the result of the action execution
     * @param message feedback for the user, may be null to indicate that the
     *                default message should be used
     */
    public ActionResult(Status status, String message)
    {
        this(status, message, null);
    }

    /**
     * Creates a new result with the given status and message for an action that created a given path.
     *
     * @param status the result of the action execution
     * @param message feedback for the user, may be null to indicate that the default message should be used
     * @param createdPath path created by executing the action (may be null)
     */
    public ActionResult(Status status, String message, String createdPath)
    {
        this.status = status;
        this.message = message;
        this.createdPath = createdPath;
    }

    /**
     * @return the result of executing the action
     */
    public Status getStatus()
    {
        return status;
    }

    /**
     * @return a feedback message for the user
     */
    public String getMessage()
    {
        return message;
    }

    public String getCreatedPath()
    {
        return createdPath;
    }
}
