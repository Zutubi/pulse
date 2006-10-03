package com.zutubi.validation.i18n;

import java.util.List;

/**
 * <class-comment/>
 */
public interface TextProvider
{
    /**
     * Gets a message based on a message key, or null if no message is found.
     *
     * @param key the resource bundle key that is to be searched for
     * @return the message as found in the resource bundle, or null if none is found.
     */
    String getText(String key);

    /**
     * Gets a message based on a key, or, if the message is not found, a supplied
     * default value is returned.
     *
     * @param key          the resource bundle key that is to be searched for
     * @param defaultValue the default value which will be returned if no message is found
     * @return the message as found in the resource bundle, or defaultValue if none is found
     */
    String getText(String key, String defaultValue);

    /**
     * Gets a message based on a key using the supplied args, as defined in
     * {@link java.text.MessageFormat}, or null if no message is found.
     *
     * @param key  the resource bundle key that is to be searched for
     * @param args a list args to be used in a {@link java.text.MessageFormat} message
     * @return the message as found in the resource bundle, or null if none is found.
     */
    String getText(String key, Object... args);

    /**
     * Gets a message based on a key using the supplied args, as defined in
     * {@link java.text.MessageFormat}, or, if the message is not found, a supplied
     * default value is returned.
     *
     * @param key          the resource bundle key that is to be searched for
     * @param defaultValue the default value which will be returned if no message is found
     * @param args         a list args to be used in a {@link java.text.MessageFormat} message
     * @return the message as found in the resource bundle, or defaultValue if none is found
     */
    String getText(String key, String defaultValue, Object... args);

    TextProvider getTextProvider(Object context);
}
