/*
 * Copyright 2024 the original author or authors.
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
package io.github.cdiunit.core.tests;

import java.util.function.Consumer;

import jakarta.annotation.Resource;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Vetoed;
import jakarta.inject.Named;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.cdiunit.AdditionalClasses;
import io.github.cdiunit.internal.TestConfiguration;
import io.github.cdiunit.internal.TestLifecycle;
import io.github.cdiunit.resource.SupportResource;

import static org.assertj.core.api.Assertions.assertThat;

class TestResource {

    private TestLifecycle testLifecycle;
    private TestBean testBean;

    @BeforeEach
    void setup() throws Throwable {
        this.testLifecycle = new TestLifecycle(new TestConfiguration(TestBean.class));
        this.testBean = testLifecycle.createTest(null);
    }

    @AfterEach
    void teardown() {
        this.testBean = null;
        this.testLifecycle.shutdown();
    }

    @Test
    void unnamedResources() {
        testBean.expose(i -> {
            assertThat(i.unnamedAResource).as("AResource")
                    .isNotNull()
                    .isInstanceOf(AResource.class);
            assertThat(i.unnamedBResource).as("BResource")
                    .isNotNull()
                    .isInstanceOf(BResource.class);
        });
    }

    @Test
    void namedResources() {
        testBean.expose(i -> {
            assertThat(i.namedAResource).as("AResource")
                    .isNotNull()
                    .isNotSameAs(i.unnamedAResource)
                    .isInstanceOf(AResource.class);
            assertThat(i.namedBResource).as("BResource")
                    .isNotNull()
                    .isNotSameAs(i.unnamedBResource)
                    .isInstanceOf(BResource.class);
        });
    }

    @Test
    void typedResources() {
        testBean.expose(i -> {
            assertThat(i.typedAResource).as("AResource")
                    .isNotNull()
                    .isInstanceOf(AResourceExt.class);
            assertThat(i.typedBResource).as("BResource")
                    .isNotNull()
                    .isInstanceOf(BResourceExt.class);
        });
    }

    static class Resources {

        @Produces
        @Resource(name = "unnamedAResource")
        AResourceType produceUnnamedAResource = new AResource();

        @Produces
        @Resource(name = "unnamedBResource")
        BResourceType produceUnnamedBResource() {
            return new BResource();
        }

        @Produces
        @Named("namedAResource")
        AResourceType produceNamedAResource = new AResource();

        @Produces
        @Resource
        public BResourceType getNamedBResource() {
            // public to make it visible as Java Bean property to derive the name
            return new BResource();
        }

        @Produces
        @Resource(name = "typedAResource", type = AResource.class)
        AResourceExt produceTypedAResource = new AResourceExt();

        @Produces
        @Resource(name = "typedBResource", type = BResource.class)
        BResourceExt produceTypedBResource() {
            return new BResourceExt();
        }

    }

    @SupportResource
    @AdditionalClasses(Resources.class)
    static class TestBean {

        AResourceType unnamedAResource;
        AResourceType namedAResource;
        AResource typedAResource;

        @Resource
        void unnamedAResource(AResourceType resource) {
            unnamedAResource = resource;
        }

        @Resource
        BResourceType unnamedBResource;

        @Resource(name = "namedAResource")
        void withNamedAResource(AResourceType resource) {
            namedAResource = resource;
        }

        @Resource(name = "namedBResource")
        BResourceType namedBResource;

        @Resource
        public void setTypedAResource(AResource resource) {
            // public to make it visible as Java Bean property to derive the name
            typedAResource = resource;
        }

        @Resource
        BResource typedBResource;

        void expose(Consumer<TestBean> consumer) {
            consumer.accept(this);
        }

    }

    public interface AResourceType {
    }

    public interface BResourceType {
    }

    @Vetoed
    public static class AResource implements AResourceType {
    }

    @Vetoed
    static class BResource implements BResourceType {
    }

    @Vetoed
    public static class AResourceExt extends AResource {
    }

    @Vetoed
    static class BResourceExt extends BResource {
    }

}
