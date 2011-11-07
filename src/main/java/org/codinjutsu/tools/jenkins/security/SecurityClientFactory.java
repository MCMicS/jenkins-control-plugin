/*
 * Copyright (c) 2011 David Boissier
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

    public static SecurityClient create(SecurityMode securityMode, String username, String password) {
        SecurityClient securityClient;

        if (SecurityMode.BASIC.equals(securityMode)) {

            securityClient = new BasicSecurityClient(username, password);
        } else {
            securityClient = none();
        }

        return securityClient;
    }

    public static SecurityClient none() {
        return new NoSecurityClient();
    }

}
