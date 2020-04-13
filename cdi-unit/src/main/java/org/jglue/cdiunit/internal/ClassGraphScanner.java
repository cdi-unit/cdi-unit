package org.jglue.cdiunit.internal;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.apache.deltaspike.core.util.ExceptionUtils;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
class ClassGraphScanner implements ClasspathScanner {

    private final BeanArchiveScanner beanArchiveScanner;

    ClassGraphScanner(final BeanArchiveScanner beanArchiveScanner) {
        this.beanArchiveScanner = beanArchiveScanner;
    }

    private List<URL> getClasspathURLs() {
        try (ScanResult scan = new ClassGraph()
                .disableNestedJarScanning()
                .scan()) {
            return scan.getClasspathURLs();
        }
    }

    @Override
    public Collection<URL> getBeanArchives() {
        final List<URL> urls = getClasspathURLs();
        try {
            return beanArchiveScanner.findBeanArchives(urls);
        } catch (Exception e) {
            ExceptionUtils.throwAsRuntimeException(e);
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> getClassNamesForClasspath(URL[] urls) {
        try (ScanResult scan = new ClassGraph()
                .disableNestedJarScanning()
                .enableClassInfo()
                .ignoreClassVisibility()
                .overrideClasspath(urls)
                .scan()) {
            return scan.getAllClasses().getNames();
        }
    }

    @Override
    public List<String> getClassNamesForPackage(String packageName, URL url) {
        try (ScanResult scan = new ClassGraph()
                .disableNestedJarScanning()
                .enableClassInfo()
                .ignoreClassVisibility()
                .overrideClasspath(url)
                .whitelistPackagesNonRecursive(packageName)
                .scan()) {
            return scan.getAllClasses().getNames();
        }
    }
}
