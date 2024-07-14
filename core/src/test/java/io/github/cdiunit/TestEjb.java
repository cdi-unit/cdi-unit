/*
 *    Copyright 2014 Bryn Cooke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.cdiunit;

import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Stateless;
import jakarta.enterprise.inject.Produces;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.github.cdiunit.TestEjb.*;
import io.github.cdiunit.ejb.SupportEjb;

@RunWith(CdiRunner.class)
@AdditionalClasses({ EJBStateless.class, EJBStatelessNamed.class, EJBStateful.class, EJBStatefulNamed.class, EJBSingleton.class,
        EJBSingletonNamed.class })
@SupportEjb
public class TestEjb {

    @EJB
    private EJBA inject;

    @EJB(beanName = "named")
    private EJBI injectNamed;

    @EJB(beanName = "TestEjb.EJBStateless")
    private EJBI injectStateless;

    @EJB(beanName = "statelessNamed")
    private EJBI injectStatelessNamed;

    @EJB(beanName = "TestEjb.EJBStateful")
    private EJBI injectStateful;

    @EJB(beanName = "statefulNamed")
    private EJBI injectStatefulNamed;

    @EJB(beanName = "TestEjb.EJBSingleton")
    private EJBI injectSingleton;

    @EJB(beanName = "singletonNamed")
    private EJBI injectSingletonNamed;

    private EJBA expectedDefault = new EJBA();
    private EJBA expectedNamed = new EJBA();

    @EJB(beanName = "named")
    @Produces
    public EJBA providesNamed() {
        return expectedNamed;
    }

    @Test
    public void testEjb() {
        Assert.assertNotEquals(inject, injectNamed);
        Assert.assertTrue(injectStateless instanceof EJBStateless);
        Assert.assertTrue(injectStatelessNamed instanceof EJBStatelessNamed);
        Assert.assertTrue(injectSingleton instanceof EJBSingleton);
        Assert.assertTrue(injectSingletonNamed instanceof EJBSingletonNamed);
        Assert.assertTrue(injectStateful instanceof EJBStateful);
        Assert.assertTrue(injectStatefulNamed instanceof EJBStatefulNamed);
    }

    public static interface EJBI {

    }

    @Stateless
    public static class EJBA implements EJBI {

    }

    @Stateless(name = "statelessNamed")
    public static class EJBStatelessNamed implements EJBI {

    }

    @Stateless(name = "TestEjb.EJBStateless")
    public static class EJBStateless implements EJBI {

    }

    @Stateless(name = "statefulNamed")
    public static class EJBStatefulNamed implements EJBI {

    }

    @Stateless(name = "TestEjb.EJBStateful")
    public static class EJBStateful implements EJBI {

    }

    @Singleton(name = "singletonNamed")
    public static class EJBSingletonNamed implements EJBI {

    }

    @Singleton(name = "TestEjb.EJBSingleton")
    public static class EJBSingleton implements EJBI {

    }
}
