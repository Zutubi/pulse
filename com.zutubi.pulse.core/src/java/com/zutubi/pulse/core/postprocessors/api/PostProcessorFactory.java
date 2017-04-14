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

package com.zutubi.pulse.core.postprocessors.api;

/**
 * Factory for creating post-processors from configuration.
 */
public interface PostProcessorFactory
{
    /**
     * Create a new post-processor from the given configuration.  The
     * configuration identifies the type of processor to create, and that type
     * should have a single-parameter constructor which will accept the
     * configuration as an argument.
     *
     * @param configuration configuration used to build the processor
     * @return the created processor
     * @throws com.zutubi.pulse.core.engine.api.BuildException on any error
     */
    PostProcessor create(PostProcessorConfiguration configuration);
}
