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

package com.zutubi.pulse.core.marshal.types;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A type with various default values for properties that convert to
 * attributes.  Used to verify that attributes are ommitted from stored
 * files when they alreayd have their default value.
 */
@SymbolicName("defaultValuesConfig")
public class DefaultValuesConfiguration extends AbstractConfiguration
{
    private String stringNull;
    private String stringEmpty = "";
    private String stringNonEmpty = "stuff";

    private int intZero = 0;
    private int intNonZero = 221156;

    private TestEnum enumNull;
    private TestEnum enumNonNull = TestEnum.C_2;

    private List<String> stringListNull;
    private List<String> stringListEmpty = Collections.emptyList();
    private List<String> stringListNonEmpty = Arrays.asList("not", "empty");

    private List<TestEnum> enumListNull;
    private List<TestEnum> enumListEmpty = Collections.emptyList();
    private List<TestEnum> enumListNonEmpty = Arrays.asList(TestEnum.C1, TestEnum.C_2);

    public String getStringNull()
    {
        return stringNull;
    }

    public void setStringNull(String stringNull)
    {
        this.stringNull = stringNull;
    }

    public String getStringEmpty()
    {
        return stringEmpty;
    }

    public void setStringEmpty(String stringEmpty)
    {
        this.stringEmpty = stringEmpty;
    }

    public String getStringNonEmpty()
    {
        return stringNonEmpty;
    }

    public void setStringNonEmpty(String stringNonEmpty)
    {
        this.stringNonEmpty = stringNonEmpty;
    }

    public int getIntZero()
    {
        return intZero;
    }

    public void setIntZero(int intZero)
    {
        this.intZero = intZero;
    }

    public int getIntNonZero()
    {
        return intNonZero;
    }

    public void setIntNonZero(int intNonZero)
    {
        this.intNonZero = intNonZero;
    }

    public TestEnum getEnumNull()
    {
        return enumNull;
    }

    public void setEnumNull(TestEnum enumNull)
    {
        this.enumNull = enumNull;
    }

    public TestEnum getEnumNonNull()
    {
        return enumNonNull;
    }

    public void setEnumNonNull(TestEnum enumNonNull)
    {
        this.enumNonNull = enumNonNull;
    }

    public List<String> getStringListNull()
    {
        return stringListNull;
    }

    public void setStringListNull(List<String> stringListNull)
    {
        this.stringListNull = stringListNull;
    }

    public List<String> getStringListEmpty()
    {
        return stringListEmpty;
    }

    public void setStringListEmpty(List<String> stringListEmpty)
    {
        this.stringListEmpty = stringListEmpty;
    }

    public List<String> getStringListNonEmpty()
    {
        return stringListNonEmpty;
    }

    public void setStringListNonEmpty(List<String> stringListNonEmpty)
    {
        this.stringListNonEmpty = stringListNonEmpty;
    }

    public List<TestEnum> getEnumListNull()
    {
        return enumListNull;
    }

    public void setEnumListNull(List<TestEnum> enumListNull)
    {
        this.enumListNull = enumListNull;
    }

    public List<TestEnum> getEnumListEmpty()
    {
        return enumListEmpty;
    }

    public void setEnumListEmpty(List<TestEnum> enumListEmpty)
    {
        this.enumListEmpty = enumListEmpty;
    }

    public List<TestEnum> getEnumListNonEmpty()
    {
        return enumListNonEmpty;
    }

    public void setEnumListNonEmpty(List<TestEnum> enumListNonEmpty)
    {
        this.enumListNonEmpty = enumListNonEmpty;
    }
}
