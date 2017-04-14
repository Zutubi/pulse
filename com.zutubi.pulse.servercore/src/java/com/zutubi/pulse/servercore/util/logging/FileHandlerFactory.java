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

package com.zutubi.pulse.servercore.util.logging;

import com.zutubi.pulse.core.util.logging.HandlerFactory;
import com.zutubi.pulse.core.util.logging.LogUtils;
import com.zutubi.util.bean.ObjectFactory;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.XMLFormatter;

/**
 */
public class FileHandlerFactory implements HandlerFactory
{
    private ObjectFactory objectFactory;

    public FileHandler createHandler(String name, Properties config)
    {
        try
        {
            FileHandler handler = objectFactory.buildBean(FileHandler.class);
            handler.setPattern(LogUtils.getString(config, name + ".pattern", "%h/java%u.log"));
            handler.setLimit(LogUtils.getInt(config, name + ".limit", 0));
            handler.setCount(LogUtils.getInt(config, name + ".count", 1));
            handler.setAppend(LogUtils.getBoolean(config, name + ".append", false));
            handler.setLevel(LogUtils.getLevel(config, name + ".level", Level.ALL));
            handler.setFilter(LogUtils.getFilter(config, name + ".filter", null));
            handler.setFormatter(LogUtils.getFormatter(config, name + ".formatter", new XMLFormatter()));
            handler.setEncoding(LogUtils.getString(config, name + ".encoding", null));
            return handler;
        }
        catch (Exception e)
        {
            System.err.println("Failed to create file handler: Cause: " + e.getMessage());
            e.printStackTrace(System.err);
            return null;
        }
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
