package org.jglue.cdiunit.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public interface ClasspathScanner {

	Logger log = LoggerFactory.getLogger(ClasspathScanner.class);

	Collection<URL> getBeanArchives();

	List<String> getClassNamesForClasspath(URL[] urls);

	List<String> getClassNamesForPackage(String packageName, URL url);

	default URL getClasspathURL(Class<?> cls) {
		return Optional.ofNullable(cls)
			.map(Class::getProtectionDomain)
			.map(ProtectionDomain::getCodeSource)
			.map(CodeSource::getLocation)
			.map(this::getRealURL)
			.orElse(null);
	}

	default URL getRealURL(URL urlWithPotentialSymLink) {
		try {
			Path realPath = Paths.get(urlWithPotentialSymLink.toURI()).toRealPath();
			URL realURL = realPath.toUri().toURL();
			if (log.isDebugEnabled() && !realURL.equals(urlWithPotentialSymLink)) {
				log.debug("Adapting URL:" + urlWithPotentialSymLink + " to URL:" + realURL);
			}
			return realURL;
		} catch (Exception e) {
			log.warn("Could not try to find real path (without symlink, ...) for URL:" + urlWithPotentialSymLink.toString(), e);
		}

		return urlWithPotentialSymLink;
	}

	default boolean isContainedInBeanArchive(Class<?> cls) {
		final URL location = getClasspathURL(cls);
		return location != null && getBeanArchives().contains(location);
	}

}
