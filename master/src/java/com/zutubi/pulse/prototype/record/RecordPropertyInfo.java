package com.zutubi.pulse.prototype.record;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @deprecated
 */
public interface RecordPropertyInfo
{
    String getName();

    Type getType();

    Method getGetter();

    Method getSetter();

    List<Annotation> getAnnotations();

    <T extends Annotation> List<T> getAnnotations(final Class<T> type);

    <T extends Annotation> T getAnnotation(final Class<T> type);

    Object convertValue(Object value, RecordMapper mapper);

    Object unconvertValue(Object value, RecordMapper mapper) throws RecordConversionException;
}
