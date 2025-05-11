/*
 * Copyright 2025 the original author or authors.
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
package io.github.cdiunit.core.beanarchive;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.jar.Manifest;

import jakarta.enterprise.inject.spi.CDIProvider;
import jakarta.enterprise.inject.spi.Extension;

import io.github.cdiunit.core.classcontributor.ClassContributor;
import io.github.cdiunit.internal.DiscoveryExtension;

interface BeanArchive {

    ResourcesContext EMPTY_RESOURCES_CONTEXT = new EmpyBeanArchiveResources();

    String[] BEANS_XML_PATHS = new String[] {
            "META-INF/beans.xml",
            "WEB-INF/beans.xml",
            "WEB-INF/classes/META-INF/beans.xml"
    };
    String META_INF_SERVICES_PATH = "META-INF/services/";
    String CDI_PROVIDER_PATH = META_INF_SERVICES_PATH + CDIProvider.class.getName();
    String CDI_EXTENSION_PATH = META_INF_SERVICES_PATH + Extension.class.getName();
    String CDI_UNIT_EXTENSION_PATH = META_INF_SERVICES_PATH + DiscoveryExtension.class.getName();
    String CDI_UNIT_ARCHIVE_PATH = "META-INF/io.github.cdiunit-archive";

    interface Resources {

        /**
         * Check if resource exists.
         *
         * @param path path to the resource
         * @return true if resource exists and readable
         */
        default boolean exists(final String path) {
            return anyExist(path);
        }

        /**
         * Check if any of the specified resources exists.
         *
         * @param paths paths to the resource
         * @return true if any of the specified resources exists and is readable
         */
        boolean anyExist(final String... paths);

        /**
         * Get manifest.
         *
         * @return optional manifest
         */
        Optional<Manifest> getManifest();

    }

    @FunctionalInterface
    interface ResourcesContext {

        /**
         * Consume bean archive resources.
         *
         * @param consumer resources consumer
         */
        void withResources(final Consumer<Resources> consumer);

    }

    @FunctionalInterface
    interface ResourceContextAdapter {

        /**
         * Construct resource context for the provided contributor.
         *
         * @param classContributor class contributor
         * @return resource context
         */
        ResourcesContext from(final ClassContributor classContributor);

    }

}
