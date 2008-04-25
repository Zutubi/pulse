package com.zutubi.pulse.transfer;

import java.util.Map;

/**
 *
 *
 */
public interface TransferListener
{
    void startTable(Table table);
    void row(Map<String, Object> row);
    void endTable();

    void start();

    void end();
}
