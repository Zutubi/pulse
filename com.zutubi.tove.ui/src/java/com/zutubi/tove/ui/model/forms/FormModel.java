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

package com.zutubi.tove.ui.model.forms;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a form to display in a UI. Forms are sets of fields, plus some config parameters.
 */
public class FormModel
{
    private List<FieldModel> fields = new ArrayList<>();

    public List<FieldModel> getFields()
    {
        return fields;
    }

    public void addField(FieldModel field)
    {
        fields.add(field);
    }

    public void sortFields(List<String> fieldOrder)
    {
        List<FieldModel> sortedFields = new ArrayList<>(fields.size());
        for (final String fieldName: fieldOrder)
        {
            Optional<FieldModel> maybeField = Iterables.tryFind(fields, new Predicate<FieldModel>()
            {
                @Override
                public boolean apply(FieldModel input)
                {
                    return input.getName().equals(fieldName);
                }
            });

            if (maybeField.isPresent())
            {
                sortedFields.add(maybeField.get());
            }
        }

        fields = sortedFields;
    }
}
