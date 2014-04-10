/*
 *    Copyright 2011 Bryn Cooke
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
package org.jglue.cdiunit;

import java.io.IOException;

import javax.naming.InitialContext;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.api.CDI11Bootstrap;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jglue.cdiunit.internal.Weld11TestUrlDeployment;
import org.jglue.cdiunit.internal.WeldTestUrlDeployment;
import org.junit.Test;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * <code>&#064;CdiRunner</code> is a JUnit runner that uses a CDI container to
 * create unit test objects. Simply add
 * <code>&#064;RunWith(CdiRunner.class)</code> to your test class.
 * 
 * <pre>
 * <code>
 * &#064;RunWith(CdiRunner.class) // Runs the test with CDI-Unit
 * class MyTest {
 *   &#064;Inject
 *   Something something; // This will be injected before the tests are run!
 * 
 *   ... //The rest of the test goes here.
 * }</code>
 * </pre>
 * 
 * @author Bryn Cooke
 */
public class CdiRunner extends BlockJUnit4ClassRunner {

    private Class<?> clazz;
    private Weld weld;
    private WeldContainer container;
    private Throwable startupException;

    public CdiRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
        this.clazz = clazz;
    }

    protected Object createTest() throws Exception {
        try {
     
            weld = new Weld() {
            	
            	protected Deployment createDeployment(
            			ResourceLoader resourceLoader, CDI11Bootstrap bootstrap) {
            		try {
                        return new Weld11TestUrlDeployment(resourceLoader, bootstrap, clazz);
                    } catch (IOException e) {
                        startupException = e;
                        throw new RuntimeException(e);
                    }
            	}
            	
                protected Deployment createDeployment(ResourceLoader resourceLoader, Bootstrap bootstrap) {
                    try {
                        return new WeldTestUrlDeployment(resourceLoader, bootstrap, clazz);
                    } catch (IOException e) {
                        startupException = e;
                        throw new RuntimeException(e);
                    }
                };

            };

            try {

                container = weld.initialize();
            } catch (Throwable e) {
                if (startupException == null) {
                    startupException = e;
                }
            }

        } catch (ClassFormatError e) {
            startupException = new Exception(
                    "There were class format errors. This is often caused by API only jars on the classpath. If you are using maven then you need to place these after the CDI unit dependency as 'provided' scope is still available during testing.", e);
        }
        catch (Throwable e) {
            startupException = new Exception(
                    "Unable to start weld", e);
        }

        return createTest(clazz);
    }

    private <T> T createTest(Class<T> testClass) {

        T t = container.instance().select(testClass).get();

        return t;
    }

    @Override
    protected Statement methodBlock(final FrameworkMethod method) {
        final Statement defaultStatement = super.methodBlock(method);
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {

                if (startupException != null) {
                    if (method.getAnnotation(Test.class).expected() == startupException.getClass()) {
                        return;
                    }
                    throw startupException;
                }
                System.setProperty("java.naming.factory.initial", "org.jglue.cdiunit.internal.CdiUnitContextFactory");
                InitialContext initialContext = new InitialContext();
                initialContext.bind("java:comp/BeanManager", container.getBeanManager());

                try {
                    defaultStatement.evaluate();

                } finally {
                    initialContext.close();
                    weld.shutdown();

                }

            }
        };

    }

}

