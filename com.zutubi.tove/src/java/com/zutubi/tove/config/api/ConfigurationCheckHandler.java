package com.zutubi.tove.config.api;

import com.zutubi.tove.annotations.Transient;

/**
 * Configuration check handlers allow testing of a configuration as a distinct user action.
 *
 * The benefits of this over standard validation include
 * <ul>
 * <li>extra non persistent data can be used in the test</li>
 * <li>testing of the configuration can be done without updating the configuration</li>
 * </ul>
 *
 * A configuration check handler is associated with a configuration via convention: the check handler
 * class should have the same (fully qualified) name as the configuration class, with an added
 * "CheckHandler" suffix.
 *
 * The configuration check handler is presented to the UI as a form, and so it has all of the standard
 * form processing support available to it.
 */
public interface ConfigurationCheckHandler<T extends Configuration> extends Configuration
{
    @Transient
    String getSuccessTemplate();

    @Transient
    String getFailureTemplate();

    void test(T configuration) throws Exception;
}
