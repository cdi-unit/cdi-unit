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
package io.github.cdiunit.core.classpath;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

class ClassGraphScanner implements ClasspathScanner {

    ClassGraphScanner() {
    }

    @Override
    public Iterable<ClassContributor> getClassContributors() {
        try (ScanResult scan = new ClassGraph()
                .disableNestedJarScanning()
                .disableModuleScanning()
                .scan()) {
            return scan.getClasspathURIs().stream()
                    .distinct()
                    .map(ClassContributor::of)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<String> getClassNamesForClasspath(final Iterable<ClassContributor> classContributors) {
        final var scanner = new ClassGraph()
                .disableNestedJarScanning()
                .enableClassInfo()
                .ignoreClassVisibility();
        StreamSupport.stream(classContributors.spliterator(), false)
                .map(ClassContributor::getURI)
                .forEachOrdered(scanner::overrideClasspath);
        try (ScanResult scan = scanner.scan()) {
            return scan.getAllClasses().getNames();
        }
    }

    @Override
    public List<String> getClassNamesForPackage(String packageName, ClassContributor classContributor) {
        try (ScanResult scan = new ClassGraph()
                .disableNestedJarScanning()
                .enableClassInfo()
                .ignoreClassVisibility()
                .overrideClasspath(classContributor.getURI())
                .acceptPackagesNonRecursive(packageName)
                .scan()) {
            return scan.getAllClasses().getNames();
        }
    }
}
