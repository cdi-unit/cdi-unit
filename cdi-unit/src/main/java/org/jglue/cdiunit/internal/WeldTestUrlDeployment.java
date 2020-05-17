/*
 *    Copyright 2011 Bryn Cooke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jglue.cdiunit.internal;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.spi.*;
import org.jboss.weld.environment.se.WeldSEBeanRegistrant;
import org.jboss.weld.metadata.BeansXmlImpl;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jglue.cdiunit.ProducesAlternative;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.Extension;

import java.io.IOException;
import java.lang.reflect.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class WeldTestUrlDeployment implements Deployment {
	private final BeanDeploymentArchive beanDeploymentArchive;
	private final ClasspathScanner scanner = new CachingClassGraphScanner(new DefaultBeanArchiveScanner());
	private final Collection<Metadata<Extension>> extensions = new ArrayList<>();
	private static final Logger log = LoggerFactory.getLogger(WeldTestUrlDeployment.class);
	private final Set<URL> cdiClasspathEntries = new HashSet<>();
	private final ServiceRegistry serviceRegistry = new SimpleServiceRegistry();

	public WeldTestUrlDeployment(ResourceLoader resourceLoader, Bootstrap bootstrap, TestConfiguration testConfiguration) throws IOException {
		cdiClasspathEntries.addAll(scanner.getBeanArchives());

		final DefaultBootstrapDiscoveryContext bdc = new DefaultBootstrapDiscoveryContext();

		final ServiceLoader<DiscoveryExtension> discoveryExtensions = ServiceLoader.load(DiscoveryExtension.class);
		discoveryExtensions.forEach(extension -> extension.bootstrap(bdc));

		// Capture values to ignore potential updates after the bootstrap
		final Consumer<DiscoveryExtension.Context> discoverExtension = bdc.discoverExtension;
		final BiConsumer<DiscoveryExtension.Context, Class<?>> discoverClass = bdc.discoverClass;
		final BiConsumer<DiscoveryExtension.Context, Field> discoverField = bdc.discoverField;
		final BiConsumer<DiscoveryExtension.Context, Method> discoverMethod = bdc.discoverMethod;

		final BeansXml beansXml = createBeansXml();

		final Context discoveryContext = new Context(scanner, beansXml, testConfiguration);

		final Set<String> discoveredClasses = new LinkedHashSet<>();
		final Set<Class<?>> classesProcessed = new HashSet<>();

		discoverExtension.accept(discoveryContext);

		discoveryContext.processBean(testConfiguration.getTestClass());

		while (discoveryContext.hasClassesToProcess()) {
			final Class<?> cls = discoveryContext.nextClassToProcess();

			if ((isCdiClass(cls) || Extension.class.isAssignableFrom(cls)) && !classesProcessed.contains(cls) && !cls.isPrimitive()
				&& !discoveryContext.isIgnored(cls)) {
				classesProcessed.add(cls);
				if (!cls.isAnnotation()) {
					discoveredClasses.add(cls.getName());
				}

				discoverClass.accept(discoveryContext, cls);

				for (Field field : cls.getDeclaredFields()) {
					discoverField.accept(discoveryContext, field);
				}
				for (Method method : cls.getDeclaredMethods()) {
					discoverMethod.accept(discoveryContext, method);
				}
			}

			discoveryContext.processed(cls);
		}

		beansXml.getEnabledAlternativeStereotypes().add(
			createMetadata(ProducesAlternative.class.getName(), ProducesAlternative.class.getName()));

		for (String alternative : discoveryContext.getAlternatives()) {
			beansXml.getEnabledAlternativeClasses().add(createMetadata(alternative, alternative));
		}

		extensions.add(createMetadata(new WeldSEBeanRegistrant(), WeldSEBeanRegistrant.class.getName()));

		extensions.addAll(discoveryContext.getExtensions());

		beanDeploymentArchive = new BeanDeploymentArchiveImpl("cdi-unit" + UUID.randomUUID(), discoveredClasses, beansXml);
		beanDeploymentArchive.getServices().add(ResourceLoader.class, resourceLoader);
		log.debug("CDI-Unit discovered:");
		for (String clazz : discoveredClasses) {
			if (!clazz.startsWith("org.jglue.cdiunit.internal.")) {
				log.debug(clazz);
			}
		}

	}

	private static <T> Metadata<T> createMetadata(T value, String location) {
		try {
			return new org.jboss.weld.bootstrap.spi.helpers.MetadataImpl<>(value, location);
		} catch (NoClassDefFoundError e) {
			// MetadataImpl moved to a new package in Weld 2.4, old copy removed in 3.0
			try {
				// If Weld < 2.4, the new package isn't there, so we try the old package.
				//noinspection unchecked
				Class<Metadata<T>> oldClass = (Class<Metadata<T>>) ClassLookup.INSTANCE.lookup("org.jboss.weld.metadata.MetadataImpl");
				Constructor<Metadata<T>> ctor = oldClass.getConstructor(Object.class, String.class);
				return ctor.newInstance(value, location);
			} catch (ReflectiveOperationException e1) {
				throw new RuntimeException(e1);
			}
		}
	}

	private static Object annotatedDiscoveryMode() {
		try {
			return BeanDiscoveryMode.ANNOTATED;
		} catch (NoClassDefFoundError e) {
			// No such enum in Weld 1.x, but the constructor for BeansXmlImpl has fewer parameters so we don't need it
			return null;
		}
	}

	private static BeansXml createBeansXml() {
		try {
			// The constructor for BeansXmlImpl has added more parameters in newer Weld versions. The parameter list
			// is truncated in older version of Weld where the number of parameters is shorter, thus omitting the
			// newer parameters.
			Object[] initArgs = new Object[]{
				new ArrayList<Metadata<String>>(), new ArrayList<Metadata<String>>(),
				new ArrayList<Metadata<String>>(), new ArrayList<Metadata<String>>(), Scanning.EMPTY_SCANNING,
				// These were added in Weld 2.0:
				new URL("file:cdi-unit"), annotatedDiscoveryMode(), "cdi-unit",
				// isTrimmed: added in Weld 2.4.2 [WELD-2314]:
				false
			};
			Constructor<?> beansXmlConstructor = BeansXmlImpl.class.getConstructors()[0];
			return (BeansXml) beansXmlConstructor.newInstance(
				Arrays.copyOfRange(initArgs, 0, beansXmlConstructor.getParameterCount()));
		} catch (MalformedURLException | ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	private static URL getClasspathURL(Class<?> clazz) {
		CodeSource codeSource = clazz.getProtectionDomain()
			.getCodeSource();
		return codeSource != null ? codeSource.getLocation() : null;
	}

	private boolean isCdiClass(Class<?> c) {
		URL location = getClasspathURL(c);
		return location != null && cdiClasspathEntries.contains(location);
	}

	@Override
	public Iterable<Metadata<Extension>> getExtensions() {
		return extensions;
	}

	public List<BeanDeploymentArchive> getBeanDeploymentArchives() {
		return Collections.singletonList(beanDeploymentArchive);
	}

	public BeanDeploymentArchive loadBeanDeploymentArchive(Class<?> beanClass) {
		return beanDeploymentArchive;
	}

	public BeanDeploymentArchive getBeanDeploymentArchive(Class<?> beanClass) {
		return beanDeploymentArchive;
	}

	@Override
	public ServiceRegistry getServices() {
		return serviceRegistry;
	}

	private static class Context implements DiscoveryExtension.Context {

		private final ClasspathScanner scanner;

		private final BeansXml beansXml;

		private final TestConfiguration testConfiguration;

		private final Collection<Metadata<Extension>> extensions = new ArrayList<>();

		private final Set<Class<?>> classesToProcess = new LinkedHashSet<>();

		private final Set<Class<?>> classesToIgnore = new LinkedHashSet<>();

		private final Set<String> alternatives = new LinkedHashSet<>();

		public Context(ClasspathScanner scanner, final BeansXml beansXml, final TestConfiguration testConfiguration) {
			this.scanner = scanner;
			this.beansXml = beansXml;
			this.testConfiguration = testConfiguration;
		}

		@Override
		public TestConfiguration getTestConfiguration() {
			return testConfiguration;
		}

		public boolean hasClassesToProcess() {
			return !classesToProcess.isEmpty();
		}

		public Class<?> nextClassToProcess() {
			return classesToProcess.iterator().next();
		}

		public void processed(Class<?> c) {
			classesToProcess.remove(c);
		}

		private void process(Type type, Consumer<Class<?>> onClass) {
			if (type instanceof Class) {
				onClass.accept((Class<?>) type);
			}

			if (type instanceof ParameterizedType) {
				ParameterizedType ptype = (ParameterizedType) type;
				onClass.accept((Class<?>) ptype.getRawType());
				for (Type arg : ptype.getActualTypeArguments()) {
					process(arg, onClass);
				}
			}
		}

		@Override
		public void processBean(String className) {
			processBean(ClassLookup.INSTANCE.lookup(className));
		}

		@Override
		public void processBean(Type type) {
			process(type, classesToProcess::add);
		}

		@Override
		public void ignoreBean(String className) {
			ignoreBean(ClassLookup.INSTANCE.lookup(className));
		}

		@Override
		public void ignoreBean(Type type) {
			process(type, classesToIgnore::add);
		}

		public boolean isIgnored(Class<?> c) {
			return classesToIgnore.contains(c);
		}

		@Override
		public void enableAlternative(String className) {
			alternatives.add(className);
		}

		@Override
		public void enableAlternative(Class<?> alternativeClass) {
			enableAlternative(alternativeClass.getName());
		}

		public Collection<String> getAlternatives() {
			return alternatives;
		}

		@Override
		public void enableDecorator(String className) {
			beansXml.getEnabledDecorators().add(createMetadata(className, className));
		}

		@Override
		public void enableDecorator(Class<?> decoratorClass) {
			enableDecorator(decoratorClass.getName());
		}

		@Override
		public void enableInterceptor(String className) {
			beansXml.getEnabledInterceptors().add(createMetadata(className, className));
		}

		@Override
		public void enableInterceptor(Class<?> interceptorClass) {
			enableInterceptor(interceptorClass.getName());
		}

		@Override
		public void enableAlternativeStereotype(String className) {
			beansXml.getEnabledAlternativeStereotypes().add(createMetadata(className, className));
		}

		@Override
		public void enableAlternativeStereotype(Class<?> alternativeStereotypeClass) {
			enableAlternativeStereotype(alternativeStereotypeClass.getName());
		}

		@Override
		public void extension(Extension extension, String location) {
			extensions.add(createMetadata(extension, location));
		}

		public Collection<Metadata<Extension>> getExtensions() {
			return extensions;
		}

		@Override
		public Collection<Class<?>> scanPackages(Collection<Class<?>> baseClasses) {
			final Collection<Class<?>> result = new LinkedHashSet<>();
			for (Class<?> baseClass : baseClasses) {
				final String packageName = baseClass.getPackage().getName();
				final URL url = getClasspathURL(baseClass);

				// It might be more efficient to scan all packageNames at once, but we
				// might pick up classes from a different package's classpath entry, which
				// would be a change in behaviour (but perhaps less surprising?).
				scanner.getClassNamesForPackage(packageName, url)
					.stream()
					.map(this::loadClass)
					.collect(Collectors.toCollection(() -> result));
			}
			return result;
		}

		@Override
		public Collection<Class<?>> scanBeanArchives(Collection<Class<?>> baseClasses) {
			URL[] urls = baseClasses.stream()
				.map(WeldTestUrlDeployment::getClasspathURL)
				.toArray(URL[]::new);
			return scanner.getClassNamesForClasspath(urls)
				.stream()
				.map(this::loadClass)
				.collect(Collectors.toSet());
		}

		private Class<?> loadClass(String name) {
			try {
				return getClass().getClassLoader().loadClass(name);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}

	}

}
