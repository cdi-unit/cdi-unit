/*
 * Copyright 2024 the original author or authors.
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.*;

import org.jboss.weld.environment.se.Weld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.cdiunit.core.beanarchive.BeanArchiveClosure;
import io.github.cdiunit.core.beanarchive.BeanArchiveClosures;
import io.github.cdiunit.core.classpath.ClassContributorLookup;
import io.github.cdiunit.core.classpath.ClasspathScanner;
import io.github.cdiunit.core.classpath.ClasspathScanners;

public final class WeldHelper {

    private static final Logger log = LoggerFactory.getLogger(WeldHelper.class);

    private WeldHelper() {
    }

    public static Weld configureWeld(TestConfiguration testConfiguration) {
        final DefaultBootstrapDiscoveryContext bdc = new DefaultBootstrapDiscoveryContext();

        final ServiceLoader<DiscoveryExtension> discoveryExtensions = ServiceLoader.load(DiscoveryExtension.class);
        discoveryExtensions.forEach(extension -> extension.bootstrap(bdc));

        // Capture values to ignore potential updates after the bootstrap
        final Consumer<DiscoveryExtension.Context> discoverExtension = bdc.discoverExtension;
        final BiConsumer<DiscoveryExtension.Context, Class<?>> discoverClass = bdc.discoverClass;
        final BiConsumer<DiscoveryExtension.Context, Field> discoverField = bdc.discoverField;
        final BiConsumer<DiscoveryExtension.Context, Method> discoverMethod = bdc.discoverMethod;
        final Consumer<DiscoveryExtension.Context> afterDiscovery = bdc.afterDiscovery;

        final ClasspathScanner scanner = ClasspathScanners.caching();
        final BeanArchiveClosure beanArchiveClosure = BeanArchiveClosures.ofManifestClassPath();
        beanArchiveClosure.resolve(scanner.getClassContributors());

        final DefaultDiscoveryContext discoveryContext = new DefaultDiscoveryContext(scanner, testConfiguration);

        final Set<Class<?>> discoveredClasses = new LinkedHashSet<>();
        final Set<Class<?>> classesProcessed = new HashSet<>();

        discoverExtension.accept(discoveryContext);

        discoveryContext.processBean(testConfiguration.getTestClass());
        testConfiguration.getAdditionalClasses().forEach(discoveryContext::processBean);

        final Predicate<Class<?>> isInTestClassContributor = c -> Objects.equals(
                ClassContributorLookup.getInstance().lookup(c),
                ClassContributorLookup.getInstance().lookup(testConfiguration.getTestClass()));

        while (discoveryContext.hasClassesToProcess()) {
            final Class<?> cls = discoveryContext.nextClassToProcess();

            final boolean candidate = beanArchiveClosure.isContainedInBeanArchive(cls)
                    || isInTestClassContributor.test(cls);
            final boolean processed = classesProcessed.contains(cls);
            final boolean primitive = cls.isPrimitive();
            final boolean ignored = discoveryContext.isIgnored(cls);

            if (candidate && !processed && !primitive && !ignored) {
                classesProcessed.add(cls);
                if (!cls.isAnnotation()) {
                    discoveredClasses.add(cls);
                }

                try {
                    discoverClass.accept(discoveryContext, cls);

                    for (Field field : cls.getDeclaredFields()) {
                        discoverField.accept(discoveryContext, field);
                    }
                    for (Method method : cls.getDeclaredMethods()) {
                        discoverMethod.accept(discoveryContext, method);
                    }
                } catch (NoClassDefFoundError ncdf) {
                    throw new IllegalStateException(String.format("Can not discover %s", cls), ncdf);
                }
            }

            discoveryContext.processed(cls);
        }

        afterDiscovery.accept(discoveryContext);

        var weld = new Weld("cdi-unit-" + UUID.randomUUID())
                .disableDiscovery();

        discoveryContext.configure(weld);

        for (var clazz : discoveredClasses) {
            weld.addBeanClass(clazz);
        }

        log.info("CDI-Unit discovered:");
        for (var clazz : discoveredClasses) {
            var clsName = clazz.getName();
            if (quietDiscovery(clazz)) {
                log.debug(clsName);
            } else {
                log.info(clsName);
            }
        }
        return weld;
    }

    static boolean quietDiscovery(Class<?> c) {
        if (c.isAnnotationPresent(QuietDiscovery.class)) {
            return true;
        }

        var p = c.getPackage();
        if (p.isAnnotationPresent(QuietDiscovery.class)) {
            return true;
        }

        var pName = p.getName();
        return pName.startsWith("io.github.cdiunit") && pName.contains(".internal");
    }

}
