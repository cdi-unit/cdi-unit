package io.github.cdiunit.internal.servlet;

import jakarta.enterprise.inject.Typed;

import org.jboss.weld.module.web.servlet.WeldInitialListener;

/**
 * Enables us to inject initial listener.
 *
 * @author Sean Flanigan
 */
@Typed(CdiUnitInitialListener.class)
public class CdiUnitInitialListenerImpl extends WeldInitialListener implements CdiUnitInitialListener {
}
