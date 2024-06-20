package io.github.cdiunit.decoratortest;

import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;

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
