package com.zutubi.i18n.bundle;

import java.io.InputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.List;
import java.util.ResourceBundle;

/**
 * <class-comment/>
 */
public interface ResourceBundleFactory
{
    public ResourceBundle loadBundle(InputStream input, Locale locale) throws IOException;

    public List<String> expand(String name, Locale locale);

}
