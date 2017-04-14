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

package com.zutubi.tove.config;

import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.config.events.*;

/**
 * A type listener is a convenient way to listen for all events that affect
 * instances of a given type.  It hides details such as where the types can
 * be located and also raises changed events when a child is inserted,
 * deleted or changed.
 */
public abstract class TypeListener<T extends Configuration> implements ConfigurationEventListener
{
    private Class<T> configurationClass;

    public TypeListener(Class<T> configurationClass)
    {
        this.configurationClass = configurationClass;
    }

    public void register(final ConfigurationProvider configurationProvider, boolean synchronous)
    {
        configurationProvider.registerEventListener(this, synchronous, true, configurationClass);
    }

    public void handleConfigurationEvent(ConfigurationEvent event)
    {
        Object instance = event.getInstance();
        // We take advantage of the fact that a type is only allowed to
        // appear once in a path.  Thus if we have the type we expected, we
        // know it has no ancestor of the same type to worry about.
        if(configurationClass.isInstance(instance))
        {
            // Change occurred directly to an instance of our type
            if (event instanceof InsertEvent)
            {
                insert((T) event.getInstance());
            }
            else if (event instanceof DeleteEvent)
            {
                delete((T) event.getInstance());
            }
            else if(event instanceof SaveEvent)
            {
                save((T) event.getInstance(), false);
            }
            else if (event instanceof PostInsertEvent)
            {
                postInsert((T) event.getInstance());
            }
            else if(event instanceof PostSaveEvent)
            {
                postSave((T) event.getInstance(), false);
            }
            else if (event instanceof PostDeleteEvent)
            {
                postDelete((T) event.getInstance());
            }
        }
        else
        {
            // We ignore cascaded inserts/deleted as the event for the root
            // of the cascade is enough to trigger the desired event.
            if (!cascaded(event))
            {
                // Change occurred to a child path.  No matter what happened, it
                // is seen as a change to our own path.
                T ancestor = event.getConfigurationTemplateManager().getAncestorOfType((Configuration) instance, configurationClass);
                if (ancestor != null)
                {
                    if (event.isPost())
                    {
                        postSave(ancestor, true);
                    }
                    else
                    {
                        save(ancestor, true);
                    }
                }
            }
        }

    }

    private boolean cascaded(ConfigurationEvent event)
    {
        if(event instanceof CascadableEvent)
        {
            return ((CascadableEvent)event).isCascaded();
        }
        else
        {
            return false;
        }
    }

    public abstract void insert(T instance);
    public abstract void postInsert(T instance);
    public abstract void delete(T instance);
    public abstract void postDelete(T instance);
    public abstract void save(T instance, boolean nested);
    public abstract void postSave(T instance, boolean nested);
}
