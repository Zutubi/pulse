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

package com.zutubi.pulse.master.vfs.provider.pulse;

import org.apache.commons.vfs.FileObject;

import java.util.Comparator;

/**
 * For file objects that can provide their own comparator for sorting
 * children.
 */
public interface ComparatorProvider
{
    /**
     * @return a comparator that should be used to sort children for human
     *         consumption, or null if the children should not be sorted
     */
    Comparator<FileObject> getComparator();
}
