package com.zutubi.prototype.type;

import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.core.config.Configuration;

/**
 */
public interface ComplexType extends Type
{
    String getSymbolicName();

    String getSavePath(String path, Record record);

    String getInsertionPath(String path, Record record);

    MutableRecord createNewRecord(boolean applyDefaults);

    boolean isTemplated();

    Type getDeclaredPropertyType(String propertyName);

    Type getActualPropertyType(String propertyName, Object propertyValue);

    /**
     * Checks if the given instance is transitively valid.  Contrast this to
     * {@link Configuration#isValid}, which only checks the instance itself,
     * ignoring nested instances.
     *
     * @param instance the instance to check
     * @return true if the given instance is valid in the transitive sense
     *         (i.e. there are no errors on the instance or any nested
     *         instances).
     */
    boolean isValid(Configuration instance);
}
