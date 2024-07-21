package io.github.cdiunit.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import jakarta.enterprise.inject.spi.Extension;

import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.bootstrap.spi.Scanning;
import org.jboss.weld.bootstrap.spi.helpers.MetadataImpl;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldSEBeanRegistrant;
import org.jboss.weld.metadata.BeansXmlImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.cdiunit.ProducesAlternative;

public final class WeldComponentFactory {

    private static final Logger log = LoggerFactory.getLogger(WeldComponentFactory.class);

    private WeldComponentFactory() {
    }

    static <T> Metadata<T> createMetadata(T value, String location) {
        return new MetadataImpl<>(value, location);
    }

    static BeansXml createBeansXml() {
        try {
            // The constructor for BeansXmlImpl has added more parameters in newer Weld versions. The parameter list
            // is truncated in older version of Weld where the number of parameters is shorter, thus omitting the
            // newer parameters.
            Object[] initArgs = new Object[] {
                    new ArrayList<Metadata<String>>(), new ArrayList<Metadata<String>>(),
                    new ArrayList<Metadata<String>>(), new ArrayList<Metadata<String>>(), Scanning.EMPTY_SCANNING,
                    // These were added in Weld 2.0:
                    new URL("file:cdi-unit"), BeanDiscoveryMode.ANNOTATED, "cdi-unit",
                    // isTrimmed: added in Weld 2.4.2 [WELD-2314]:
                    false
            };
            Constructor<?> beansXmlConstructor = BeansXmlImpl.class.getConstructors()[0];
            return (BeansXml) beansXmlConstructor.newInstance(
                    Arrays.copyOfRange(initArgs, 0, beansXmlConstructor.getParameterCount()));
        } catch (MalformedURLException | ReflectiveOperationException e) {
            throw ExceptionUtils.asRuntimeException(e);
        }
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

        final BeansXml beansXml = WeldComponentFactory.createBeansXml();

        final ClasspathScanner scanner = new CachingClassGraphScanner(new DefaultBeanArchiveScanner());
        final DefaultDiscoveryContext discoveryContext = new DefaultDiscoveryContext(scanner, beansXml, testConfiguration);

        final Set<String> discoveredClasses = new LinkedHashSet<>();
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
                    discoveredClasses.add(cls.getName());
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
        weld.addBeanClass(testConfiguration.getTestClass());

        weld.addExtensions(WeldSEBeanRegistrant.class);
        for (var extensionMetadata : discoveryContext.getExtensions()) {
            weld.addExtension(extensionMetadata.getValue());
        }

        for (String alternative : discoveryContext.getAlternatives()) {
            Class<?> mClass = ClassLookup.INSTANCE.lookup(alternative);
            weld.addAlternative(mClass);
        }

        for (var metadata : beansXml.getEnabledAlternativeClasses()) {
            Class<?> mClass = ClassLookup.INSTANCE.lookup(metadata.getValue());
            weld.addAlternative(mClass);
        }

        weld.addAlternativeStereotype(ProducesAlternative.class);
        for (var metadata : beansXml.getEnabledAlternativeStereotypes()) {
            Class<? extends Annotation> mClass = ClassLookup.INSTANCE.lookup(metadata.getValue());
            weld.addAlternativeStereotype(mClass);
        }

        for (var metadata : beansXml.getEnabledDecorators()) {
            Class<?> mClass = ClassLookup.INSTANCE.lookup(metadata.getValue());
            weld.addDecorator(mClass);
        }

        for (var metadata : beansXml.getEnabledInterceptors()) {
            Class<?> mClass = ClassLookup.INSTANCE.lookup(metadata.getValue());
            weld.addInterceptor(mClass);
        }

        for (var className : discoveredClasses) {
            Class<?> mClass = ClassLookup.INSTANCE.lookup(className);
            weld.addBeanClass(mClass);
        }

        log.debug("CDI-Unit discovered:");
        for (String clazz : discoveredClasses) {
            if (clazz.startsWith("io.github.cdiunit.internal.")) {
                log.trace(clazz);
            } else {
                log.debug(clazz);
            }
        }
        return weld;
    }
}
