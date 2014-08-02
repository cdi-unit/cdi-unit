package org.jglue.cdiunit.internal;

import org.reflections.scanners.AbstractScanner;
import org.reflections.vfs.Vfs;

public class TypesScanner extends AbstractScanner {


    @Override
    public void scan(Object cls) {
    	String className = getMetadataAdapter().getClassName(cls);
		

        if (acceptResult(className)) {
            getStore().put(className, className);
        }
    }
}