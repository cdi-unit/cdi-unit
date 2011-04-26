package org.sprog.cdiunit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.enterprise.inject.spi.Extension;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.environment.se.discovery.AbstractWeldSEDeployment;
import org.jboss.weld.environment.se.discovery.ImmutableBeanDeploymentArchive;
import org.jboss.weld.environment.se.discovery.url.URLScanner;
import org.jboss.weld.metadata.MetadataImpl;
import org.jboss.weld.resources.spi.ResourceLoader;

import com.google.common.collect.Iterables;

public class WeldTestUrlDeployment extends AbstractWeldSEDeployment {
	private final BeanDeploymentArchive _beanDeploymentArchive;
	private Collection<Metadata<Extension>> _extensions = new ArrayList<Metadata<Extension>>();
	public WeldTestUrlDeployment(ResourceLoader resourceLoader,
			Bootstrap bootstrap, Class<?> testClass) {
		super(bootstrap);
		BeanDeploymentArchive archive = new URLScanner(resourceLoader,
				bootstrap, RESOURCES).scan();

		Collection<String> discoveredClasses = new ArrayList<String>(archive.getBeanClasses());
		discoveredClasses.add(testClass.getName());
		
		SupportClasses supportClasses = testClass.getAnnotation(SupportClasses.class);
		for(Class<?> supportClass : supportClasses.value()){
			discoveredClasses.add(supportClass.getName());
		}
		archive.getBeansXml().getEnabledAlternativeStereotypes().add(new MetadataImpl<String>(TestAlternative.class.getName(), TestAlternative.class.getName()));
		_beanDeploymentArchive = new ImmutableBeanDeploymentArchive(
				archive.getId(), discoveredClasses,
				archive.getBeansXml());
		
		_beanDeploymentArchive.getServices().add(ResourceLoader.class, resourceLoader);
		_extensions.add(new MetadataImpl<Extension>(new MockExtension(), MockExtension.class.getName()));
	}
	
	@Override
	public Iterable<Metadata<Extension>> getExtensions() {
		return Iterables.concat(super.getExtensions(), _extensions);
	}
	
	

	public List<BeanDeploymentArchive> getBeanDeploymentArchives() {
		return Collections.singletonList(_beanDeploymentArchive);
	}

	public BeanDeploymentArchive loadBeanDeploymentArchive(Class<?> beanClass) {
		return _beanDeploymentArchive;
	}
}
