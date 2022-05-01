/*
 * Copyright (c) 2013 David Boissier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codinjutsu.tools.jenkins.security;

import org.jetbrains.annotations.NotNull;

public class SecurityClientFactory {

    private SecurityClientFactory() {
        throw new IllegalStateException("Utility class");
    }

    private static JenkinsVersion _version;

    public static void setVersion(JenkinsVersion version) {
        _version = version;
    }

    @NotNull
    public static SecurityClient basic(String username, String password, String crumbData, int connectionTimout) {
        return setConnectionProperties(new BasicSecurityClient(username, password, crumbData, connectionTimout));
    }

    @NotNull
    public static SecurityClient none(String crumbData, int connectionTimout) {
        return setConnectionProperties(new DefaultSecurityClient(crumbData, connectionTimout));
    }

    @NotNull
    private static SecurityClient setConnectionProperties(@NotNull DefaultSecurityClient securityClient) {
        securityClient.setJenkinsVersion(_version);
        return securityClient;
    }
}
