package com.zutubi.pulse.master.upgrade;

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
