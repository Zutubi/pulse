package com.zutubi.pulse.master.transfer;

/**
 * 
 *
 */
public interface TransferSource
{
    void transferTo(TransferTarget target) throws TransferException;
}
