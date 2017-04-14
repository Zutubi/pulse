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

package com.zutubi.i18n.bundle;

import java.io.InputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.List;
import java.util.ResourceBundle;

/**
 * A factory interface for creating resource bundle instances.
 */
public interface ResourceBundleFactory
{
    /**
     * Create a resource bundle instance from the input stream.
     *
     * @param input the input stream contains the data to be used to populate
     * the resource bundle.
     * @param locale the locale of the data contained by the input stream.
     * @return a new resource bundle instance
     *
     * @throws IOException if there are any problems loading the resource bundle
     * from the input stream.
     */
    public ResourceBundle loadBundle(InputStream input, Locale locale) throws IOException;

    /**
     * Given the resource name, this method will expand that resource name to
     * a list of comparable resource names based on the specified locale.
     *
     * @param name the base bundle name
     * @param locale the locale to be used when expanding the base name.
     * 
     * @return the list of expanded bundle names.
     */
    public List<String> expand(String name, Locale locale);

}
