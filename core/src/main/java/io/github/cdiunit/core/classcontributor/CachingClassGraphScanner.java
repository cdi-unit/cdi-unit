/*
 * Copyright 2020 the original author or authors.
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
package io.github.cdiunit.core.classcontributor;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import io.github.cdiunit.core.beanarchive.BeanArchiveScanner;
import io.github.cdiunit.internal.ExceptionUtils;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

class CachingClassGraphScanner implements ClasspathScanner {

    /**
     * The default number of worker threads to use while scanning. This number gave the best results on a relatively
     * modern laptop with SSD, while scanning a large classpath.
     */
    static final int DEFAULT_NUM_WORKER_THREADS = Math.max(
            // Always scan with at least 2 threads
            2, //
            (int) Math.ceil(
                    // Num IO threads (top out at 4, since most I/O devices won't scale better than this)
                    Math.min(4.0f, Runtime.getRuntime().availableProcessors() * 0.75f)
                            // Num scanning threads (higher than available processors, because some threads can be blocked)
                            + // Num scanning threads (higher than available processors, because some threads can be blocked)
                            Runtime.getRuntime().availableProcessors() * 1.25f) //
    );

    static final ExecutorService scanExecutor = Executors.newWorkStealingPool(DEFAULT_NUM_WORKER_THREADS);

    static ConcurrentHashMap<Object, Object> cache = new ConcurrentHashMap<>();

    private final BeanArchiveScanner beanArchiveScanner;

    public CachingClassGraphScanner(final BeanArchiveScanner beanArchiveScanner) {
        this.beanArchiveScanner = beanArchiveScanner;
    }

    @SuppressWarnings("unchecked")
    private <K, V> V computeIfAbsent(final K k, final Supplier<V> computeValue) {
        return (V) cache.computeIfAbsent(k, o -> computeValue.get());
    }

    @Override
    public Iterable<ClassContributor> getClassContributors() {
        return computeIfAbsent(getClass().getClassLoader(), this::computeClassContributors);
    }

    @Override
    public Collection<ClassContributor> getBeanArchives() {
        final Iterable<ClassContributor> classContributors = getClassContributors();
        return computeIfAbsent(computeKey(classContributors), () -> findBeanArchives(classContributors));
    }

    private Collection<ClassContributor> findBeanArchives(final Iterable<ClassContributor> classContributors) {
        try {
            return beanArchiveScanner.findBeanArchives(classContributors);
        } catch (Exception e) {
            throw ExceptionUtils.asRuntimeException(e);
        }
    }

    private Iterable<ClassContributor> computeClassContributors() {
        try (ScanResult scan = new ClassGraph()
                .disableNestedJarScanning()
                .disableModuleScanning()
                .scan(scanExecutor, DEFAULT_NUM_WORKER_THREADS)) {
            return scan.getClasspathURIs().stream()
                    .distinct()
                    .map(ClassContributor::of)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<String> getClassNamesForClasspath(final Iterable<ClassContributor> classContributors) {
        return computeIfAbsent(computeKey(classContributors), () -> this.computeClassNamesForClasspath(classContributors));
    }

    private Object computeKey(final Iterable<ClassContributor> classContributors) {
        return StreamSupport.stream(classContributors.spliterator(), false)
                .map(ClassContributor::getURI)
                .map(Objects::toString)
                .collect(Collectors.joining(File.pathSeparator));
    }

    private List<String> computeClassNamesForClasspath(final Iterable<ClassContributor> classContributors) {
        final var scanner = new ClassGraph()
                .disableNestedJarScanning()
                .enableClassInfo()
                .ignoreClassVisibility();
        StreamSupport.stream(classContributors.spliterator(), false)
                .map(ClassContributor::getURI)
                .forEachOrdered(scanner::overrideClasspath);
        try (ScanResult scan = scanner.scan(scanExecutor, DEFAULT_NUM_WORKER_THREADS)) {
            return scan.getAllClasses().getNames();
        }
    }

    @Override
    public List<String> getClassNamesForPackage(final String packageName, final ClassContributor classContributor) {
        return computeIfAbsent(computeKey(packageName, classContributor),
                () -> this.computeClassNamesForPackage(packageName, classContributor));
    }

    private Object computeKey(final String packageName, final ClassContributor classContributor) {
        return String.format("%s@%s", packageName, classContributor);
    }

    private List<String> computeClassNamesForPackage(final String packageName, final ClassContributor classContributor) {
        try (ScanResult scan = new ClassGraph()
                .disableNestedJarScanning()
                .enableClassInfo()
                .ignoreClassVisibility()
                .overrideClasspath(classContributor.getURI())
                .acceptPackagesNonRecursive(packageName)
                .scan(scanExecutor, DEFAULT_NUM_WORKER_THREADS)) {
            return scan.getAllClasses().getNames();
        }
    }

}
