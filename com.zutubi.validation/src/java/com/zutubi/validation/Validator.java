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

package com.zutubi.validation;

/**
 * The base interface used by all objects that validate something.
 */
public interface Validator
{
    /**
     * Getter for the wired validation context.
     *
     * @return wired validation context.
     */
    ValidationContext getValidationContext();

    /**
     * Allow the validation context to be wired into the validator.
     *
     * @param ctx the validation context
     */
    void setValidationContext(ValidationContext ctx);

    /**
     * Validate the specified object, recording the results of the validation
     * within the wired validation context.
     *
     * @param obj to be validated
     *
     * @throws ValidationException if a problem occurs during the validation that
     * prevents the validation from completing successfully.
     */
    void validate(Object obj) throws ValidationException;

}
