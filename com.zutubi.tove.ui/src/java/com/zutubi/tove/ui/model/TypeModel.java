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

package com.zutubi.tove.ui.model;

import com.zutubi.tove.type.*;

/**
 * Model for type information.
 */
public class TypeModel
{
    private String symbolicName;

    public TypeModel()
    {
    }

    public TypeModel(String symbolicName)
    {
        this.symbolicName = symbolicName;
    }

    public String getSymbolicName()
    {
        return symbolicName;
    }

    public void setSymbolicName(String symbolicName)
    {
        this.symbolicName = symbolicName;
    }

    public static String formatShortType(Type type)
    {
        if (type instanceof CollectionType)
        {
            String collectionType;
            if (type instanceof ListType)
            {
                collectionType = "List";
            }
            else
            {
                collectionType = "Map";
            }

            return collectionType + "<" + formatShortType(type.getTargetType()) + ">";
        }
        else if (type instanceof ReferenceType)
        {
            return "Reference<" + formatShortType(((ReferenceType) type).getReferencedType()) +">";
        }
        else if (type instanceof EnumType)
        {
            return "Enum";
        }
        else
        {
            return type.getSymbolicName();
        }
    }
}
