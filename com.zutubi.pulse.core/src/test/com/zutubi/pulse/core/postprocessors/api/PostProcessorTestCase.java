package com.zutubi.pulse.core.postprocessors.api;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;

/**
 * Support base class for post-processor tests.  Includes methods for running
 * a processor against an input file, returning a context that can be inspected
 * in test assertions.
 * <p/>
 * A typical test case is expected to look something like:
 * <pre>{@code
 *public void testErrors()
 *{
 *    TestPostProcessorContext context = runProcessor(pp);
 *    assertThat(context.getFeatures(), FeatureMatchers.hasOrderedErrors("error: first error", "error: second error"));
 *}}
 * </pre>
 * Assuming the test class was named CompilerPostProcessorTest the input file
 * used in this case would be call CompilerPostProcessorTest.testErrors.txt,
 * located in the same package as the test class on the classpath.  This case
 * makes use of {@link com.zutubi.pulse.core.postprocessors.api.FeatureMatchers}
 * to create a Hamcrest matcher for verifying features.
 * <p/>
 * For testing post-processors that extract test results, consider using a more
 * targetted subclass such as {@link com.zutubi.pulse.core.postprocessors.api.TestPostProcessorTestCase}.
 *
 * @see com.zutubi.pulse.core.postprocessors.api.TestPostProcessorTestCase
 * @see com.zutubi.pulse.core.postprocessors.api.FeatureMatchers
 */
public abstract class PostProcessorTestCase extends PulseTestCase
{
    private static final String EXTENSION_TEXT = "txt";

    private File tempDir;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        tempDir = FileSystemUtils.createTempDir(getName(), ".tmp");
    }

    @Override
    protected void tearDown() throws Exception
    {
        removeDirectory(tempDir);
        super.tearDown();
    }

    /**
     * Equivalent to runProcessor(postProcessor, getName(), extension).  Runs
     * the processor against the default test input file with the default
     * extension and an empty execution context.
     *
     * @see #runProcessor(PostProcessor, String)
     * @see #runProcessor(PostProcessor, String, String)
     * @see #runProcessor(PostProcessor, String, String, com.zutubi.pulse.core.engine.api.ExecutionContext)
     *
     * @param postProcessor the post-processor to run
     * @return a context with all captured information in a form that can be
     *         inspected for matching/assertions
     * @throws IOException on any error
     */
    public TestPostProcessorContext runProcessor(PostProcessor postProcessor) throws IOException
    {
        return runProcessor(postProcessor, getName(), getExtension());
    }

    /**
     * Equivalent to runProcessor(postProcessor, getName(), extension).  Runs
     * the processor against the default test input file with a custom
     * extension and an empty execution context.
     *
     * @see #runProcessor(PostProcessor)
     * @see #runProcessor(PostProcessor, String, String)
     * @see #runProcessor(PostProcessor, String, String, com.zutubi.pulse.core.engine.api.ExecutionContext)
     *
     * @param postProcessor the post-processor to run
     * @param extension     extension of the test input file to process
     * @return a context with all captured information in a form that can be
     *         inspected for matching/assertions
     * @throws IOException on any error
     */
    public TestPostProcessorContext runProcessor(PostProcessor postProcessor, String extension) throws IOException
    {
        return runProcessor(postProcessor, getName(), extension);
    }

    /**
     * Equivalent to runProcessor(postProcessor, name, extension, createExecutionContext()).
     * Runs the post-processor against the given input file with an empty
     * execution context.
     *
     * @see #runProcessor(PostProcessor)
     * @see #runProcessor(PostProcessor, String)
     * @see #runProcessor(PostProcessor, String, String, com.zutubi.pulse.core.engine.api.ExecutionContext)
     *
     * @param postProcessor the post-processor to run
     * @param name          name of the test input file to process
     * @param extension     extension of the test input file to process
     * @return a context with all captured information in a form that can be
     *         inspected for matching/assertions
     * @throws IOException on any error
     */
    public TestPostProcessorContext runProcessor(PostProcessor postProcessor, String name, String extension) throws IOException
    {
        return runProcessor(postProcessor, name, extension, new PulseExecutionContext());
    }

    /**
     * Runs the given post-processor against the given input file, passing the
     * given execution context, and returns a post-processor context that has
     * captured all information added by the processor for verification.
     * <p/>
     * The test input file is retrieved using {@link #copyInputToDirectory(String, String, java.io.File)},
     * thus it should be located alongside the test class in the classpath.
     *
     * @see #runProcessor(PostProcessor)
     * @see #runProcessor(PostProcessor, String)
     * @see #runProcessor(PostProcessor, String, String)
     *
     * @param postProcessor    the post-processor to run
     * @param name             name of the test input file to process
     * @param extension        extension of the test input file to process
     * @param executionContext execution context passed to the post-processor
     * @return a context with all captured information in a form that can be
     *         inspected for matching/assertions
     * @throws IOException on any error
     */
    public TestPostProcessorContext runProcessor(PostProcessor postProcessor, String name, String extension, ExecutionContext executionContext) throws IOException
    {
        File artifactFile = copyInputToDirectory(name, extension, tempDir);
        TestPostProcessorContext context = new TestPostProcessorContext(executionContext);
        postProcessor.process(artifactFile, context);
        return context;
    }

    /**
     * Factory method to create an empty execution context.
     *
     * @return the new execution context
     */
    public ExecutionContext createExecutionContext()
    {
        return new PulseExecutionContext();
    }

    /**
     * Returns "txt", the default extension to use for test input files.  May
     * be overridden for processors that read different file types.
     *
     * @return the default input file extension
     */
    protected String getExtension()
    {
        return EXTENSION_TEXT;
    }
}
