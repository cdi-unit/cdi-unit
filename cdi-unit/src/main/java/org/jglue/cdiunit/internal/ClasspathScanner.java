package org.jglue.cdiunit.internal;

import java.net.URL;
import java.util.Collection;
import java.util.List;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public interface ClasspathScanner {
    Collection<URL> getBeanArchives();
    List<String> getClassNamesForClasspath(URL[] urls);
    List<String> getClassNamesForPackage(String packageName, URL url);
}
