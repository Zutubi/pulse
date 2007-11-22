package com.zutubi.pulse.upgrade;

import java.util.List;

/**
 *
 *
 */
public interface UpgradeableComponentSource
{
    boolean isUpgradeRequired();

    List<UpgradeableComponent> getUpgradeableComponents();
}
