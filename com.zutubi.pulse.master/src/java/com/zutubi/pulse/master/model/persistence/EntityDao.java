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

package com.zutubi.pulse.master.model.persistence;

import com.zutubi.pulse.core.model.Entity;

import java.util.List;

/**
 * The base interface for all entity dao implementations.
 */
public interface EntityDao<T extends Entity>
{
    /**
     * Find the entity identified by the specified unique identifier.
     * @param id    the id uniquely identifying the entity.
     * @return the entity or null if it does not exist.
     */
    T findById(long id);

    /**
     * Force a flush of the current changes to the database.
     */
    void flush();

    /**
     * Find all instances of the entity defined by the implementation of this type.
     * @return a list of entities.
     */
    List<T> findAll();

    /**
     * Save the entity to the persistent store.  If the entity does not have a persistent
     * representation, then one is created, otherwise the persistent representation is
     * updated.
     *
     * @param entity    the entity being saved.
     */
    void save(T entity);

    void delete(T entity);

    void refresh(T entity);

    long count();
}
