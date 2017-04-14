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
import java.util.logging.Level;
import java.util.logging.MemoryHandler;
import java.util.logging.SimpleFormatter;

/**
 * An implementation of the HandlerFactory used to create instances of the
 * {@link java.util.logging.MemoryHandler}
 *
 * @see java.util.logging.MemoryHandler for details on the supported
 * configuration options.
 */
public class MemoryHandlerFactory implements HandlerFactory
{
    public MemoryHandler createHandler(String name, Properties config)
    {
        int size = LogUtils.getInt(config, name + ".size", 1000);
        if (size <= 0) {
            size = 1000;
        }

        Handler target;
        try
        {
            String clsName = config.getProperty(name + ".target");
            Class cls = Class.forName(clsName);
            target = (Handler)cls.newInstance();
        }
        catch (Exception e)
        {
            return null;
        }

        Level pushLevel = LogUtils.getLevel(config, name+".push", Level.SEVERE);

        MemoryHandler handler = new MemoryHandler(target, size, pushLevel);

        handler.setLevel(LogUtils.getLevel(config, name + ".level", Level.ALL));
        handler.setFilter(LogUtils.getFilter(config, name +".filter", null));
        handler.setFormatter(LogUtils.getFormatter(config, name +".formatter", new SimpleFormatter()));

        return handler;
    }
}
