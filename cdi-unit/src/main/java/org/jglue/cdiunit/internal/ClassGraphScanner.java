package org.jglue.cdiunit.internal;

import java.net.URL;
import java.util.List;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class ClassGraphScanner implements ClasspathScanner {
    private static ClassGraphScanner instance = new ClassGraphScanner();

    private List<URL> classpathURLs;

    @Override
    public List<URL> getClasspathURLs() {
        if (classpathURLs != null) {
          return classpathURLs;
        }

        try (ScanResult scan = new ClassGraph()
                .disableNestedJarScanning()
                .scan()) {
          classpathURLs = scan.getClasspathURLs();
          return classpathURLs;
        }
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

    public static ClassGraphScanner getInstance() {
	    return instance;
    }
}
