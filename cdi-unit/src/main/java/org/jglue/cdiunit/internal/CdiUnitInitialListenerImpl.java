package org.jglue.cdiunit.internal;

import org.jboss.weld.module.web.servlet.WeldInitialListener;

/**
 * Enables us to inject initial listener. This implementation is for Weld 3. See CdiUnitInitialListenerProducer for
 * the implementation which is dynamically generated when running with Weld 1 or 2.
 * @author Sean Flanigan
 */
public class CdiUnitInitialListenerImpl extends WeldInitialListener implements CdiUnitInitialListener
{
}
