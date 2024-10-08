/*
 * Copyright 2014 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.cdiunit.internal.servlet.common;

import static io.github.cdiunit.internal.servlet.common.ExceptionUtils.illegalInstantiation;

public final class WebConstants {
    public static final String DATE_FORMAT_HEADER = "EEE, d MMM yyyy HH:mm:ss z";

    private WebConstants() throws IllegalAccessException {
        illegalInstantiation();
    }

}
