package com.zutubi.pulse.form.mock;

import com.zutubi.pulse.form.descriptor.annotation.Radio;

/**
 * <class-comment/>
 */
public class MockRadio
{
    private String option;

    @Radio(list={"a", "b", "c"}) public String getOption()
    {
        return option;
    }

    public void setOption(String option)
    {
        this.option = option;
    }
}
