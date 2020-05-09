package org.jglue.cdiunit.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultBeanArchiveScanner implements BeanArchiveScanner {

	private static final Logger log = LoggerFactory.getLogger(DefaultBeanArchiveScanner.class);

	@Override
	public Collection<URL> findBeanArchives(final Collection<URL> classPathEntries) throws IOException {
		// Use string instead of class reference as CdiRunner.class forces JUnit 4 dependency to be present.
		final URL cdiUnitClasspathURL = getClasspathURL("org.jglue.cdiunit.CdiRunner");

		final Set<URL> result = new HashSet<>();
		// cdiClasspathEntries doesn't preserve order, so HashSet is fine
		final Set<URL> entrySet = new HashSet<>(classPathEntries);

		for (URL url : classPathEntries) {
			entrySet.addAll(getEntriesFromManifestClasspath(url));
		}

		for (URL url : entrySet) {
			try (URLClassLoader classLoader = new URLClassLoader(new URL[]{ url },
				null)) {
				// TODO this seems pretty Maven-specific, and fragile
				if (url.getFile().endsWith("/classes/")) {
					URL webInfBeans = new URL(url,
						"../../src/main/webapp/WEB-INF/beans.xml");
					try (InputStream ignore = webInfBeans.openStream()) {
						result.add(url);
					} catch (IOException ignore) {
						// no such file
					}
				}
				// TODO beans.xml is no longer required by CDI (1.1+)
				URL beansXml = classLoader.getResource("META-INF/beans.xml");
				boolean isCdiUnit = url.equals(cdiUnitClasspathURL);
				if (isCdiUnit || beansXml != null || isDirectory(url)) {
					result.add(url);
				}
			}
		}
		log.debug("CDI classpath entries discovered:");
		for (URL url : result) {
			log.debug("{}", url);
		}
		return result;
	}

	private URL getClasspathURL(Class<?> clazz) {
		CodeSource codeSource = clazz.getProtectionDomain()
			.getCodeSource();
		return codeSource != null ? codeSource.getLocation() : null;
	}

	private URL getClasspathURL(String className) {
		try {
			final Class<?> clazz = Class.forName(className);
			return getClasspathURL(clazz);
		} catch (ClassNotFoundException | NoClassDefFoundError clnfe) {
			// NoClassDefFoundError may be thrown as CdiRunner is available on the classpath but
			// BlockJUnit4ClassRunner is not (when using TestNG instead of JUnit 4)
			return null;
		}
	}

	private boolean isDirectory(URL classpathEntry) {
		try {
			return new File(classpathEntry.toURI()).isDirectory();
		} catch (IllegalArgumentException ignore) {
			// Ignore, thrown by File constructor for unsupported URIs
		} catch (URISyntaxException ignore) {
			// Ignore, does not denote an URI that points to a directory
		}
		return false;
	}

	private static Set<URL> getEntriesFromManifestClasspath(URL url)
		throws IOException {
		Set<URL> manifestURLs = new HashSet<>();
		// If this is a surefire manifest-only jar we need to get the original classpath.
		// When testing cdi-unit-tests through Maven, this finds extra entries compared to FCS:
		// eg ".../cdi-unit/cdi-unit-tests/target/classes"
		try (InputStream in = url.openStream();
			 JarInputStream jar = new JarInputStream(in)) {
			Manifest manifest = jar.getManifest();
			if (manifest != null) {
				String classpath = (String) manifest.getMainAttributes()
					.get(Attributes.Name.CLASS_PATH);
				if (classpath != null) {
					String[] manifestEntries = classpath.split(" ?file:");
					for (String entry : manifestEntries) {
						if (entry.length() > 0) {
							// entries is a Set, so this won't add duplicates
							manifestURLs.add(new URL("file:" + entry));
						}
					}
				}
			}
		}
		return manifestURLs;
	}

}
