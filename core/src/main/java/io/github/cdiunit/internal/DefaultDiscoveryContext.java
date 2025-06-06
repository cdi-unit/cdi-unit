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
package io.github.cdiunit.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import jakarta.enterprise.inject.spi.Extension;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldSEBeanRegistrant;

import io.github.cdiunit.ProducesAlternative;
import io.github.cdiunit.core.classpath.ClassContributor;
import io.github.cdiunit.core.classpath.ClassContributorLookup;
import io.github.cdiunit.core.classpath.ClassLookup;
import io.github.cdiunit.core.classpath.ClasspathScanner;
import io.github.cdiunit.core.context.internal.ContextActivator;
import io.github.cdiunit.core.context.internal.ContextTracker;

class DefaultDiscoveryContext implements DiscoveryExtension.Context {

    private final ClasspathScanner scanner;

    private final TestConfiguration testConfiguration;

    private final Set<Extension> extensions = new LinkedHashSet<>();

    private final Set<Class<?>> classesToProcess = new LinkedHashSet<>();

    private final Set<Class<?>> classesToIgnore = new LinkedHashSet<>();

    private final Set<Class<?>> alternatives = new LinkedHashSet<>();

    private final Set<Class<?>> decorators = new LinkedHashSet<>();

    private final Set<Class<?>> interceptors = new LinkedHashSet<>();

    private final Set<Class<? extends Annotation>> alternativeStereotypes = new LinkedHashSet<>();

    private final Set<Class<? extends Annotation>> scopeTypes = new LinkedHashSet<>();

    public DefaultDiscoveryContext(ClasspathScanner scanner, final TestConfiguration testConfiguration) {
        this.scanner = scanner;
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
        processBean(loadClass(className));
    }

    @Override
    public void processBean(Type type) {
        process(type, classesToProcess::add);
    }

    @Override
    public void ignoreBean(String className) {
        ignoreBean(loadClass(className));
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
        alternatives.add(loadClass(className));
    }

    @Override
    public void enableAlternative(Class<?> alternativeClass) {
        alternatives.add(alternativeClass);
    }

    public Collection<Class<?>> getAlternatives() {
        return alternatives;
    }

    @Override
    public void enableDecorator(String className) {
        decorators.add(loadClass(className));
    }

    @Override
    public void enableDecorator(Class<?> decoratorClass) {
        decorators.add(decoratorClass);
    }

    public Collection<Class<?>> getDecorators() {
        return decorators;
    }

    @Override
    public void enableInterceptor(String className) {
        interceptors.add(loadClass(className));
    }

    @Override
    public void enableInterceptor(Class<?> interceptorClass) {
        interceptors.add(interceptorClass);
    }

    public Collection<Class<?>> getInterceptors() {
        return interceptors;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void enableAlternativeStereotype(String className) {
        alternativeStereotypes.add((Class<? extends Annotation>) loadClass(className));
    }

    @Override
    public void enableAlternativeStereotype(Class<? extends Annotation> alternativeStereotypeClass) {
        alternativeStereotypes.add(alternativeStereotypeClass);
    }

    public Collection<Class<? extends Annotation>> getAlternativeStereotypes() {
        return alternativeStereotypes;
    }

    @Override
    public void extension(Extension extension) {
        extensions.add(extension);
    }

    public Collection<Extension> getExtensions() {
        return extensions;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void scope(String additionalScope) {
        scope((Class<? extends Annotation>) loadClass(additionalScope));
    }

    @Override
    public void scope(Class<? extends Annotation> additionalScope) {
        scopeTypes.add(additionalScope);
    }

    public Collection<Class<? extends Annotation>> getScopes() {
        return scopeTypes;
    }

    @Override
    public Collection<Class<?>> scanPackages(Collection<Class<?>> baseClasses) {
        final Collection<Class<?>> result = new LinkedHashSet<>();
        for (Class<?> baseClass : baseClasses) {
            final String packageName = baseClass.getPackage().getName();
            final ClassContributor contributor = ClassContributorLookup.getInstance().lookup(baseClass);
            if (contributor == null) {
                continue;
            }

            // It might be more efficient to scan all packageNames at once, but we
            // might pick up classes from a different package's classpath entry, which
            // would be a change in behaviour (but perhaps less surprising?).
            scanner.getClassNamesForPackage(packageName, contributor)
                    .stream()
                    .map(this::loadClass)
                    .collect(Collectors.toCollection(() -> result));
        }
        return result;
    }

    @Override
    public Collection<Class<?>> scanBeanArchives(Collection<Class<?>> baseClasses) {
        final List<ClassContributor> classContributors = baseClasses.stream()
                .map(ClassContributorLookup.getInstance()::lookup)
                .collect(Collectors.toList());
        return scanner.getClassNamesForClasspath(classContributors)
                .stream()
                .map(this::loadClass)
                .collect(Collectors.toSet());
    }

    private Class<?> loadClass(String name) {
        final Class<?> result = ClassLookup.getInstance().lookup(name);
        if (result == null) {
            throw ExceptionUtils.asRuntimeException(new ClassNotFoundException(String.format("Class %s not found", name)));
        }
        return result;
    }

    void configure(Weld weld) {
        weld.addExtension(new WeldSEBeanRegistrant());
        weld.addAlternativeStereotype(ProducesAlternative.class);

        weld.addBeanClass(testConfiguration.getTestClass());

        extensions.forEach(weld::addExtension);
        alternatives.forEach(weld::addAlternative);
        alternativeStereotypes.forEach(weld::addAlternativeStereotype);
        decorators.forEach(weld::addDecorator);
        interceptors.forEach(weld::addInterceptor);

        weld.addExtension(new ContextActivator(scopeTypes));
        weld.addExtension(new ContextTracker(scopeTypes));
    }

}
