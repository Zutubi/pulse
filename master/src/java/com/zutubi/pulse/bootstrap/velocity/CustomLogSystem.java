/********************************************************************************
  @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.bootstrap.velocity;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogSystem;

/**
 */
public class CustomLogSystem implements LogSystem
{
//    private static final Logger LOG = Logger.getLogger(CustomLogSystem.class);

    public void init(RuntimeServices runtimeServices) throws Exception
    {
        // Do nothing
    }

    public void logVelocityMessage(int level, String message)
    {
        // Ignore it all because Velocity's loggin is b0rked!
//        switch (level)
//        {
//            case DEBUG_ID:
//                LOG.debug(message);
//                break;
//            case INFO_ID:
//                LOG.info(message);
//                break;
//            case WARN_ID:
//                LOG.warning(message);
//                break;
//            case ERROR_ID:
//                LOG.error(message);
//                break;
//            default:
//                LOG.fine(message);
//                break;
//        }
    }
}
