package com.zutubi.validation.i18n;

import java.util.*;
import java.text.MessageFormat;

/**
 * <class-comment/>
 */
public class DefaultTextProvider extends TextProviderSupport
{
    protected String lookupText(String key, Object... args)
    {
        return key;
    }
}
