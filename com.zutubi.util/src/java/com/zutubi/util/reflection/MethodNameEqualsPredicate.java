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

package com.zutubi.util.reflection;

import com.google.common.base.Predicate;

import java.lang.reflect.Method;

/**
 * A predicate to test if a method name equals an expected name.
 */
public class MethodNameEqualsPredicate implements Predicate<Method>
{
    private String name;

    /**
     * Create a predicate matching methods with the given name.
     *
     * @param name the name to test for
     */
    public MethodNameEqualsPredicate(String name)
    {
        this.name = name;
    }

    public boolean apply(Method method)
    {
        return method.getName().equals(name);
    }
}