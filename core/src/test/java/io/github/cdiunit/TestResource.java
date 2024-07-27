package io.github.cdiunit;

import jakarta.annotation.Resource;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Vetoed;
import jakarta.inject.Named;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.github.cdiunit.resource.SupportResource;

@RunWith(CdiRunner.class)
@SupportResource
public class TestResource {

    @Resource
    AResourceType unnamedAResource;

    @Resource
    BResourceType unnamedBResource;

    @Resource(name = "namedAResource")
    AResourceType namedAResource;

    @Resource(name = "namedBResource")
    BResourceType namedBResource;

    @Resource
    AResource typedAResource;

    @Resource
    BResource typedBResource;

    @Test
    public void testResourceSupport() {
        Assert.assertNotEquals(unnamedAResource, namedAResource);
        Assert.assertNotEquals(unnamedBResource, namedBResource);
        Assert.assertTrue(unnamedAResource instanceof AResource);
        Assert.assertTrue(unnamedBResource instanceof BResource);
        Assert.assertTrue(namedAResource instanceof AResource);
        Assert.assertTrue(namedBResource instanceof BResource);
        Assert.assertTrue(typedAResource instanceof AResourceExt);
        Assert.assertTrue(typedBResource instanceof BResourceExt);
    }

    interface AResourceType {
    }

    interface BResourceType {
    }

    @Vetoed
    static class AResource implements AResourceType {
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
    @Named("namedBResource")
    BResourceType produceNamedBResource() {
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
