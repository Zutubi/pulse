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

package com.zutubi.pulse.core.util.logging;

import java.util.Properties;
import java.util.logging.Handler;

/**
 * The factory interface implemented by all handler factories.
 */
public interface HandlerFactory
{
    /**
     * Create a new handler instance.
     *
     * @param name the name of the handler.
     * @param config the logging configuration properties
     *
     * @return a new instance of a handler configured via the properties in the
     * properties object.
     */
    Handler createHandler(String name, Properties config);
}
