/*
 * Copyright 2013 the original author or authors.
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
package io.github.cdiunit.deltaspike.tests;

import org.apache.deltaspike.core.api.exclude.Exclude;

@Exclude
public class Scoped {

    private Runnable disposeListener;

    public Scoped() {

    }

    public void setDisposedListener(Runnable disposeListener) {
        this.disposeListener = disposeListener;

    }

    public void dispose() {
        disposeListener.run();
    }
}
