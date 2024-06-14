package io.github.cdiunit.internal;

import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.Metadata;

import javax.enterprise.inject.spi.Extension;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.github.cdiunit.internal.WeldComponentFactory.createMetadata;

class DefaultDiscoveryContext implements DiscoveryExtension.Context {

	private final ClasspathScanner scanner;

	private final BeansXml beansXml;

	private final TestConfiguration testConfiguration;

	private final Collection<Metadata<Extension>> extensions = new ArrayList<>();

	private final Set<Class<?>> classesToProcess = new LinkedHashSet<>();

	private final Set<Class<?>> classesToIgnore = new LinkedHashSet<>();

	private final Set<String> alternatives = new LinkedHashSet<>();

	public DefaultDiscoveryContext(ClasspathScanner scanner, final BeansXml beansXml, final TestConfiguration testConfiguration) {
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
			final URL url = scanner.getClasspathURL(baseClass);

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
			.map(scanner::getClasspathURL)
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
