package io.github.cdiunit.decoratortest;

import static org.junit.Assert.assertEquals;

import jakarta.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.github.cdiunit.AdditionalClasses;
import io.github.cdiunit.CdiRunner;

/**
 * Created by pcasaes on 30/03/17.
 */
@RunWith(CdiRunner.class)
@AdditionalClasses({
        DecoratedImpl.class,
        AddOneDecorator.class
})
public class AddOneDecoratorTest {

    @Inject
    private DecoratedInterface decorated;

    @Test
    public void testAddOne() {
        assertEquals(1, decorated.calculate());
    }
}
