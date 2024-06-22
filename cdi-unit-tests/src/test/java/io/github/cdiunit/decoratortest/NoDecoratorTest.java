package io.github.cdiunit.decoratortest;

import jakarta.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.github.cdiunit.AdditionalClasses;
import io.github.cdiunit.CdiRunner;

import static org.junit.Assert.assertEquals;

/**
 * Created by pcasaes on 30/03/17.
 */
@RunWith(CdiRunner.class)
@AdditionalClasses({
        DecoratedImpl.class,
})
public class NoDecoratorTest {
    @Inject
    private DecoratedInterface decorated;

    @Test
    public void testZero() {
        assertEquals(0, decorated.calculate());
    }
}
