package org.jglue.cdiunit.internal;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;

/**
 * Scanner for bean archives.
 */
public interface BeanArchiveScanner {

	Collection<URL> findBeanArchives(Collection<URL> classPathEntries) throws IOException;

}
