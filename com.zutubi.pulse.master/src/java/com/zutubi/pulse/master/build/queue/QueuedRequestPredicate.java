package com.zutubi.pulse.master.build.queue;

import com.google.common.base.Predicate;

/**
 * A predicate for a QueuedRequest instance.
 */
public interface QueuedRequestPredicate extends Predicate<QueuedRequest>
{
}
