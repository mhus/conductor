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
package de.mhus.con.core.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.net.URI;

import org.junit.jupiter.api.Test;

import de.mhus.con.api.ConUtil;
import de.mhus.con.api.Conductor;
import de.mhus.con.api.Context;
import de.mhus.con.api.Lifecycle;
import de.mhus.con.core.ConductorImpl;
import de.mhus.con.core.ConfigTypesImpl;
import de.mhus.con.core.ConfiguratorImpl;
import de.mhus.con.core.ContextImpl;
import de.mhus.con.core.FileScheme;
import de.mhus.con.core.LabelsImpl;
import de.mhus.con.core.ProjectsValidator;
import de.mhus.con.core.SchemesImpl;
import de.mhus.con.core.YmlConfigType;
import de.mhus.lib.errors.MException;
import de.mhus.lib.tests.TestCase;
import de.mhus.lib.tests.TestUtil;

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
        assertEquals("4", con.getProperties().getString("overwriteMe"));
        assertEquals("1", con.getProperties().getString("rootWasThere"));
        assertEquals("1", con.getProperties().getString("parentWasThere"));
        assertEquals("1", con.getProperties().getString("defaultLifecycleWasThere"));
        assertEquals("1", con.getProperties().getString("conductorWasThere"));

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
        assertEquals(
                "2.0.0",
                TestUtil.getPluginVersion(
                        context.make(con.getPlugins().get("newParent").getUri())));

        // test lifecycle
        assertEquals(1, con.getLifecycles().size());
        Lifecycle lc = con.getLifecycles().get("default");
        assertNotNull(lc);
        assertEquals(22, lc.getSteps().size());
    }
}
