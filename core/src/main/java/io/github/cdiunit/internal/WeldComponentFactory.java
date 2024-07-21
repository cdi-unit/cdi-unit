package io.github.cdiunit.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import jakarta.enterprise.inject.spi.Extension;

import org.jboss.weld.environment.se.Weld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class WeldComponentFactory {

    private static final Logger log = LoggerFactory.getLogger(WeldComponentFactory.class);

    private WeldComponentFactory() {
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

        final ClasspathScanner scanner = new CachingClassGraphScanner(new DefaultBeanArchiveScanner());
        final DefaultDiscoveryContext discoveryContext = new DefaultDiscoveryContext(scanner, testConfiguration);

        final Set<Class<?>> discoveredClasses = new LinkedHashSet<>();
        final Set<Class<?>> classesProcessed = new HashSet<>();

        discoverExtension.accept(discoveryContext);

        discoveryContext.processBean(testConfiguration.getTestClass());

        while (discoveryContext.hasClassesToProcess()) {
            final Class<?> cls = discoveryContext.nextClassToProcess();

            final boolean candidate = scanner.isContainedInBeanArchive(cls) || Extension.class.isAssignableFrom(cls);
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

        var weld = new Weld("cdi-unit-" + UUID.randomUUID())
                .disableDiscovery();

        discoveryContext.configure(weld);

        for (var clazz : discoveredClasses) {
            weld.addBeanClass(clazz);
        }

        log.debug("CDI-Unit discovered:");
        for (var clazz : discoveredClasses) {
            var clsName = clazz.getName();
            if (clsName.startsWith("io.github.cdiunit.internal.")) {
                log.trace(clsName);
            } else {
                log.debug(clsName);
            }
        }
        return weld;
    }

}
