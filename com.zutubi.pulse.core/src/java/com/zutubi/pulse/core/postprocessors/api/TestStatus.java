package com.zutubi.pulse.core.postprocessors.api;

/**
 * Indicates the state of a completed test result (suite or case).
 */
public enum TestStatus
{
    /**
     * The test passed (for suites this implies all nested cases passed).
     */
    PASS
    {
        public boolean isBroken()
        {
            return false;
        }
    },
    /**
     * The test failed due to a normal assertion (for suites this implies some
     * nested case failed, but none errored).
     */
    FAILURE
    {
        public boolean isBroken()
        {
            return true;
        }
    },
    /**
     * The test failed due to an unexpected error (for suites this implies some nested
     * cases errored).
     */
    ERROR
    {
        public boolean isBroken()
        {
            return true;
        }
    },
    /**
     * The test case was not executed, perhaps because it was explicitly disabled,
     * perhaps because a dependent test failed.
     */
    SKIPPED
    {
        public boolean isBroken()
        {
            return false;
        }
    };

    /**
     * @return true if this status indicates an unsuccessful test execution
     */
    public abstract boolean isBroken();
}
