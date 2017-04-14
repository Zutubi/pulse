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

import java.util.ResourceBundle;

/**
 * <class-comment/>
 */
public abstract class BaseBundle extends ResourceBundle
{
    public BaseBundle()
    {
    }

    public void setParent(ResourceBundle parent)
    {
        if (this == parent)
        {
            throw new IllegalArgumentException("Can't set bundle as its own parent.");
        }
        this.parent = parent;
    }

    public ResourceBundle getParent()
    {
        return parent;
    }
}