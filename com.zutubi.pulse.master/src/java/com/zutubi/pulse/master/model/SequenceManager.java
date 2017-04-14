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

package com.zutubi.pulse.master.model;

/**
 * The sequence manager provides Pulse with access to a sequence of numbers.
 * These sequences are characterised by a) not repeating and b) always ascending.
 */
public interface SequenceManager
{
    /**
     * Get the sequence uniquely identified by the specified name.
     *
     * @param name  the name of the sequence to be retrieved.
     * @return the sequence.
     */
    Sequence getSequence(String name);
}
