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

public class SecurityClientFactory {

    public static SecurityClient basic(String username, String password, String crumbData) {
        return new BasicSecurityClient(username, password, crumbData);
    }

    public static SecurityClient none(String crumbData) {
        return new DefaultSecurityClient(crumbData);
    }

    public static SecurityClient basicVer2(String username, String password, String crumbData) {
        BasicSecurityClient basicSecurityClient = new BasicSecurityClient(username, password, crumbData);
        basicSecurityClient.setVersion(DefaultSecurityClient.Version.VER_2);
        return basicSecurityClient;
    }

    public static SecurityClient noneVer2(String crumbData) {
        DefaultSecurityClient defaultSecurityClient = new DefaultSecurityClient(crumbData);
        defaultSecurityClient.setVersion(DefaultSecurityClient.Version.VER_2);
        return defaultSecurityClient;
    }
}
