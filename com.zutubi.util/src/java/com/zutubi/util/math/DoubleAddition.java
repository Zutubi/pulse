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

package com.zutubi.util.math;

import com.zutubi.util.BinaryFunction;

/**
 * A binary function that adds two doubles.
*/
public class DoubleAddition implements BinaryFunction<Double, Double, Double>
{
    public Double process(Double input1, Double input2)
    {
        return input1 + input2;
    }
}