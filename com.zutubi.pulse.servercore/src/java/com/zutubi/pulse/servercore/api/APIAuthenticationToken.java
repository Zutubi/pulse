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

package com.zutubi.pulse.servercore.api;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * The api token manages the encoding / decoding of the token used
 * by the api for authenticating requests.
 */
public class APIAuthenticationToken
{
    private long expiryTime;
    private String username;
    private String signature;

    public APIAuthenticationToken(String username, String password, long expiryTime)
    {
        this.username = username;
        this.expiryTime = expiryTime;
        this.signature = DigestUtils.md5Hex(username + ":" + expiryTime + ":" + password);
    }

    public APIAuthenticationToken(String token)
    {
        String decoded = new String(Base64.decodeBase64(token.getBytes()));
        String[] parts = decoded.split(":");
        if (parts.length != 3)
        {
            throw new IllegalArgumentException("Invalid token format.");
        }
        String username = parts[0];
        String expiryTime = parts[1];

        // verify the signature? Not needed since our handling of the tokens ensures that they are always valid.
        // We keep a copy of created tokens and require that the user provided token first matches one of these.

        this.username = username;
        this.expiryTime = Long.valueOf(expiryTime);
        this.signature = parts[2];
    }

    public String toString()
    {
        String tokenValue = this.username + ":" + this.expiryTime + ":" + this.signature;
        return new String(Base64.encodeBase64(tokenValue.getBytes()));
    }

    public String getUsername()
    {
        return this.username;
    }

    public long getExpiryTime()
    {
        return expiryTime;
    }
}
