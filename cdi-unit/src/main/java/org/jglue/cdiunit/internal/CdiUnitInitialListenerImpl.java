package org.jglue.cdiunit.internal;

import org.jboss.weld.module.web.servlet.WeldInitialListener;

/**
 * Enables us to inject initial listener. This implementation works with Weld 1.x and 2.x.
 * @author Sean Flanigan
 */
@SuppressWarnings("deprecation")
public class CdiUnitInitialListenerImpl extends WeldInitialListener implements CdiUnitInitialListener
{
}
