package org.jglue.cdiunit.decoratortest;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

/**
 * Created by pcasaes on 30/03/17.
 */
@Decorator
public abstract class AddOneDecorator implements DecoratedInterface {
    @Inject
    @Delegate
    @Any
    private DecoratedInterface decorated;

    @Override
    public int calculate() {
        return 1 + decorated.calculate();
    }
}
