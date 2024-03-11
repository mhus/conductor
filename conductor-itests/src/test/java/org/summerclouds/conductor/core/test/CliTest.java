package de.mhus.conductor.core.test;

import org.junit.jupiter.api.Test;
import de.mhus.common.core.node.MProperties;
import de.mhus.common.core.tool.MFile;
import de.mhus.common.core.tool.MXml;
import de.mhus.common.junit.TestCase;
import de.mhus.conductor.core.MainCli;
import org.w3c.dom.Element;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CliTest extends TestCase {

    @Test
    public void testUsageNoConcatenate() throws Exception {

        File to = new File("target/scenario");
        if (to.exists()) MFile.deleteDir(to);
        to.mkdirs();

        File from = new File("../example2");
        MFile.copyDir(from, to);

        File root = new File(to, "sample-parent");

        String[] args = new String[] {
                "-vvv", "-d", root.getAbsolutePath(),
                "-inspect","all","newVersion",
                "-",
                "version2",
                "-",
                "newVersion"
        };

        MainCli.main(args);

        // test
        {
            Element pomE = MXml.loadXml(new File(to, "sample-parent/pom.xml")).getDocumentElement();
            String version = MXml.getValue(pomE, "/version", "");
            assertEquals("1.0.0", version);
            {
                String pVersion = MXml.getValue(pomE, "properties/sample-core.version", "");
                assertEquals("1.0.0", pVersion);
            }
            {
                String pVersion = MXml.getValue(pomE, "properties/sample-api.version", "");
                assertEquals("0.0.1", pVersion);
            }
        }
        {
            Element pomE = MXml.loadXml(new File(to, "sample-api/pom.xml")).getDocumentElement();
            String version = MXml.getValue(pomE, "/version", "");
            assertEquals("1.0.0-SNAPSHOT", version);
            String parent = MXml.getValue(pomE, "/parent/version", "");
            assertEquals("1.0.0-SNAPSHOT", parent);
        }
        {
            Element pomE = MXml.loadXml(new File(to, "sample-core/pom.xml")).getDocumentElement();
            String version = MXml.getValue(pomE, "/version", "");
            assertEquals("1.0.0", version);
            String parent = MXml.getValue(pomE, "/parent/version", "");
            assertEquals("1.0.0", parent);
        }
        {
            MProperties hist = MProperties.load(new File(to, "sample-parent/history.properties"));
            assertEquals("1.0.0", hist.getString("core"));
            assertEquals("1.0.0", hist.getString("parent"));
            assertEquals("0.0.1", hist.getString("api"));
        }

    }

    @Test
    public void testUsageConcatenate() throws Exception {

        File to = new File("target/scenario");
        if (to.exists()) MFile.deleteDir(to);
        to.mkdirs();

        File from = new File("../example2");
        MFile.copyDir(from, to);

        File root = new File(to, "sample-parent");

        String[] args = new String[] {
                "-vvv", "-co", "-d", root.getAbsolutePath(),
                "-inspect","all","newVersion",
                "-",
                "version2",
                "-",
                "newVersion"
                };

        MainCli.main(args);

        // test
        {
            Element pomE = MXml.loadXml(new File(to, "sample-parent/pom.xml")).getDocumentElement();
            String version = MXml.getValue(pomE, "/version", "");
            assertEquals("1.0.0", version);
            {
                String pVersion = MXml.getValue(pomE, "properties/sample-core.version", "");
                assertEquals("2.0.0", pVersion);
            }
            {
                String pVersion = MXml.getValue(pomE, "properties/sample-api.version", "");
                assertEquals("0.0.1", pVersion);
            }
        }
        {
            Element pomE = MXml.loadXml(new File(to, "sample-api/pom.xml")).getDocumentElement();
            String version = MXml.getValue(pomE, "/version", "");
            assertEquals("1.0.0-SNAPSHOT", version);
            String parent = MXml.getValue(pomE, "/parent/version", "");
            assertEquals("1.0.0-SNAPSHOT", parent);
        }
        {
            Element pomE = MXml.loadXml(new File(to, "sample-core/pom.xml")).getDocumentElement();
            String version = MXml.getValue(pomE, "/version", "");
            assertEquals("2.0.0", version);
            String parent = MXml.getValue(pomE, "/parent/version", "");
            assertEquals("1.0.0", parent);
        }
        {
            MProperties hist = MProperties.load(new File(to, "sample-parent/history.properties"));
            assertEquals("2.0.0", hist.getString("core"));
            assertEquals("1.0.0", hist.getString("parent"));
            assertEquals("0.0.1", hist.getString("api"));
        }

    }
}
