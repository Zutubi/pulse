package com.zutubi.pulse.master.model;

/**
 * Describes why a build occurred (i.e. why the request was triggered).
 */
public interface BuildReason extends Cloneable
{
    boolean isUser();

    String getSummary();

    /**
     * @return the name of the configured trigger that fired to cause the build when there is one
     *         and it is known, null otherwise (e.g. remote API trigger, indirect trigger via
     *         dependencies)
     */
    String getTriggerName();

    Object clone() throws CloneNotSupportedException;
}
