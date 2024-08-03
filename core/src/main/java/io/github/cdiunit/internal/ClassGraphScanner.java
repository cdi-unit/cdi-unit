/*
 * Copyright 2018 the original author or authors.
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
package io.github.cdiunit.internal;

import java.net.URL;
import java.util.Collection;
import java.util.List;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

class ClassGraphScanner implements ClasspathScanner {

    private final BeanArchiveScanner beanArchiveScanner;

    ClassGraphScanner(final BeanArchiveScanner beanArchiveScanner) {
        this.beanArchiveScanner = beanArchiveScanner;
    }

    private List<URL> getClasspathURLs() {
        try (ScanResult scan = new ClassGraph()
                .disableNestedJarScanning()
                .scan()) {
            return scan.getClasspathURLs();
        }
    }

    @Override
    public Collection<URL> getBeanArchives() {
        final List<URL> urls = getClasspathURLs();
        try {
            return beanArchiveScanner.findBeanArchives(urls);
        } catch (Exception e) {
            throw ExceptionUtils.asRuntimeException(e);
        }
    }

    @Override
    public List<String> getClassNamesForClasspath(URL[] urls) {
        try (ScanResult scan = new ClassGraph()
                .disableNestedJarScanning()
                .enableClassInfo()
                .ignoreClassVisibility()
                .overrideClasspath(urls)
                .scan()) {
            return scan.getAllClasses().getNames();
        }
    }

    @Override
    public List<String> getClassNamesForPackage(String packageName, URL url) {
        try (ScanResult scan = new ClassGraph()
                .disableNestedJarScanning()
                .enableClassInfo()
                .ignoreClassVisibility()
                .overrideClasspath(url)
                .acceptPackagesNonRecursive(packageName)
                .scan()) {
            return scan.getAllClasses().getNames();
        }
    }
}
