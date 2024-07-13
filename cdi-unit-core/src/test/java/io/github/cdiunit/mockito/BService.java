package io.github.cdiunit.mockito;

import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
public class BService {

    @Inject
    private CService unknownService;

}
