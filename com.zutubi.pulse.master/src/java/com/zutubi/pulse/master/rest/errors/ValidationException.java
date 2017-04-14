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

package com.zutubi.pulse.master.rest.errors;

import com.zutubi.tove.config.api.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a validation failure of a composite instance on insert/update.
 */
public class ValidationException extends RuntimeException
{
    private final Error error;

    public ValidationException()
    {
        super("Validation failed");
        this.error = new Error(this);
    }

    public ValidationException(Configuration instance)
    {
        this(instance, null);
    }

    public ValidationException(Configuration instance, String key)
    {
        super("Validation failed");
        this.error = new Error(this, instance, key);
    }

    public boolean hasErrors()
    {
        return error.hasErrors();
    }

    public void addFieldError(String field, String message)
    {
        error.addFieldError(field, message);
    }

    public void addInstanceError(String message)
    {
        error.addInstanceError(message);
    }

    public Error getError()
    {
        return error;
    }

    public static class Error extends ApiExceptionHandler.Error
    {
        private final Map<String, List<String>> validationErrors;
        private final String key;

        public Error(ValidationException ex)
        {
            super(ex);
            validationErrors = new HashMap<>();
            key = null;
        }

        public Error(ValidationException ex, Configuration instance, String key)
        {
            super(ex);
            if (instance == null)
            {
                validationErrors = new HashMap<>();
            }
            else
            {
                validationErrors = new HashMap<>(instance.getFieldErrors());
                validationErrors.put("", new ArrayList<>(instance.getInstanceErrors()));
            }

            this.key = key;
        }

        public void addInstanceError(String message)
        {
            addFieldError("", message);
        }

        public void addFieldError(String field, String message)
        {
            List<String> list = validationErrors.get(field);
            if (list == null)
            {
                list = new ArrayList<>();
                validationErrors.put(field, list);
            }

            list.add(message);
        }

        public String getKey()
        {
            return key;
        }

        public boolean hasErrors()
        {
            for (List<String> list: validationErrors.values())
            {
                if (list.size() > 0)
                {
                    return true;
                }
            }

            return false;
        }

        public Map<String, List<String>> getValidationErrors()
        {
            return validationErrors;
        }
    }
}
