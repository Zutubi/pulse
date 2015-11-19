package com.zutubi.validation;

import com.zutubi.util.config.Config;
import com.zutubi.validation.i18n.TextProvider;

/**
 * The context represents the environment in which validation is run.  This
 * includes access to arbitrary configuration and the ability to accumulate
 * validation errors.
 */
public interface ValidationContext extends ValidationAware, TextProvider, Config
{
    boolean shouldIgnoreValidator(Validator validator);
}
