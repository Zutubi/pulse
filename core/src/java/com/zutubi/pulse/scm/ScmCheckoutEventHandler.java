package com.zutubi.pulse.scm;

import com.zutubi.pulse.core.model.Change;

/**
 * A callback interface for receiving information about a checkout in progress.
 */
public interface ScmCheckoutEventHandler
{
    /**
     * Called to report a simple freeform status message.
     *
     * @param message status information about the checkout operation
     */
    void status(String message);

    /**
     * Called when a file is checked out.
     *
     * @param change holds details of the checked out file, including at
     *               least the file path and action
     */
    void fileCheckedOut(Change change);

    /**
     * Called periodically to check if the operation is cancelled.  If this
     * method throws, the checkout operation will exit as soon as
     * possible with an error.
     *
     * @throws ScmCancelledException if the operation should be cancelled
     */
    void checkCancelled() throws ScmCancelledException;
}
