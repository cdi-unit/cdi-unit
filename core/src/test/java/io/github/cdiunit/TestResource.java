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
package io.github.cdiunit;

import jakarta.annotation.Resource;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Vetoed;
import jakarta.inject.Named;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.github.cdiunit.junit4.CdiRunner;
import io.github.cdiunit.resource.SupportResource;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(CdiRunner.class)
@SupportResource
public class TestResource {

    AResourceType _unnamedAResource;
    AResourceType _namedAResource;
    AResource _typedAResource;

    @Resource
    void unnamedAResource(AResourceType resource) {
        _unnamedAResource = resource;
    }

    @Resource
    BResourceType unnamedBResource;

    @Resource(name = "namedAResource")
    void withNamedAResource(AResourceType resource) {
        _namedAResource = resource;
    }

    @Resource(name = "namedBResource")
    BResourceType namedBResource;

    @Resource
    public void setTypedAResource(AResource resource) {
        // public to make it visible as Java Bean property to derive the name
        _typedAResource = resource;
    }

    @Resource
    BResource typedBResource;

    @Test
    public void testResourceSupport() {
        assertThat(_namedAResource).isNotEqualTo(_unnamedAResource);
        assertThat(namedBResource).isNotEqualTo(unnamedBResource);
        assertThat(_unnamedAResource instanceof AResource).isTrue();
        assertThat(unnamedBResource instanceof BResource).isTrue();
        assertThat(_namedAResource instanceof AResource).isTrue();
        assertThat(namedBResource instanceof BResource).isTrue();
        assertThat(_typedAResource instanceof AResourceExt).isTrue();
        assertThat(typedBResource instanceof BResourceExt).isTrue();
    }

    interface AResourceType {
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
    static class AResourceExt extends AResource {
    }

    @Vetoed
    static class BResourceExt extends BResource {
    }

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
