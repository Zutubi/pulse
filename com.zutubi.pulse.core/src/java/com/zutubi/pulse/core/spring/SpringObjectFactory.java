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

package com.zutubi.pulse.core.spring;

import com.zutubi.util.bean.DefaultObjectFactory;

/**
 * An implementation of {@link com.zutubi.util.bean.ObjectFactory} that uses
 * the {@link SpringComponentContext} to wire the objects on creation.
 */
public class SpringObjectFactory extends DefaultObjectFactory
{
    public <T> T buildBean(Class<? extends T> clazz)
    {
        T object = super.buildBean(clazz);
        SpringComponentContext.autowire(object);
        return object;
    }

    public <T> T buildBean(String className, Class<? super T> supertype)
    {
        return buildBean(this.<T>getClassInstance(className, supertype));
    }

    @Override
    public <T> T buildBean(Class<? extends T> clazz, Object... args)
    {
        T object = super.buildBean(clazz, args);
        SpringComponentContext.autowire(object);
        return object;
    }

    public <T> T buildBean(Class<? extends T> clazz, Class[] argTypes, Object[] args)
    {
        T object = super.buildBean(clazz, argTypes, args);
        SpringComponentContext.autowire(object);
        return object;
    }

    public <T> T buildBean(String className, Class<? super T> supertype, Class[] argTypes, Object[] args)
    {
        return buildBean(this.<T>getClassInstance(className, supertype), argTypes, args);
    }
}
