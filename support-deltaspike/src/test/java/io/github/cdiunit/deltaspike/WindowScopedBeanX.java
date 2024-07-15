package io.github.cdiunit.deltaspike;

import java.io.Serializable;

import org.apache.deltaspike.core.api.scope.WindowScoped;

@WindowScoped
public class WindowScopedBeanX implements Serializable {

    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
