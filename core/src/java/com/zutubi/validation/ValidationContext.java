package com.zutubi.validation;

import com.zutubi.validation.i18n.LocaleProvider;
import com.zutubi.validation.i18n.TextProvider;

import java.util.Map;

/**
 * <class-comment/>
 */
public interface ValidationContext extends ValidationAware, LocaleProvider, TextProvider
{
    String getFullFieldName(String fieldName);

}
