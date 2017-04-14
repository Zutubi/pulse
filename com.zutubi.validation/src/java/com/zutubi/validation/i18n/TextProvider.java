/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.validation.i18n;

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
     * Gets a message based on a key using the supplied args, as defined in
     * {@link java.text.MessageFormat}, or null if no message is found.
     *
     * @param key  the resource bundle key that is to be searched for
     * @param args a list args to be used in a {@link java.text.MessageFormat} message
     * @return the message as found in the resource bundle, or null if none is found.
     */
    String getText(String key, Object... args);

    TextProvider getTextProvider(Object context);
}
