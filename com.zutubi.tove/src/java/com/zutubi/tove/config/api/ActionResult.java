package com.zutubi.tove.config.api;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
     * A list of paths that this action execution has invalidated.  The UI will
     * refresh displays of these paths.
     */
    private List<String> invalidatedPaths;

    /**
     * Creates a new result with the given status and message.
     *
     * @param status  the result of the action execution
     * @param message feedback for the user, may be null to indicate that the
     *                default message should be used
     */
    public ActionResult(Status status, String message)
    {
        this(status, message, Collections.<String>emptyList());
    }

    /**
     * Creates a new result with the given status and message for an action
     * that invalidated some configuration paths.
     *
     * @param status           the result of the action execution
     * @param message          feedback for the user, may be null to indicate
     *                         that the default message should be used
     * @param invalidatedPaths configuration paths that have been invalidated
     *                         by executing the action
     */
    public ActionResult(Status status, String message, Collection<String> invalidatedPaths)
    {
        this.status = status;
        this.message = message;
        this.invalidatedPaths = new LinkedList<>(invalidatedPaths);
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

    /**
     * @return an unmodifiable list of configuration paths invalidated by the
     *         action execution
     */
    public List<String> getInvalidatedPaths()
    {
        return Collections.unmodifiableList(invalidatedPaths);
    }
}
