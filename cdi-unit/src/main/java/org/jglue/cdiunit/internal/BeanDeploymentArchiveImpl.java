package org.jglue.cdiunit.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.ejb.spi.EjbDescriptor;
import org.jboss.weld.environment.se.discovery.AbstractWeldSEBeanDeploymentArchive;

public class BeanDeploymentArchiveImpl implements BeanDeploymentArchive {

	private final Collection<String> beanClasses;
	private final BeansXml beansXml;
	private final Collection<BeanDeploymentArchive> beanDeploymentArchives;
	private final ServiceRegistry serviceRegistry = new SimpleServiceRegistry();
	private final String id;

	public BeanDeploymentArchiveImpl(String id,
			Collection<String> beanClasses, BeansXml beansXml,
			Collection<BeanDeploymentArchive> beanDeploymentArchives) {
		this.id = id;
		this.beanClasses = beanClasses;
		this.beansXml = beansXml;
		this.beanDeploymentArchives = beanDeploymentArchives;
	}

	public BeanDeploymentArchiveImpl(String id,
			Collection<String> beanClasses, BeansXml beansXml) {
		this(id, beanClasses, beansXml, new ArrayList<BeanDeploymentArchive>());
	}

	public Collection<String> getBeanClasses() {
		return Collections.unmodifiableCollection(beanClasses);
	}

	public Collection<BeanDeploymentArchive> getBeanDeploymentArchives() {
		return Collections.unmodifiableCollection(beanDeploymentArchives);
	}

	public BeansXml getBeansXml() {
		return beansXml;
	}

	@Override
	public Collection<EjbDescriptor<?>> getEjbs() {
		return Collections.emptyList();
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public ServiceRegistry getServices() {
		return serviceRegistry;
	}
}