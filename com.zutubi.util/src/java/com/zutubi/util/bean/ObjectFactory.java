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

package com.zutubi.util.bean;

/**
 * Interface for a generic factory, used to build instances by type.
 */
public interface ObjectFactory
{
    /**
     * Creates an instance of the given class using the default constructor.
     *
     * @param clazz the type to construct an instance of
     * @param <T> the type of the returned insstance
     * @return the new instance
     * @throws RuntimeException on error
     */
    public <T> T buildBean(Class<? extends T> clazz);

    /**
     * Creates an instance of the given named class using the default,
     * constructor and casts it to the given type.
     *
     * @param className name of the class to construct an instance of
     * @param supertype type to cast the instance to
     * @param <T> the type of the returned insstance
     * @return the new instance
     * @throws RuntimeException on error
     */
    public <T> T buildBean(String className, Class<? super T> supertype);

    /**
     * Creates an instance of the given class using a constructor with the
     * given arguments.  This may fail if multiple constructors could accept
     * the given arguments, in which case
     * {@link #buildBean(Class, Class[], Object[])} should be used.
     *
     * @param clazz    the type to construct an instance of
     * @param args     arguments to pass to the constructor (must match a
     *                 single constructor)
     * @param <T> the type of the returned instance
     * @return the new instance
     * @throws RuntimeException on error
     */
    <T> T buildBean(Class<? extends T> clazz, Object... args);

    /**
     * Creates an instance of the given class using a constructor with the
     * given argument types.  Only required in the case where the matching
     * constructor cannot be inferred from the argument values.
     *
     * @param clazz    the type to construct an instance of
     * @param argTypes types of the constructor arguments
     * @param args     arguments to pass to the constructor (must match the
     *                 types)
     * @param <T> the type of the returned instance
     * @return the new instance
     * @throws RuntimeException on error
     */
    <T> T buildBean(Class<? extends T> clazz, Class[] argTypes, Object[] args);

    /**
     * Creates an instance of the given named class using a constructor with
     * the given argument types.  The result is cast to the given type.
     *
     * @param className name of the class to construct an instance of
     * @param supertype type to cast the instance to
     * @param argTypes  types of the constructor arguments
     * @param args      arguments to pass to the constructor (must match the
     *                  types)
     * @param <T> the type of the returned insstance
     * @return the new instance
     * @throws RuntimeException on error
     */
    <T> T buildBean(String className, Class<? super T> supertype, Class[] argTypes, Object[] args);

    /**
     * Loads the class of the given name, ensuring it can be assigned to the
     * token type.
     *
     * @param className name of the class to load
     * @param supertype type that the class must be a subtype of
     * @param <T>       the type that the returned class represents
     * @return the loaded class
     */
    <T> Class<? extends T> getClassInstance(String className, Class<? super T> supertype);
}
