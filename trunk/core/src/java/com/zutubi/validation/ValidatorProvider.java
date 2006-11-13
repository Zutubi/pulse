package com.zutubi.validation;

import java.util.List;

/**
 * <class-comment/>
 */
public interface ValidatorProvider
{
    List<Validator> getValidators(Object obj);
}
