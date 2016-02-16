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
