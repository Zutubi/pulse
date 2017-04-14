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

import java.util.List;
import java.util.LinkedList;

/**
 * <class-comment/>
 */
public class PackageContextResolver implements ContextResolver<PackageContext>
{
    protected static final String BUNDLE_NAME = "package";

    public String[] resolve(PackageContext context)
    {
        String packageName = context.getContext().replace('.', '/');

        List<String> resolvedNames = new LinkedList<String>();

        while (packageName.length() > 0)
        {
            resolvedNames.add(packageName + "/" + BUNDLE_NAME);
            if (packageName.indexOf('/') == -1)
            {
                break;
            }
            packageName = packageName.substring(0, packageName.lastIndexOf('/'));
        }

        resolvedNames.add(BUNDLE_NAME);

        return resolvedNames.toArray(new String[resolvedNames.size()]);
    }

    public Class<PackageContext> getContextType()
    {
        return PackageContext.class;
    }
}
