package org.jglue.cdiunit.internal;

import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public interface ClasspathScanner {

	Collection<URL> getBeanArchives();

	List<String> getClassNamesForClasspath(URL[] urls);

	List<String> getClassNamesForPackage(String packageName, URL url);

	default URL getClasspathURL(Class<?> cls) {
		return Optional.ofNullable(cls)
			.map(Class::getProtectionDomain)
			.map(ProtectionDomain::getCodeSource)
			.map(CodeSource::getLocation)
			.orElse(null);
	}

	default boolean isContainedInBeanArchive(Class<?> cls) {
		final URL location = getClasspathURL(cls);
		return location != null && getBeanArchives().contains(location);
	}

}
