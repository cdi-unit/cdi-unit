package org.jglue.cdiunit.internal;

import java.net.URL;
import java.util.List;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class ClassGraphScanner implements ClasspathScanner {

    @Override
    public List<URL> getClasspathURLs() {
        return new ClassGraph().scan()
                .getClasspathURLs();
    }

    @Override
    public List<String> getClassNamesForClasspath(URL[] urls) {
        ScanResult scan = new ClassGraph()
                .overrideClasspath(urls)
                .ignoreClassVisibility()
                .enableClassInfo()
                .scan();
        return scan.getAllClasses().getNames();
    }

    @Override
    public List<String> getClassNamesForPackage(String packageName, URL url) {
        ScanResult scan = new ClassGraph()
                .whitelistPackagesNonRecursive(packageName)
                .overrideClasspath(url)
                .ignoreClassVisibility()
                .enableClassInfo()
                .scan();

        return scan.getAllClasses().getNames();
    }
}
