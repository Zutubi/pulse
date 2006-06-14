package com.zutubi.pulse.model;

/**
 * Describes why a build occured (i.e. why the request was triggered).
 */
public interface BuildReason
{
    String getSummary();
}
