package com.zutubi.pulse.core.postprocessors.clang;

import com.zutubi.pulse.core.commands.core.RegexPostProcessor;
import static com.zutubi.pulse.core.engine.api.FeatureMatchers.hasOrderedErrors;
import static com.zutubi.pulse.core.engine.api.FeatureMatchers.hasOrderedWarnings;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorTestCase;
import com.zutubi.pulse.core.postprocessors.api.TestPostProcessorContext;
import static org.hamcrest.MatcherAssert.assertThat;

public class ClangPostProcessorTest extends PostProcessorTestCase
{
    private RegexPostProcessor pp;

    public void setUp() throws Exception
    {
        super.setUp();
        ClangPostProcessorConfiguration config = new ClangPostProcessorConfiguration();
        config.setLeadingContext(0);
        config.setTrailingContext(0);
        pp = new RegexPostProcessor(config);
    }

    public void testErrors() throws Exception
    {
        TestPostProcessorContext context = runProcessor(pp);
        assertThat(context.getFeatures(), hasOrderedErrors(
                "main.c:3:10: error: initializing 'float' with an expression of incompatible type 'char [6]'",
                "main.c:8:10: error: expected ';' at end of declaration"
        ));
    }

    public void testLinkerErrors() throws Exception
    {
        TestPostProcessorContext context = runProcessor(pp);
        assertThat(context.getFeatures(), hasOrderedErrors(
                "Undefined symbols for architecture x86_64:",
                "ld: symbol(s) not found for architecture x86_64",
                "clang: error: linker command failed with exit code 1 (use -v to see invocation)"
        ));
    }

    public void testWarnings() throws Exception
    {
        TestPostProcessorContext context = runProcessor(pp);
        assertThat(context.getFeatures(), hasOrderedWarnings(
                "main.c:3:5: warning: implicitly declaring library function 'printf' with type 'int (const char *, ...)'",
                "main.c:8:5: warning: implicit declaration of function 'foo' is invalid in C99 [-Wimplicit-function-declaration]"
        ));
    }
}
