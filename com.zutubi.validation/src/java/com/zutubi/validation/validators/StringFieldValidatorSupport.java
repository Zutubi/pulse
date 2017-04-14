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

package com.zutubi.validation.validators;

import com.zutubi.util.StringUtils;
import com.zutubi.validation.ValidationException;

/**
 * Helper base for validation of string fields.  Also supports ignoring unset
 * string values.
 */
public abstract class StringFieldValidatorSupport extends FieldValidatorSupport
{
    /**
     * If true, strings that are not set are ignored by this validator. This
     * flag should be used in conjunction with the required validator to allow
     * fields to remain blank in templates.
     *
     * Defaults to true.
     *
     * @see StringUtils#stringSet(String) 
     */
    private boolean ignoreEmpty = true;

    protected StringFieldValidatorSupport()
    {
    }

    protected StringFieldValidatorSupport(String defaultKeySuffix)
    {
        super(defaultKeySuffix);
    }

    protected StringFieldValidatorSupport(boolean ignoreEmpty)
    {
        this.ignoreEmpty = ignoreEmpty;
    }

    protected StringFieldValidatorSupport(String defaultKeySuffix, boolean ignoreEmpty)
    {
        super(defaultKeySuffix);
        this.ignoreEmpty = ignoreEmpty;
    }

    public void setIgnoreEmpty(boolean ignoreEmpty)
    {
        this.ignoreEmpty = ignoreEmpty;
    }

    protected void validateField(Object value) throws ValidationException
    {
        if (value == null)
        {
            if (ignoreEmpty)
            {
                return;
            }
        }
        else if(!(value instanceof String))
        {
            throw new ValidationException("Expecting string value for field '" + getFieldLabel() + "'");
        }

        String s = (String) value;
        if (!StringUtils.stringSet(s) && ignoreEmpty)
        {
            return;
        }

        validateStringField(s);
    }

    protected abstract void validateStringField(String value) throws ValidationException;
}
