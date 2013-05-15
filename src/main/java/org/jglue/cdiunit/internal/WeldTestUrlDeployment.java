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

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Stereotype;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.interceptor.Interceptor;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.bootstrap.spi.Scanning;
import org.jboss.weld.environment.se.WeldSEBeanRegistrant;
import org.jboss.weld.environment.se.discovery.AbstractWeldSEDeployment;
import org.jboss.weld.environment.se.discovery.ImmutableBeanDeploymentArchive;
import org.jboss.weld.metadata.BeansXmlImpl;
import org.jboss.weld.metadata.MetadataImpl;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jglue.cdiunit.ActivatedAlternatives;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.ProducesAlternative;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeldTestUrlDeployment extends AbstractWeldSEDeployment {
    private final BeanDeploymentArchive _beanDeploymentArchive;
    private Collection<Metadata<Extension>> _extensions = new ArrayList<Metadata<Extension>>();
    private static Logger log = LoggerFactory.getLogger(WeldTestUrlDeployment.class);
    private Set<URL> cdiClasspathEntries = new HashSet<URL>();

    public WeldTestUrlDeployment(ResourceLoader resourceLoader, Bootstrap bootstrap, Class<?> testClass) throws IOException {
        super(bootstrap);

        populateCdiClasspathSet();
        // BeanDeploymentArchive archive = new URLScanner(resourceLoader,
        // bootstrap, RESOURCES).scan();
        BeansXml beansXml = new BeansXmlImpl(new ArrayList<Metadata<String>>(), new ArrayList<Metadata<String>>(), new ArrayList<Metadata<String>>(),
                new ArrayList<Metadata<String>>(), Scanning.EMPTY_SCANNING);
        Set<String> discoveredClasses = new LinkedHashSet<String>();
        Set<String> alternatives = new HashSet<String>();
        discoveredClasses.add(testClass.getName());
        Set<Class<?>> classesToProcess = new LinkedHashSet<Class<?>>();
        Set<Class<?>> classesProcessed = new HashSet<Class<?>>();

        classesToProcess.add(testClass);
        _extensions.add(new MetadataImpl<Extension>(new TestScopeExtension(testClass), TestScopeExtension.class.getName()));
        try {
            Class.forName("javax.servlet.http.HttpServletRequest");
            classesToProcess.add(InRequestInterceptor.class);
            classesToProcess.add(InSessionInterceptor.class);
            classesToProcess.add(InConversationInterceptor.class);
        } catch (ClassNotFoundException e) {
        }
        
        Set<Method> producesMethods = new HashSet<Method>();
        Reflections reflections = new Reflections("", new MethodAnnotationsScanner());
        producesMethods.addAll(reflections.getMethodsAnnotatedWith(Produces.class));

        while (!classesToProcess.isEmpty()) {

            Class<?> c = classesToProcess.iterator().next();

            if (isCdiClass(c) && !classesProcessed.contains(c) && !c.isPrimitive()) {
                classesProcessed.add(c);
                discoveredClasses.add(c.getName());
                if (Extension.class.isAssignableFrom(c)) {
                    try {
                        _extensions.add(new MetadataImpl<Extension>((Extension) c.newInstance(), c.getName()));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                if (c.isAnnotationPresent(Interceptor.class)) {
                    beansXml.getEnabledInterceptors().add(new MetadataImpl<String>(c.getName(), c.getName()));
                }

                if (isAlternativeStereotype(c)) {
                    beansXml.getEnabledAlternativeStereotypes().add(new MetadataImpl<String>(c.getName(), c.getName()));

                }

                AdditionalClasses additionalClasses = c.getAnnotation(AdditionalClasses.class);
                if (additionalClasses != null) {
                    for (Class<?> supportClass : additionalClasses.value()) {
                        classesToProcess.add(supportClass);
                    }
                    for (String lateBound : additionalClasses.late()) {
                        try {
                            Class<?> clazz = Class.forName(lateBound);
                            classesToProcess.add(clazz);
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }

                ActivatedAlternatives alternativeClasses = c.getAnnotation(ActivatedAlternatives.class);
                if (alternativeClasses != null) {
                    for (Class<?> alternativeClass : alternativeClasses.value()) {
                        classesToProcess.add(alternativeClass);

                        if (!isAlternativeStereotype(alternativeClass)) {
                            alternatives.add(alternativeClass.getName());
                        }
                    }
                }

                Class<?> superClass = c.getSuperclass();
                if (superClass != null && superClass != Object.class) {
                    classesToProcess.add(superClass);
                }

                for (Field field : c.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Inject.class)) {
                        Class<?> type = field.getType();
                        classesToProcess.add(type);
                    }
                    if (field.getType().equals(Provider.class)) {
                        ParameterizedType type = (ParameterizedType) field.getGenericType();
                        classesToProcess.add((Class<?>) type.getActualTypeArguments()[0]);
                    }
                }
                for (Method method : c.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(Inject.class)) {
                        for (Class<?> param : method.getParameterTypes()) {
                            classesToProcess.add(param);
                        }
                    }
                }
                
                // Add all producers that are in charge of building this class
                for (Method producesMethod : producesMethods) {
                	if (producesMethod.getReturnType().equals(c)) {
                		classesToProcess.add(producesMethod.getDeclaringClass());
                	}
                }
            }
            
            
            classesToProcess.remove(c);
        }

        beansXml.getEnabledAlternativeStereotypes().add(new MetadataImpl<String>(ProducesAlternative.class.getName(), ProducesAlternative.class.getName()));

        for (String alternative : alternatives) {
            beansXml.getEnabledAlternativeClasses().add(new MetadataImpl<String>(alternative, alternative));
        }

        try {
            Class.forName("org.mockito.Mock");
            _extensions.add(new MetadataImpl<Extension>(new MockExtension(), MockExtension.class.getName()));
        } catch (ClassNotFoundException e) {

        }

        _extensions.add(new MetadataImpl<Extension>(new WeldSEBeanRegistrant(), WeldSEBeanRegistrant.class.getName()));

        _beanDeploymentArchive = new ImmutableBeanDeploymentArchive("unitTest", discoveredClasses, beansXml);
        _beanDeploymentArchive.getServices().add(ResourceLoader.class, resourceLoader);
        log.debug("CDI-Unit discovered:");
        for (String clazz : discoveredClasses) {
            if (!clazz.startsWith("org.jglue.cdiunit.internal.")) {
                log.debug(clazz);
            }
        }

    }

    private void populateCdiClasspathSet() throws IOException {
        ClassLoader classLoader = WeldTestUrlDeployment.class.getClassLoader();
        List<URL> entries = new ArrayList<URL>(Arrays.asList(((URLClassLoader) classLoader).getURLs()));

        // If this is surefire we need to get the original claspath
        JarInputStream firstEntry = new JarInputStream(entries.get(0).openStream());
        Manifest manifest = firstEntry.getManifest();
        if (manifest != null) {
            String classpath = (String) manifest.getMainAttributes().get(Attributes.Name.CLASS_PATH);
            if (classpath != null) {
                String[] manifestEntries = classpath.split(" ?file:");
                for (String entry : manifestEntries) {
                    if (entry.length() > 0) {
                        entries.add(new URL("file:" + entry));
                    }
                }
            }

        }
        firstEntry.close();

        for (URL url : entries) {
            URLClassLoader cl = new URLClassLoader(new URL[] { url }, null);
            try {
                if (url.getFile().endsWith("/classes/")) {
                    URL webInfBeans = new URL(url, "../../src/main/webapp/WEB-INF/beans.xml");
                    try {
                        webInfBeans.openConnection();
                        cdiClasspathEntries.add(url);
                    } catch (IOException e) {

                    }
                }
                URL resource = cl.getResource("META-INF/beans.xml");

                boolean testClasses = url.getFile().endsWith("/test-classes/");
                if (resource != null || testClasses) {
                    cdiClasspathEntries.add(url);
                }

            } finally {
                // cl.close();
            }
        }

    }

    private boolean isCdiClass(Class<?> c) {
        if (c.getProtectionDomain().getCodeSource() == null) {
            return false;
        }
        URL location = c.getProtectionDomain().getCodeSource().getLocation();
        boolean isCdi = cdiClasspathEntries.contains(location);
        return isCdi;

    }

    private boolean isAlternativeStereotype(Class<?> c) {
        return c.isAnnotationPresent(Stereotype.class) && c.isAnnotationPresent(Alternative.class);
    }

    @Override
    public Iterable<Metadata<Extension>> getExtensions() {
        return _extensions;
    }

    public List<BeanDeploymentArchive> getBeanDeploymentArchives() {
        return Collections.singletonList(_beanDeploymentArchive);
    }

    public BeanDeploymentArchive loadBeanDeploymentArchive(Class<?> beanClass) {
        return _beanDeploymentArchive;
    }
}
