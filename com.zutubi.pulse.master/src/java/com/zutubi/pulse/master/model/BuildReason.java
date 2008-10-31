package com.zutubi.pulse.master.model;

/**
 * Describes why a build occured (i.e. why the request was triggered).
 */
public interface BuildReason extends Cloneable
{
    boolean isUser();

    String getSummary();

    Object clone() throws CloneNotSupportedException;
}
