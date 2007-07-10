package com.zutubi.prototype;

import com.zutubi.config.annotations.Transient;
import com.zutubi.pulse.core.config.Configuration;

/**
 * Configuration check handlers allow testing of a configuration as a distinct user action.
 *
 * The benefits of this over standard validation include
 * <ul>
 * <li>extra non persistent data can be used in the test</li>
 * <li>testing of the configuration can be done without updating the configuration</li>
 * </ul>
 *
 * A configuration check handler is associated with a configuration via the ConfigurationCheck annotation.
 *
 * The configuration check handler is presented to the UI as a form, and so it has all of the standard
 * form processing support available to it.
 *
 * @see com.zutubi.config.annotations.ConfigurationCheck
 */
public interface ConfigurationCheckHandler<T extends Configuration> extends Configuration
{
    @Transient
    String getSuccessTemplate();

    @Transient
    String getFailureTemplate();

    void test(T configuration) throws Exception;
}
