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

package com.zutubi.pulse.servercore.bootstrap;

/**
 * Common application configuration shared by master and slaves.
 */
public interface SystemConfiguration
{
    public static final String WEBAPP_BIND_ADDRESS = "webapp.bindAddress";

    public static final String DEFAULT_WEBAPP_BIND_ADDRESS = "0.0.0.0";

    public static final String WEBAPP_PORT = "webapp.port";

    public static final Integer DEFAULT_WEBAPP_PORT = 8080;

    public static final String CONTEXT_PATH = "webapp.contextPath";

    public static final String DEFAULT_CONTEXT_PATH = "/";

    public static final String PULSE_DATA = "pulse.data";

    public static final String RESTORE_FILE = "pulse.restore.file";

    public static final String RESTORE_ARTIFACTS = "pulse.restore.artifacts";

    public static final String SSL_ENABLED      = "ssl.enabled";
    public static final String SSL_KEYSTORE     = "ssl.keystore";
    public static final String SSL_PASSWORD     = "ssl.password";
    public static final String SSL_KEY_PASSWORD = "ssl.keyPassword";

    /**
     * @return the location of the server config file (note the file may not
     *         exist on agents)
     */
    String getConfigFilePath();

    /**
     * @return the address that we should bind the http server to.
     */
    String getBindAddress();

    /**
     * The port on which the http server will listen for connections.
     *
     * This is a read only property.
     */
    int getServerPort();

    /**
     *
     * This is a read only property.
     */
    String getContextPath();

    String getContextPathNormalised();

    String getRestoreFile();

    String getRestoreArtifacts();

    void setDataPath(String path);

    String getDataPath();

    boolean isSslEnabled();
    String getSslKeystore();
    String getSslPassword();
    String getSslKeyPassword();

}
