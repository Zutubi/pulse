package com.zutubi.pulse.form.mock;

import com.zutubi.pulse.form.descriptor.annotation.Form;

/**
 * <class-comment/>
 */
@Form(fieldOrder = {"fieldA", "fieldB"})
public class MockForm
{
    private String fieldA;
    private String fieldB;

    public String getFieldA()
    {
        return fieldA;
    }

    public void setFieldA(String fieldA)
    {
        this.fieldA = fieldA;
    }

    public String getFieldB()
    {
        return fieldB;
    }

    public void setFieldB(String fieldB)
    {
        this.fieldB = fieldB;
    }
}
