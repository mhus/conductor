package de.mhus.conductor.core.test;

import org.apache.commons.codec.Charsets;
import org.junit.jupiter.api.Test;
import de.mhus.commons.console.Console;
import de.mhus.commons.console.SimpleConsole;
import de.mhus.commons.io.YOutputStream;
import de.mhus.commons.io.YWriter;
import de.mhus.commons.tools.Collection;
import de.mhus.commons.tools.MFile;
import de.mhus.commons.tools.MString;
import de.mhus.common.junit.TestCase;
import de.mhus.conductor.core.MainCli;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class OrderTest extends TestCase {

    @Test
    public void testDependencies() throws Exception {

        File to = new File("target/scenario");
        if (to.exists()) MFile.deleteDir(to);
        to.mkdirs();

        File from = new File("../example4");
        MFile.copyDir(from, to);

        File root = new File(to, "sample-parent");

        // prepare console tee
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        YOutputStream yOutput = new YOutputStream(System.out, output);
        SimpleConsole testConsole = new SimpleConsole(System.in, new PrintStream(yOutput));
        Console.set(testConsole);
        try {
            String[] args = new String[]{"-vvv", "-d", root.getAbsolutePath(), "-inspect", "all", "dependencies"};

            MainCli.main(args);
        } finally {
            Console.resetConsole();
        }

        // grep result from output
        String content = new String(output.toByteArray(),  StandardCharsets.UTF_8);
        content = MString.afterIndex(content, "[1/1] [4/4] con.test >>>");
        assertFalse(content.isBlank());
        content = MString.afterIndex(content, "con.test");
        assertFalse(content.isBlank());
        content = MString.beforeIndex(content, "BUILD SUCCESS");
        assertFalse(content.isBlank());
        List<String> lines = MCollection.toList(MString.split(content, "\n"));

        //0 ................................................... SUCCESS
        //1    parent ................................................... SUCCESS
        //2    api ...................................................... SUCCESS
        //3    core ..................................................... SUCCESS
        //4    itest .................................................... SUCCESS
        //5
        //6------------------------------------------------------------------------
        //7

        assertEquals(8, lines.size());
        lines.remove(7);
        lines.remove(6);
        lines.remove(5);
        lines.remove(0);
        lines = lines.stream().map(v -> MString.beforeIndex(v, '.').trim() ).collect(Collectors.toList());

        assertEquals("parent", lines.get(0));
        assertEquals("api", lines.get(1));
        assertEquals("core", lines.get(2));
        assertEquals("itest", lines.get(3));
    }

    @Test
    public void testDependenciesDesc() throws Exception {

        File to = new File("target/scenario");
        if (to.exists()) MFile.deleteDir(to);
        to.mkdirs();

        File from = new File("../example4");
        MFile.copyDir(from, to);

        File root = new File(to, "sample-parent");

        // prepare console tee
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        YOutputStream yOutput = new YOutputStream(System.out, output);
        SimpleConsole testConsole = new SimpleConsole(System.in, new PrintStream(yOutput));
        Console.set(testConsole);
        try {
            String[] args = new String[]{"-vvv", "-d", root.getAbsolutePath(), "-inspect", "all", "dependencies_desc"};

            MainCli.main(args);
        } finally {
            Console.resetConsole();
        }

        // grep result from output
        String content = new String(output.toByteArray(),  StandardCharsets.UTF_8);
        content = MString.afterIndex(content, "[1/1] [4/4] con.test >>>");
        assertFalse(content.isBlank());
        content = MString.afterIndex(content, "con.test");
        assertFalse(content.isBlank());
        content = MString.beforeIndex(content, "BUILD SUCCESS");
        assertFalse(content.isBlank());
        List<String> lines = MCollection.toList(MString.split(content, "\n"));

        //0 ................................................... SUCCESS
        //1    itest .................................................... SUCCESS
        //2    core ..................................................... SUCCESS
        //3    api ...................................................... SUCCESS
        //4    parent ................................................... SUCCESS
        //5
        //6------------------------------------------------------------------------
        //7

        assertEquals(8, lines.size());
        lines.remove(7);
        lines.remove(6);
        lines.remove(5);
        lines.remove(0);
        lines = lines.stream().map(v -> MString.beforeIndex(v, '.').trim() ).collect(Collectors.toList());

        assertEquals("parent", lines.get(3));
        assertEquals("api", lines.get(2));
        assertEquals("core", lines.get(1));
        assertEquals("itest", lines.get(0));
    }
    @Test
    public void testIndexes() throws Exception {

        File to = new File("target/scenario");
        if (to.exists()) MFile.deleteDir(to);
        to.mkdirs();

        File from = new File("../example4");
        MFile.copyDir(from, to);

        File root = new File(to, "sample-parent");

        // prepare console tee
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        YOutputStream yOutput = new YOutputStream(System.out, output);
        SimpleConsole testConsole = new SimpleConsole(System.in, new PrintStream(yOutput));
        Console.set(testConsole);
        try {
            String[] args = new String[]{"-vvv", "-d", root.getAbsolutePath(), "-inspect", "all", "indexes"};

            MainCli.main(args);
        } finally {
            Console.resetConsole();
        }

        // grep result from output
        String content = new String(output.toByteArray(),  StandardCharsets.UTF_8);
        content = MString.afterIndex(content, "[1/1] [4/4] con.test >>>");
        assertFalse(content.isBlank());
        content = MString.afterIndex(content, "con.test");
        assertFalse(content.isBlank());
        content = MString.beforeIndex(content, "BUILD SUCCESS");
        assertFalse(content.isBlank());
        List<String> lines = MCollection.toList(MString.split(content, "\n"));

        //0 ................................................... SUCCESS
        //1    parent ................................................... SUCCESS
        //2    core ..................................................... SUCCESS
        //3    api ...................................................... SUCCESS
        //4    itest .................................................... SUCCESS
        //5
        //6------------------------------------------------------------------------
        //7

        assertEquals(8, lines.size());
        lines.remove(7);
        lines.remove(6);
        lines.remove(5);
        lines.remove(0);
        lines = lines.stream().map(v -> MString.beforeIndex(v, '.').trim() ).collect(Collectors.toList());

        assertEquals("parent", lines.get(0));
        assertEquals("core", lines.get(1));
        assertEquals("api", lines.get(2));
        assertEquals("itest", lines.get(3));
    }

    @Test
    public void testIndexesDesc() throws Exception {

        File to = new File("target/scenario");
        if (to.exists()) MFile.deleteDir(to);
        to.mkdirs();

        File from = new File("../example4");
        MFile.copyDir(from, to);

        File root = new File(to, "sample-parent");

        // prepare console tee
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        YOutputStream yOutput = new YOutputStream(System.out, output);
        SimpleConsole testConsole = new SimpleConsole(System.in, new PrintStream(yOutput));
        Console.set(testConsole);
        try {
            String[] args = new String[]{"-vvv", "-d", root.getAbsolutePath(), "-inspect", "all", "indexes_desc"};

            MainCli.main(args);
        } finally {
            Console.resetConsole();
        }

        // grep result from output
        String content = new String(output.toByteArray(),  StandardCharsets.UTF_8);
        content = MString.afterIndex(content, "[1/1] [4/4] con.test >>>");
        assertFalse(content.isBlank());
        content = MString.afterIndex(content, "con.test");
        assertFalse(content.isBlank());
        content = MString.beforeIndex(content, "BUILD SUCCESS");
        assertFalse(content.isBlank());
        List<String> lines = MCollection.toList(MString.split(content, "\n"));

        //0 ................................................... SUCCESS
        //1    itest .................................................... SUCCESS
        //2    api ...................................................... SUCCESS
        //3    core ..................................................... SUCCESS
        //4    parent ................................................... SUCCESS
        //5
        //6------------------------------------------------------------------------
        //7

        assertEquals(8, lines.size());
        lines.remove(7);
        lines.remove(6);
        lines.remove(5);
        lines.remove(0);
        lines = lines.stream().map(v -> MString.beforeIndex(v, '.').trim() ).collect(Collectors.toList());

        assertEquals("parent", lines.get(3));
        assertEquals("core", lines.get(2));
        assertEquals("api", lines.get(1));
        assertEquals("itest", lines.get(0));
    }

    @Test
    public void testCombi() throws Exception {

        File to = new File("target/scenario");
        if (to.exists()) MFile.deleteDir(to);
        to.mkdirs();

        File from = new File("../example4");
        MFile.copyDir(from, to);

        File root = new File(to, "sample-parent");

        // prepare console tee
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        YOutputStream yOutput = new YOutputStream(System.out, output);
        SimpleConsole testConsole = new SimpleConsole(System.in, new PrintStream(yOutput));
        Console.set(testConsole);
        try {
            String[] args = new String[]{"-vvv", "-d", root.getAbsolutePath(), "-inspect", "all", "combi"};

            MainCli.main(args);
        } finally {
            Console.resetConsole();
        }

        // grep result from output
        String content = new String(output.toByteArray(),  StandardCharsets.UTF_8);
        content = MString.afterIndex(content, "[1/1] [4/4] con.test >>>");
        assertFalse(content.isBlank());
        content = MString.afterIndex(content, "con.test");
        assertFalse(content.isBlank());
        content = MString.beforeIndex(content, "BUILD SUCCESS");
        assertFalse(content.isBlank());
        List<String> lines = MCollection.toList(MString.split(content, "\n"));

        //0 ................................................... SUCCESS
        //1    itest .................................................... SUCCESS
        //2    parent ................................................... SUCCESS
        //3    core ..................................................... SUCCESS
        //4    api ...................................................... SUCCESS
        //5
        //6------------------------------------------------------------------------
        //7

        assertEquals(8, lines.size());
        lines.remove(7);
        lines.remove(6);
        lines.remove(5);
        lines.remove(0);
        lines = lines.stream().map(v -> MString.beforeIndex(v, '.').trim() ).collect(Collectors.toList());

        assertEquals("parent", lines.get(1));
        assertEquals("core", lines.get(2));
        assertEquals("api", lines.get(3));
        assertEquals("itest", lines.get(0));
    }
}
