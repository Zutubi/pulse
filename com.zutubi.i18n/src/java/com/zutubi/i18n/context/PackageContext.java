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

package com.zutubi.i18n.context;

import java.io.InputStream;

/**
 * <class-comment/>
 */
public class PackageContext implements Context
{
    private final String packageName;

    public PackageContext(String packageName)
    {
        if (packageName == null)
        {
            throw new NullPointerException("packageName is required");
        }

        this.packageName = packageName;
    }

    public PackageContext(Object obj)
    {
        packageName = getPackageName(obj);
    }

    public InputStream getResourceAsStream(String name)
    {
        return getClass().getResourceAsStream(name);
    }

    private String getPackageName(Object context)
    {
        if (context == null || context.getClass().getPackage() == null)
        {
            return "";
        }

        if (context instanceof Class)
        {
            return ((Class) context).getPackage().getName();
        }
        else
        {
            return context.getClass().getPackage().getName();
        }
    }

    public String getContext()
    {
        return packageName;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof PackageContext))
        {
            return false;
        }

        final PackageContext packageContext = (PackageContext) o;
        return packageName.equals(packageContext.packageName);
    }

    public int hashCode()
    {
        return packageName.hashCode();
    }

    public String toString()
    {
        return "<" + packageName + ">";
    }
}
