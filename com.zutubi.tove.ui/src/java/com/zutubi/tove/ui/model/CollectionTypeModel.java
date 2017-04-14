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

import com.zutubi.i18n.Messages;
import com.zutubi.tove.type.CollectionType;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.Type;

/**
 * Model wrapping collection type.
 */
public class CollectionTypeModel extends TypeModel
{
    private CollectionType collectionType;
    private CompositeTypeModel targetType;
    private String targetLabel;

    public CollectionTypeModel(CollectionType type)
    {
        super();
        collectionType = type;

        Type targetType = type.getTargetType();
        if (targetType instanceof CompositeType)
        {
            this.targetType = new CompositeTypeModel((CompositeType) targetType);
            targetLabel = Messages.getInstance(targetType.getClazz()).format("label");
        }
    }

    public boolean isOrdered()
    {
        return collectionType.isOrdered();
    }

    public boolean isKeyed()
    {
        return collectionType.hasSignificantKeys();
    }

    public String getTargetShortType()
    {
        return formatShortType(collectionType.getTargetType());
    }

    public CompositeTypeModel getTargetType()
    {
        return targetType;
    }

    public String getTargetLabel()
    {
        return targetLabel;
    }
}
