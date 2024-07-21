package io.github.cdiunit.internal;

import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.bootstrap.spi.Scanning;
import org.jboss.weld.bootstrap.spi.helpers.MetadataImpl;
import org.jboss.weld.metadata.BeansXmlImpl;

public final class WeldComponentFactory {

    private WeldComponentFactory() {
    }

    static <T> Metadata<T> createMetadata(T value, String location) {
        return new MetadataImpl<>(value, location);
    }

    static BeansXml createBeansXml() {
        try {
            // The constructor for BeansXmlImpl has added more parameters in newer Weld versions. The parameter list
            // is truncated in older version of Weld where the number of parameters is shorter, thus omitting the
            // newer parameters.
            Object[] initArgs = new Object[] {
                    new ArrayList<Metadata<String>>(), new ArrayList<Metadata<String>>(),
                    new ArrayList<Metadata<String>>(), new ArrayList<Metadata<String>>(), Scanning.EMPTY_SCANNING,
                    // These were added in Weld 2.0:
                    new URL("file:cdi-unit"), BeanDiscoveryMode.ANNOTATED, "cdi-unit",
                    // isTrimmed: added in Weld 2.4.2 [WELD-2314]:
                    false
            };
            Constructor<?> beansXmlConstructor = BeansXmlImpl.class.getConstructors()[0];
            return (BeansXml) beansXmlConstructor.newInstance(
                    Arrays.copyOfRange(initArgs, 0, beansXmlConstructor.getParameterCount()));
        } catch (MalformedURLException | ReflectiveOperationException e) {
            throw ExceptionUtils.asRuntimeException(e);
        }
    }

}
