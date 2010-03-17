package com.zutubi.pulse.servercore.agent;

import com.zutubi.util.EnumUtils;

/**
 * Defines a task run during synchronisation of an agent.  Such tasks must
 * currently:
 *
 * <ul>
 *   <li>execute quickly: any slow execution should be taken out of line</li>
 *   <li>be robust to multiple execution</li>
 * </ul>
 *
 * To implement a task, add a new type in the enum here, and subclass {@link SynchronisationTaskSupport}.
 * Note that all tasks must include a constructor that takes a {@link java.util.Properties}
 * instance as its only argument.  These properties should be bound to the
 * task's fields (this comes for free using the support base class).
 */
public interface SynchronisationTask
{
    /**
     * Types of tasks, linked to the classes that implement them.  These types
     * are used to allow simple messages sent to the agent to be turned into
     * executable tasks.
     */
    enum Type
    {
        /**
         * An instruction to remove a specific directory, e.g. a directory that
         * corresponds to a project that has been removed.
         */
        CLEANUP_DIRECTORY(DeleteDirectoryTask.class),
        /**
         * An instruction to rename a directory, e.g. a persistent working
         * directory that includes a name that has changed in the config.
         */
        RENAME_DIRECTORY(RenameDirectoryTask.class),
        /**
         * A task used for testing only.
         */
        TEST(TestSynchronisationTask.class);

        private Class<? extends SynchronisationTask> clazz;

        Type(Class<? extends SynchronisationTask> clazz)
        {
            this.clazz = clazz;
        }

        /**
         * Indicates which class implements tasks of this type.
         *
         * @return the class that implements this type of task
         */
        public Class<? extends SynchronisationTask> getClazz()
        {
            return clazz;
        }

        public String getPrettyString()
        {
            return EnumUtils.toPrettyString(this);
        }

        @Override
        public String toString()
        {
            return EnumUtils.toString(this);
        }
    }

    /**
     * Returns the type of this task, from a pre-defined enumeration.
     *
     * @return the task type
     */
    Type getType();

    /**
     * Converts this task to a message which can be sent to an agent.  The
     * message can be converted back on the agent side by constructing the
     * correct task type using the message's arguments.
     *
     * @return a message encoding this task and its arguments
     */
    SynchronisationMessage toMessage();

    /**
     * Executes this task.  Implementations of this method:
     *
     * <ul>
     *   <li>Must be fast: these tasks are synchronously executed during agent
     *       synchronisation.  Anything lengthy should be done asynchronously
     *       where possible.</li>
     *   <li>Must allow multiple calls: as messages may be retried, even though
     *       they got through the first time (because the master may not
     *       realise this).</li>
     *   <li>Should throw a {@link RuntimeException} on any error, with a
     *       detail message that is human-readable.</li>
     * </ul>
     * 
     * @throws RuntimeException on any error
     */
    void execute();
}
