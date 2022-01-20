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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import de.mhus.con.api.ConUtil;
import de.mhus.con.api.Conductor;
import de.mhus.con.api.ConductorPlugin;
import de.mhus.con.api.ExecutePlugin;
import de.mhus.con.api.Plugin;
import de.mhus.con.core.ConductorImpl;
import de.mhus.con.core.ConfigTypesImpl;
import de.mhus.con.core.ConfiguratorImpl;
import de.mhus.con.core.ContextImpl;
import de.mhus.con.core.ExecutorImpl;
import de.mhus.con.core.FileScheme;
import de.mhus.con.core.MavenScheme;
import de.mhus.con.core.SchemesImpl;
import de.mhus.con.core.StepImpl;
import de.mhus.con.core.YmlConfigType;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.util.MUri;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.lib.tests.TestCase;
import de.mhus.lib.tests.TestUtil;

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
