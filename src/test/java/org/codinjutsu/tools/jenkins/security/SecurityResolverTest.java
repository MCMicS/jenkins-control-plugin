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

import org.junit.Ignore;
import org.junit.Test;

import java.net.URL;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;


public class SecurityResolverTest {
    @Test
    @Ignore
    public void testResolveSSLSecurity() throws Exception {

        SecurityMode securityMode = SecurityResolver.resolve(new URL("https://localhost:8443/"));
        assertThat(securityMode, equalTo(SecurityMode.SSL));
    }

    @Test
    @Ignore
    public void testResolveNoSecurity() throws Exception {
        SecurityMode securityMode = SecurityResolver.resolve(new URL("http://localhost:8080/"));
        assertThat(securityMode, equalTo(SecurityMode.NONE));
    }

    @Test
    @Ignore
    public void testResolveJenkinsSecurity() throws Exception {
        SecurityMode securityMode = SecurityResolver.resolve(new URL("http://localhost:8080/"));
        assertThat(securityMode, equalTo(SecurityMode.BASIC));
    }
}
