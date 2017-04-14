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

package com.zutubi.tove.config.api;

/**
 * Abstract base for configuration check handlers that supplies the standard
 * template names.
 */
public abstract class AbstractConfigurationCheckHandler<T extends Configuration> extends AbstractConfiguration implements ConfigurationCheckHandler<T>
{
    public String getSuccessTemplate()
    {
        return "tove/check/success.ftl";
    }

    public String getFailureTemplate()
    {
        return "tove/check/failure.ftl";
    }
}
