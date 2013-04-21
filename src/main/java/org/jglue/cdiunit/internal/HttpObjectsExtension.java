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
		Set types = bean.getBeanAttributes().getTypes();

		if (types.contains(HttpServletRequest.class) || types.contains(HttpSession.class) && !types.contains(HttpSessionBean.class) && !types.contains(HttpServletRequestBean.class)) {
			final Set qualifiers = new HashSet(bean.getBeanAttributes().getQualifiers());
			qualifiers.add(CdiUnitImplLiteral.INSTANCE);
			qualifiers.remove(DefaultLiteral.INSTANCE);
			final BeanAttributes beanAttributes = bean.getBeanAttributes();
			
			bean.setBeanAttributes(new BeanAttributes() {

				@Override
				public Set getTypes() {
					return beanAttributes.getTypes();
				}

				@Override
				public Set getQualifiers() {
					return qualifiers;
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
