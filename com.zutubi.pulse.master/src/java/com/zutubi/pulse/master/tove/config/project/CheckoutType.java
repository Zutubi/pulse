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

package com.zutubi.pulse.master.tove.config.project;

/**
 * Defines where/how the checkout from an SCM is done at the start of a build stage.
 */
public enum CheckoutType
{
    /**
     * Nothing is checked out.
     */
    NO_CHECKOUT,
    /**
     * A clean checkout is performed to the temporary recipe directory.
     */
    CLEAN_CHECKOUT,
    /**
     * An incremental checkout is performed to the persistent project work directory.
     */
    INCREMENTAL_CHECKOUT,
}
