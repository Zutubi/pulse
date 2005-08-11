package com.cinnamonbob.model;

/**
 *
 * 
 */
public interface ScmManager extends EntityManager<Scm>
{
    Scm getScm(long id);

}
