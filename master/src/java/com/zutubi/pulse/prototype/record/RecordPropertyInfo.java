package com.zutubi.pulse.prototype.record;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

/**
 */
public interface RecordPropertyInfo
{
    String getName();

    Type getType();

    Method getGetter();

    Method getSetter();

    List<Annotation> getAnnotations();

    Object convertValue(Object value, RecordMapper mapper);

    Object unconvertValue(Object value, RecordMapper mapper) throws RecordConversionException;
}
