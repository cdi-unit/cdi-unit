/*
 * Copyright 2018, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jglue.cdiunit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;

import static org.junit.platform.commons.util.ReflectionUtils.newInstance;

public class InterceptorExperiment {
    public static void main(String[] arg) throws Exception {
//        CdiExtensionTest outer = new CdiExtensionTest();
//        CdiExtensionTest.Inner inner = outer.new Inner() {
//            @Override
//            void canIntercept() {
//                System.err.println("BEFORE canIntercept");
//                try {
//                    super.canIntercept();
//                } finally {
//                    System.err.println("AFTER  canIntercept");
//                }
//            }
//        };
        CdiExtensionTest outer = getProxy(CdiExtensionTest.class, Optional.empty());
        CdiExtensionTest.Inner inner =
                getProxy(CdiExtensionTest.Inner.class, Optional.of(CdiExtensionTest.class), outer);

        try {
            outer.canInterceptOuter();
        } catch (Exception e) {
            // ignore
        }

        try {
            inner.canIntercept();
        } catch (Exception e) {
            // ignore
        }
    }

    private static <T> T getProxy(Class<T> superclass, Optional<Class<?>> outerClass, Object... constructorArgs)
            throws IllegalAccessException, InvocationTargetException,
            InstantiationException, NoSuchMethodException {
        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(superclass);

        @SuppressWarnings("unchecked")
        Class<? extends Proxy> proxyClass = factory.createClass();
        Class[] paramTypes =
                outerClass.map(aClass -> new Class[]{ aClass })
                        .orElseGet(() -> new Class[0]);

        Constructor<? extends Proxy> ctor =
                proxyClass.getConstructor(paramTypes);
        Proxy proxy = ctor.newInstance(constructorArgs);

        proxy.setHandler(InterceptorExperiment::loggingInterceptor);
        return (T) proxy;
    }

    private static Object loggingInterceptor(Object self, Method thisMethod, Method proceed,
            Object[] args)
            throws InvocationTargetException, IllegalAccessException {
        try {
            System.err.println("BEFORE " + thisMethod);
            return proceed.invoke(self, args);
        } finally {
            System.err.println("AFTER  " + thisMethod);
        }
    }
}
