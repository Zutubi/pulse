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
 * Interface implemented by a validator that can be bypassed if the validation
 * processing that has occured prior to has indicated a validation error.
 */
public interface ShortCircuitableValidator
{
    void setShortCircuit(boolean b);

    /**
     * This validator will not be triggered if the isShortCircuit returns true
     * and the validation context indicates that a validation error has already
     * been registered.
     *
     * @return true to bypass this validator, false otherwise.
     */
    boolean isShortCircuit();
}
