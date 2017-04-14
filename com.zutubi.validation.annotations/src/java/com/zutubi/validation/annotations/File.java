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

package com.zutubi.validation.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that the field value is the path of a file on the local
 * filesystem with various properties.
 */
@Constraint("com.zutubi.validation.validators.FileValidator")
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface File
{
    static final String DEFAULT_defaultKeySuffix = "";

    static final boolean DEFAULT_shortCircuit = true;

    String defaultKeySuffix() default DEFAULT_defaultKeySuffix;

    boolean shortCircuit() default DEFAULT_shortCircuit;

    /**
     * @return true if the field value must be a valid file.
     * @see java.io.File#isFile()
     */
    boolean verifyFile() default false;

    /**
     * @return true if the field value must be a valid directory.
     * @see java.io.File#isDirectory()
     */
    boolean verifyDirectory() default false;

    /**
     * @return true if the field value must be readable
     * @see java.io.File#canRead()
     */
    boolean verifyReadable() default false;

    /**
     * @return true if the field value must be writable
     * @see java.io.File#canWrite()
     */
    boolean verifyWritable() default false;
}
