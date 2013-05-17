package org.jglue.cdiunit.internal;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBeanAttributes;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.weld.bean.builtin.ee.HttpServletRequestBean;
import org.jboss.weld.bean.builtin.ee.HttpSessionBean;
import org.jboss.weld.literal.DefaultLiteral;

public class HttpObjectsExtension implements Extension {

	<T> void processSession(@Observes final ProcessBeanAttributes bean) {
		final BeanAttributes beanAttributes = bean.getBeanAttributes();
		Set types = beanAttributes.getTypes();
		Set qualifiers = beanAttributes.getQualifiers();
		if (qualifiers.contains(DefaultLiteral.INSTANCE) && (types.contains(HttpServletRequest.class) || types.contains(HttpSession.class)) && !types.contains(HttpSessionBean.class) && !types.contains(HttpServletRequestBean.class)) {
			
			final Set modifiedTypes = new HashSet(types);
			final Set modifiedQualifiers = new HashSet(qualifiers);
			if(types.contains(HttpServletRequest.class)) {
				modifiedQualifiers.add(CdiUnitRequestLiteral.INSTANCE);
			}
			
			modifiedTypes.remove(HttpSession.class);
			modifiedTypes.remove(HttpServletRequest.class);

						
			
			bean.setBeanAttributes(new BeanAttributes() {

				@Override
				public Set getTypes() {
					return modifiedTypes;
				}

				@Override
				public Set getQualifiers() {
					return modifiedQualifiers;
				}

				@Override
				public Class getScope() {
					return beanAttributes.getScope();
				}

				@Override
				public String getName() {
					return beanAttributes.getName();
				}

				@Override
				public Set getStereotypes() {
					return beanAttributes.getStereotypes();
				}

				@Override
				public boolean isAlternative() {
					return beanAttributes.isAlternative();
				}
				
			});
		}
	}
}
