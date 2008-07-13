package com.zutubi.tove.config.cleanup;

import java.util.List;
import java.util.Set;

/**
 * An action that needs to be undertaken to cleanup when a record is deleted.
 * As the action itself may lead to further records being deleted, further
 * actions may cascade.  The root action itself is always the deletion of the
 * original record.
 */
public interface RecordCleanupTask extends Runnable
{
    /**
     * Executes the cleanup task.  Note that tasks <b>must</b> be robust to
     * paths having been deleted by previous tasks.  That is, no task can
     * assume that any record it may intend to change still exists.
     */
    void run();

    /**
     * Indicates whether this task should run synchronously when the config
     * delete occurs or in the background.  Note that configuration changes
     * <b>must</b> be executed synchronously so that the configuration is
     * consistent at the end of the delete.  Tasks that cleanup external
     * information (such as state objects) can run asynchronously to improve
     * the responsiveness of the UI.
     *
     * @return true if this task should be executed asynchronously in the
     *         background.
     */
    boolean isAsynchronous();

    /**
     * @return true if this is an internal task that the user need not be
     * notified of.
     */
    boolean isInternal();
    
    /**
     * @return the path of the record or record property affected by this
     * task.
     */
    String getAffectedPath();

    /**
     * @return cleanup tasks cascaded from this one: i.e. tasks that must be
     *         carried out to cleanup after records removed by this task
     */
    List<RecordCleanupTask> getCascaded();

    /**
     * Populates the given set with the paths invalidated by this task, and
     * all cascaded tasks.  Used to identify redundant tasks from the tree
     * (e.g. no need to fix path foo/bar/baz if another task is removing
     * foo/bar).
     *
     * @param paths set to which invalidated paths are added
     */
     void getInvalidatedPaths(Set<String> paths);
}
