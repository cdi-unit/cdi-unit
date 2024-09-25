/*
 * Copyright 2014 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.cdiunit.internal.naming;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

public class CdiUnitContextFactory implements InitialContextFactory {

    private static final ThreadLocal<CdiUnitContext> context = ThreadLocal.withInitial(
            CdiUnitContextFactory::newInitialContext);

    public CdiUnitContextFactory() {
        this(new Hashtable<>());
    }

    public CdiUnitContextFactory(Hashtable<?, ?> environment) {
    }

    @Override
    public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
        return context.get();
    }

    private static CdiUnitContext newInitialContext() {
        CdiUnitContext initialContext = new CdiUnitContext();
        initialContext.doAfterClose(CdiUnitContextFactory::cleanup);
        return initialContext;
    }

    private static void cleanup() {
        context.remove();
    }

}
