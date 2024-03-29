/**
 * Copyright (C) 2020 Mike Hummel (mh@mhus.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.conductor.core.test;

import de.mhus.conductor.core.test.util.TestCase;
import de.mhus.conductor.core.test.util.TestUtil;
import org.junit.jupiter.api.Test;
import de.mhus.commons.errors.MException;
import de.mhus.conductor.api.ConUtil;
import de.mhus.conductor.api.Conductor;
import de.mhus.conductor.api.Context;
import de.mhus.conductor.api.Lifecycle;
import de.mhus.conductor.core.*;

import java.io.File;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ConfiguratorTest extends TestCase {

    @Test
    public void testLoading() throws MException {

        Conductor con = new ConductorImpl(new File("../example/sample-parent"));

        ConfiguratorImpl config = new ConfiguratorImpl();
        ((SchemesImpl) config.getSchemes()).put("file", new FileScheme());
        ((SchemesImpl) config.getSchemes()).put("mvn", new DummyScheme());
        ((ConfigTypesImpl) config.getTypes()).put("yml", new YmlConfigType());
        ((ConfigTypesImpl) config.getTypes()).put("yaml", new YmlConfigType());

        config.getValidators().put("project", new ProjectsValidator());
        config.getDefaultProperties().setString(ConUtil.PROPERTY_VALIDATORS, "project");

        URI uri = URI.create("file:conductor.yml");
        config.configure(uri, con, null);

        // test parameters
        assertEquals("4", con.getProperties().getString("overwriteMe").get());
        assertEquals("1", con.getProperties().getString("rootWasThere").get());
        assertEquals("1", con.getProperties().getString("parentWasThere").get());
        assertEquals("1", con.getProperties().getString("defaultLifecycleWasThere").get());
        assertEquals("1", con.getProperties().getString("conductorWasThere").get());

        // test projects
        assertEquals(4, con.getProjects().size());
        {
            LabelsImpl labels = new LabelsImpl();
            labels.put("group", "bundles");
            assertEquals(2, con.getProjects().select(labels).size());
        }
        {
            LabelsImpl labels = new LabelsImpl();
            labels.put("group", "parent");
            assertEquals(1, con.getProjects().select(labels).size());
        }
        {
            LabelsImpl labels = new LabelsImpl();
            assertEquals(4, con.getProjects().select(labels).size());
        }
        Context context = new ContextImpl(con, 0);

        // test plugins
        assertEquals(8, con.getPlugins().size());
//        assertEquals(
//                "2.0.0",
//                TestUtil.getPluginVersion(
//                        context.make(con.getPlugins().get("newParent").getUri())));

        // test lifecycle
        assertEquals(1, con.getLifecycles().size());
        Lifecycle lc = con.getLifecycles().get("default");
        assertNotNull(lc);
        assertEquals(22, lc.getSteps().size());
    }
}
