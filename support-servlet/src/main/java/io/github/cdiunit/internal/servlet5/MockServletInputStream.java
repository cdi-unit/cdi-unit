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
package io.github.cdiunit.internal.servlet5;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

/**
 * Mock implementation of <code>ServletInputStream</code>.
 */
public class MockServletInputStream extends ServletInputStream {
    private final ByteArrayInputStream stream;

    public MockServletInputStream(byte[] data) {
        stream = new ByteArrayInputStream(data);
    }

    public int read() throws IOException {
        return stream.read();
    }

    @Override
    public boolean isFinished() {
        return stream.available() == 0;
    }

    @Override
    public boolean isReady() {
        return stream.available() > 0;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        throw new UnsupportedOperationException();
    }

}
