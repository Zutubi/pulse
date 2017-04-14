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

import com.zutubi.util.config.Config;
import com.zutubi.validation.i18n.TextProvider;

/**
 * The context represents the environment in which validation is run.  This
 * includes access to arbitrary configuration and the ability to accumulate
 * validation errors.
 */
public interface ValidationContext extends ValidationAware, TextProvider, Config
{
    boolean shouldIgnoreValidator(Validator validator);
}
