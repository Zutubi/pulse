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

package com.zutubi.tove.type;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public class TypeConversionException extends TypeException
{
    private Map<String, String> fieldErrors = new HashMap<String, String>();

    public TypeConversionException()
    {
    }

    public TypeConversionException(String message)
    {
        super(message);
    }

    public TypeConversionException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public TypeConversionException(Throwable cause)
    {
        super(cause);
    }

    public List<String> getFieldErrors()
    {
        return new LinkedList<String>(fieldErrors.keySet());
    }

    public void addFieldError(String field, String errorMessage)
    {
        fieldErrors.put(field, "" + errorMessage);
    }

    public String getFieldError(String field)
    {
        return fieldErrors.get(field);
    }
}
