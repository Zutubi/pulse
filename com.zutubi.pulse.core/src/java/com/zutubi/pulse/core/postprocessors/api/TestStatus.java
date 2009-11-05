package com.zutubi.pulse.core.postprocessors.api;

/**
 * Indicates the state of a completed test result (suite or case).
 */
public enum TestStatus
{
    /**
     * The test passed (for suites this implies all nested cases passed).
     */
    PASS(false),
    /**
     * The test failed, but has been flagged as unstable, so this failure will
     * not cause the build to fail.
     */
    EXPECTED_FAILURE(true),
    /**
     * The test failed due to a normal assertion (for suites this implies some
     * nested case failed, but none errored).
     */
    FAILURE(true),
    /**
     * The test failed due to an unexpected error (for suites this implies some nested
     * cases errored).
     */
    ERROR(true),
    /**
     * The test case was not executed, perhaps because it was explicitly disabled,
     * perhaps because a dependent test failed.
     */
    SKIPPED(false);

    private boolean broken;

    TestStatus(boolean broken)
    {
        this.broken = broken;
    }

    /**
     * @return true if this status indicates an unsuccessful test execution
     */
    public boolean isBroken()
    {
        return broken;
    }
}
