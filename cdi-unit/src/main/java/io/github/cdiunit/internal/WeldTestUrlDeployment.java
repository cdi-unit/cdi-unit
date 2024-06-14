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
package io.github.cdiunit.internal;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.environment.se.WeldSEBeanRegistrant;
import org.jboss.weld.resources.spi.ResourceLoader;
import io.github.cdiunit.ProducesAlternative;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.Extension;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class WeldTestUrlDeployment implements Deployment {
	private final BeanDeploymentArchive beanDeploymentArchive;
	private final ClasspathScanner scanner = new CachingClassGraphScanner(new DefaultBeanArchiveScanner());
	private final Collection<Metadata<Extension>> extensions = new ArrayList<>();
	private static final Logger log = LoggerFactory.getLogger(WeldTestUrlDeployment.class);
	private final ServiceRegistry serviceRegistry = new SimpleServiceRegistry();

	public WeldTestUrlDeployment(ResourceLoader resourceLoader, Bootstrap bootstrap, TestConfiguration testConfiguration) throws IOException {
		final DefaultBootstrapDiscoveryContext bdc = new DefaultBootstrapDiscoveryContext();

		final ServiceLoader<DiscoveryExtension> discoveryExtensions = ServiceLoader.load(DiscoveryExtension.class);
		discoveryExtensions.forEach(extension -> extension.bootstrap(bdc));

		// Capture values to ignore potential updates after the bootstrap
		final Consumer<DiscoveryExtension.Context> discoverExtension = bdc.discoverExtension;
		final BiConsumer<DiscoveryExtension.Context, Class<?>> discoverClass = bdc.discoverClass;
		final BiConsumer<DiscoveryExtension.Context, Field> discoverField = bdc.discoverField;
		final BiConsumer<DiscoveryExtension.Context, Method> discoverMethod = bdc.discoverMethod;

		final BeansXml beansXml = WeldComponentFactory.createBeansXml();

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

		beansXml.getEnabledAlternativeStereotypes().add(WeldComponentFactory.createMetadata(
			ProducesAlternative.class.getName(), ProducesAlternative.class.getName()
		));

		for (String alternative : discoveryContext.getAlternatives()) {
			beansXml.getEnabledAlternativeClasses().add(WeldComponentFactory.createMetadata(alternative, alternative));
		}

		extensions.add(WeldComponentFactory.createMetadata(new WeldSEBeanRegistrant(), WeldSEBeanRegistrant.class.getName()));

		extensions.addAll(discoveryContext.getExtensions());

		beanDeploymentArchive = new BeanDeploymentArchiveImpl("cdi-unit" + UUID.randomUUID(), discoveredClasses, beansXml);
		beanDeploymentArchive.getServices().add(ResourceLoader.class, resourceLoader);
		log.debug("CDI-Unit discovered:");
		for (String clazz : discoveredClasses) {
			if (!clazz.startsWith("io.github.cdiunit.internal.")) {
				log.debug(clazz);
			}
		}

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

}
