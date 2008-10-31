package com.zutubi.pulse.core.scm.api;

/**
 * An exception to be thrown when an operation is to be cancelled.  Used for
 * cooperative cancellation of long-running tasks.  When a long-running task is
 * passed a callback interface with a {@code checkCancelled} method, the task
 * should periodically call that method and allow it to throw this exception if
 * it has detected that the operation has been cancelled.  Usually the task can
 * just allow this exception to propagate and be handled be the calling
 * infrastructure.
 *
 * @see com.zutubi.pulse.core.scm.api.ScmFeedbackHandler
 */
public class ScmCancelledException extends ScmException
{
    /**
     * Create a new cancelled exception with the given message.
     *
     * @param message human-readable error message
     */
    public ScmCancelledException(String message)
    {
        super(message);
    }

    /**
     * Create a new cancelled exception with the given root cause.
     *
     * @param cause cause of the exception, or null if there is no root cause
     */
    public ScmCancelledException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Create a new cancelled exception with the given message and root cause.
     *
     * @param message human-readable error message
     * @param cause   cause of the exception, or null if there is no root cause
     */
    public ScmCancelledException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
