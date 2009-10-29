package com.zutubi.pulse.core.postprocessors.api;

import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wizard;
import com.zutubi.validation.annotations.Required;

/**
 * <p>
 * A support base for post processors that find test results.  Includes
 * standard support for failing the build on a failed test, and for adding
 * to a custom child suite.
 * </p>
 * <p>
 * Note that it is common for reports to be in XML format, for which more
 * specific support classes ({@link com.zutubi.pulse.core.postprocessors.api.DomTestReportPostProcessorSupport} and
 * {@link com.zutubi.pulse.core.postprocessors.api.StAXTestReportPostProcessorSupport}) are available.
 * </p>
 *
 * @see com.zutubi.pulse.core.postprocessors.api.StAXTestReportPostProcessorSupport
 * @see com.zutubi.pulse.core.postprocessors.api.DomTestReportPostProcessorSupport
 */
@SymbolicName("zutubi.testPostProcessorConfigSupport")
@Form(fieldOrder = {"name", "failOnFailure", "suite", "resolveConflicts"})
public abstract class TestReportPostProcessorConfigurationSupport extends PostProcessorConfigurationSupport
{
    @Wizard.Ignore
    private String suite;
    @Wizard.Ignore
    private boolean failOnFailure = true;
    @Wizard.Ignore @Required
    private NameConflictResolution resolveConflicts = NameConflictResolution.OFF;

    protected TestReportPostProcessorConfigurationSupport(Class<? extends TestReportPostProcessorSupport> postProcessorType)
    {
        super(postProcessorType);
    }

    public String getSuite()
    {
        return suite;
    }

    /**
     * Sets the name of a nested suite to add all found test results to.
     * This is especially useful when the same tests are processed twice in a
     * build.  By using two processors with different nested suites
     * collisions may be avoided.
     *
     * @param suite name of a nested suite to create and add all tests to
     */
    public void setSuite(String suite)
    {
        this.suite = suite;
    }

    /**
     * @see #setFailOnFailure(boolean)
     * @return the current value of the failOnFailure flag
     */
    public boolean getFailOnFailure()
    {
        return failOnFailure;
    }

    /**
     * If set to true, the command (and thus build) will be failed if any
     * failed test case is discovered by this processor.  This flag is true
     * by default.
     *
     * @param failOnFailure true to fail build on a failed test case
     */
    public void setFailOnFailure(boolean failOnFailure)
    {
        this.failOnFailure = failOnFailure;
    }

    public NameConflictResolution getResolveConflicts()
    {
        return resolveConflicts;
    }

    public void setResolveConflicts(NameConflictResolution resolveConflicts)
    {
        this.resolveConflicts = resolveConflicts;
    }
}