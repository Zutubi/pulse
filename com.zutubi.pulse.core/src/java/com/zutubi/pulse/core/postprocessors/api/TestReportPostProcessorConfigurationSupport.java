/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
@Form(fieldOrder = {"name", "failOnFailure", "suite", "resolveConflicts", "expectedFailureFile"})
public abstract class TestReportPostProcessorConfigurationSupport extends PostProcessorConfigurationSupport
{
    @Wizard.Ignore
    private String suite;
    @Wizard.Ignore
    private boolean failOnFailure = true;
    @Wizard.Ignore @Required
    private NameConflictResolution resolveConflicts = NameConflictResolution.WORST_RESULT;
    @Wizard.Ignore
    private String expectedFailureFile;

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

    /**
     * @see #setExpectedFailureFile(String)
     * @return the path of the expected failure file, or null if none has been
     *         specified
     */
    public String getExpectedFailureFile()
    {
        return expectedFailureFile;
    }

    /**
     * Sets the path, relative to the base directory for the build, of a file
     * containing expected failure information.  This file should be a simple
     * text file with the fully-qualified names of test cases one per line.  A
     * qualified case name has the form:
     * <pre>
     *   &lt;suite name&gt;/&lt;suite name&gt;/&lt;case name&gt;
     * </pre>
     * where the suite names are the (possibly nested) suites under which the
     * case is found.  If a name contains a slash it should be percent-encoded
     * as %2f, likewise a literal percent should be encoded as %25.
     * <p/>
     * If any cases listed in the file fail, their status will be marked as
     * {@link com.zutubi.pulse.core.postprocessors.api.TestStatus#EXPECTED_FAILURE}.
     * The recipe will not be failed due to such failures.
     *
     * @param expectedFailureFile path, relative to the base directory, of a
     *                            file naming expected test failures
     */
    public void setExpectedFailureFile(String expectedFailureFile)
    {
        this.expectedFailureFile = expectedFailureFile;
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