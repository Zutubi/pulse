package com.zutubi.pulse.form;

import com.zutubi.pulse.form.squeezer.TypeSqueezer;
import com.zutubi.pulse.form.validator.Validator;

import java.util.List;

/**
 * <class-comment/>
 */
public interface FieldTypeRegistry
{
    String getFieldType(Class type);

    boolean supportsFieldType(String fieldType);

    void register(String fieldType, TypeSqueezer squeezer, List<Validator> validators);

    void unregister(String fieldType);

    TypeSqueezer getSqueezer(String fieldType);

    List<Validator> getValidators(String fieldType);
}
