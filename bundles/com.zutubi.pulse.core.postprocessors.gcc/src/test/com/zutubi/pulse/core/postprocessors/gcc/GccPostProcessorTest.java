package com.zutubi.pulse.core.postprocessors.gcc;

import static com.zutubi.pulse.core.postprocessors.api.FeatureMatchers.hasOrderedErrors;
import static com.zutubi.pulse.core.postprocessors.api.FeatureMatchers.hasOrderedWarnings;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorTestCase;
import com.zutubi.pulse.core.postprocessors.api.TestPostProcessorContext;
import static org.hamcrest.MatcherAssert.assertThat;

public class GccPostProcessorTest extends PostProcessorTestCase
{
    private GccPostProcessor pp;

    public void setUp() throws Exception
    {
        pp = new GccPostProcessor();
        super.setUp();
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testErrors() throws Exception
    {
        TestPostProcessorContext context = runProcessor(pp);
        assertThat(context.getFeatures(), hasOrderedErrors(
                "MkDir1 ../bin.v2/libs/tr1/test/tr1_has_tr1_result_of_fail.test/gcc-4.1.2/debug\n" +
                "gcc.compile.c++ ../bin.v2/libs/tr1/test/tr1_has_tr1_result_of_fail.test/gcc-4.1.2/debug/tr1_has_tr1_result_of_fail.o\n" +
                "../libs/config/test/boost_has_tr1_result_of.ipp:16: error: ‘std::tr1’ has not been declared\n" +
                "../libs/config/test/boost_has_tr1_result_of.ipp:16: error: expected initializer before ‘<’ token\n" +
                "../libs/config/test/boost_has_tr1_result_of.ipp:17: error: using ‘typename’ outside of template\n" +
                "../libs/config/test/boost_has_tr1_result_of.ipp:17: error: ‘r’ has not been declared\n" +
                "../libs/config/test/boost_has_tr1_result_of.ipp:17: error: invalid type in declaration before ‘;’ token\n" +
                "(failed-as-expected) ../bin.v2/libs/tr1/test/tr1_has_tr1_result_of_fail.test/gcc-4.1.2/debug/tr1_has_tr1_result_of_fail.o\n" +
                "**passed** ../bin.v2/libs/tr1/test/tr1_has_tr1_result_of_fail.test/gcc-4.1.2/debug/tr1_has_tr1_result_of_fail.test\n" +
                "MkDir1 ../bin.v2/libs/tr1/test/tr1_has_tr1_ref_wrap_fail.test"
        ));
    }

    public void testWarnings() throws Exception
    {
        TestPostProcessorContext context = runProcessor(pp);
        assertThat(context.getFeatures(), hasOrderedWarnings(
                "                 from /home/jsankey/archives/boost_1_34_1/boost/tr1/tr1/strstream:18,\n" +
                "                 from /home/jsankey/archives/boost_1_34_1/libs/tr1/test/std_headers/test_strstream.cpp:6:\n" +
                "/usr/lib/gcc/i486-linux-gnu/4.1.2/../../../../include/c++/4.1.2/backward/backward_warning.h:32:2: warning: #warning This file includes at least one deprecated or antiquated header. Please consider using one of the 32 headers found in section 17.4.1.2 of the C++ standard. Examples include substituting the <X> header for the <X.h> header for C++ includes, or <iostream> instead of the deprecated header <iostream.h>. To disable this warning use -Wno-deprecated.\n" +
                "**passed** ../bin.v2/libs/tr1/test/test_strstream_std_header.test/gcc-4.1.2/debug/test_strstream_std_header.test\n" +
                "MkDir1 ../bin.v2/libs/tr1/test/test_string_std_header.test\n" +
                "MkDir1 ../bin.v2/libs/tr1/test/test_string_std_header.test/gcc-4.1.2"
        ));
    }
}
