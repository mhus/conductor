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
package org.summerclouds.conductor.core.test;

import org.junit.jupiter.api.Test;
import org.summerclouds.common.core.error.NotFoundException;
import org.summerclouds.common.core.node.MProperties;
import org.summerclouds.common.core.tool.MFile;
import org.summerclouds.common.core.tool.MString;
import org.summerclouds.common.core.util.MUri;
import org.summerclouds.common.junit.TestCase;
import org.summerclouds.common.junit.TestUtil;
import org.summerclouds.conductor.api.*;
import org.summerclouds.conductor.core.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SchemeTest extends TestCase {

    @Test
    public void testClassifier()
            throws IOException, NotFoundException, ParserConfigurationException, SAXException {
        MavenScheme scheme = new MavenScheme();
        Conductor con = new ConductorImpl(new File("../example/sample-parent"));

        MUri uri = ConUtil.getDefaultConfiguration("configuration-default.yml");
        File file = scheme.load(con, uri);
        assertNotNull(file);
        String content = MFile.readFile(file);
        assertTrue(MString.isSetTrim(content));
    }

    @Test
    public void testMavenScheme() throws IOException, NotFoundException {
        MavenScheme scheme = new MavenScheme();
        Conductor con = new ConductorImpl(new File("../example/sample-parent"));

        String mvnPath = ConUtil.cmdLocationOrNull(con, "mvn");
        if (mvnPath != null) {

            MUri uri = MUri.toUri("mvn:com.hpe.adm.octane.ciplugins/integrations-sdk/2.6.3.4");

            File loc = scheme.getArtifactLocation("TEST", con, uri);
            System.out.println(loc);

            if (loc.exists()) loc.delete();

            scheme.load(con, uri);

            assertTrue(loc.exists());
        } else {
            System.err.println("Maven not found, skip test: " + mvnPath);
        }
    }

    @Test
    public void testMavenSchemeClassifier() throws IOException, NotFoundException {
        MavenScheme scheme = new MavenScheme();
        Conductor con = new ConductorImpl(new File("../example/sample-parent"));

        String mvnPath = ConUtil.cmdLocationOrNull(con, "mvn");
        if (mvnPath != null) {

            MUri uri = MUri.toUri("mvn:org.apache.karaf.features/standard/4.2.6/xml/features");

            File loc = scheme.getArtifactLocation("TEST", con, uri);
            System.out.println(loc);

            if (loc.exists()) loc.delete();

            scheme.load(con, uri);

            assertTrue(loc.exists());
        } else {
            System.err.println("Maven not found, skip test: " + mvnPath);
        }
    }

    @Test
    public void loadPlugin() throws Exception {

        ConductorImpl con = new ConductorImpl(new File("../example/sample-parent"));

        String mvnPath = ConUtil.cmdLocationOrNull(con, "mvn");
        if (mvnPath != null) {

            ConfiguratorImpl config = new ConfiguratorImpl();
            ((SchemesImpl) config.getSchemes()).put("file", new FileScheme());
            ((SchemesImpl) config.getSchemes()).put("mvn", new DummyScheme());
            ((ConfigTypesImpl) config.getTypes()).put("yml", new YmlConfigType());
            ((ConfigTypesImpl) config.getTypes()).put("yaml", new YmlConfigType());

            URI uri = URI.create("file:conductor.yml");
            config.configure(uri, con, null);

            ((MProperties) con.getProperties()).put("conductor.version", TestUtil.conrentVersion());
            ((SchemesImpl) con.getSchemes()).put("mvn", new MavenScheme());

            StepImpl step = new StepImpl();
            ContextImpl context = new ContextImpl(con, 0);
            context.init(null, null, null, con.getPlugins().get("test"), step);

            ExecutorImpl exec = new ExecutorImpl();

            Plugin plugin = context.getPlugin();

            ConductorPlugin mojo = exec.createMojo(con, plugin);

            assertNotNull(mojo);
            ((ExecutePlugin) mojo).execute(context);

        } else {
            System.err.println("Maven not found, skip test: " + mvnPath);
        }
    }
}
