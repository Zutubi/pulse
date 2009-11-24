package com.zutubi.pulse.core.postprocessors.api;

/**
 * Support base class for XML test-report post-processors.  Identical to
 * {@link com.zutubi.pulse.core.postprocessors.api.PostProcessorTestCase}, but
 * uses "xml" as the default extension for test input files.
 *
 * @see com.zutubi.pulse.core.postprocessors.api.PostProcessorTestCase
 */
public abstract class XMLTestPostProcessorTestCase extends TestPostProcessorTestCase
{
    public static final String EXTENSION_XML = "xml";

    @Override
    protected String getExtension()
    {
        return EXTENSION_XML;
    }
}
