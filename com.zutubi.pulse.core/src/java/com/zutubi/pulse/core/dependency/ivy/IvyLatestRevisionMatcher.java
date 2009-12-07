package com.zutubi.pulse.core.dependency.ivy;

/**
 * An extraction of the 'latest' revision processing from ivy.  The
 * expected format of the revision string is 'latest.'<status>, and
 * uses the status's priority to determine whether or not the revision
 * string matches a particular status.
 */
public class IvyLatestRevisionMatcher
{
    /**
     * The revision prefix that triggers the latest revision matching.
     */
    public static final String LATEST = "latest.";

    /**
     * Returns true if this revision matcher can be applied to the revision.
     *
     * @param revision the revision being tested.
     *
     * @return true if the matcher can be applied, false otherwise.
     */
    public boolean isApplicable(String revision)
    {
        return revision.startsWith(LATEST) && IvyStatus.isValidStatus(extractStatus(revision));
    }

    /**
     * Returns true if the target status matches the revision.
     *
     * @param requiredRevision  the latest revision string.
     * @param targetStatus      the status we match the revision against
     *
     * @return true if the status matches the revision.
     */
    public boolean accept(String requiredRevision, String targetStatus)
    {
        String askedStatus = extractStatus(requiredRevision);
        return IvyStatus.getPriority(askedStatus) >= IvyStatus.getPriority(targetStatus);
    }

    private String extractStatus(String revision)
    {
        return revision.substring(LATEST.length());
    }
}

