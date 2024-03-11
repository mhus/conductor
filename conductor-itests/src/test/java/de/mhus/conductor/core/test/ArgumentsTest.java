package de.mhus.conductor.core.test;

import org.junit.jupiter.api.Test;
import de.mhus.commons.tree.MProperties;
import de.mhus.common.junit.TestCase;
import de.mhus.conductor.api.ConUtil;
import de.mhus.conductor.core.ConductorImpl;
import de.mhus.conductor.core.ContextImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArgumentsTest extends TestCase {

    @Test
    public void testSimpleBash() {
        String os = System.getProperty("os.name");
        System.setProperty("os.name", "linux");
        try {
            ConductorImpl con = new ConductorImpl(new File("target"));
            ((MProperties) con.getProperties()).setString("0", "one");
            ((MProperties) con.getProperties()).setString("1", "two");
            ContextImpl context = new ContextImpl(con, 0);
            List<String> args = new ArrayList<>();
            args.add("zero");
            args.add("@");
            String res = ConUtil.escapeArgumentsForShell(context, args);
            System.out.println(res);
            assertEquals("zero one two", res);
        } finally {
            System.setProperty("os.name", os);
        }
    }

    @Test
    public void testSimpleEscapedBash() {
        String os = System.getProperty("os.name");
        System.setProperty("os.name", "linux");
        try {
            ConductorImpl con = new ConductorImpl(new File("target"));
            ((MProperties)con.getProperties()).setString("0", "one");
            ((MProperties)con.getProperties()).setString("1", "two");
            ContextImpl context = new ContextImpl(con, 0);
            List<String> args = new ArrayList<>();
            args.add("zero");
            args.add("\\@");
            String res = ConUtil.escapeArgumentsForShell(context, args);
            System.out.println(res);
            assertEquals("zero @", res);
        } finally {
            System.setProperty("os.name", os);
        }
    }

    @Test
    public void testEscapedArgumentsBash() {
        String os = System.getProperty("os.name");
        System.setProperty("os.name", "linux");
        try {
            ConductorImpl con = new ConductorImpl(new File("target"));
            ((MProperties)con.getProperties()).setString("0", "one two");
            ((MProperties)con.getProperties()).setString("1", "three\"four");
            ((MProperties)con.getProperties()).setString("2", "five\\six");
            ContextImpl context = new ContextImpl(con, 0);
            List<String> args = new ArrayList<>();
            args.add("zero");
            args.add("@");
            String res = ConUtil.escapeArgumentsForShell(context, args);
            System.out.println(res);
            assertEquals("zero 'one two' 'three\"four' 'five\\six'", res);
        } finally {
            System.setProperty("os.name", os);
        }
    }

    @Test
    public void testEscapedArgumentsAllBash() {
        String os = System.getProperty("os.name");
        System.setProperty("os.name", "linux");
        try {
            ConductorImpl con = new ConductorImpl(new File("target"));
            ((MProperties)con.getProperties()).setString("0", "\"");
            ((MProperties)con.getProperties()).setString("1", "'");
            ((MProperties)con.getProperties()).setString("2", "\\");
            ((MProperties)con.getProperties()).setString("3", "[");
            ((MProperties)con.getProperties()).setString("4", "]");
            ((MProperties)con.getProperties()).setString("5", "*");
            ((MProperties)con.getProperties()).setString("6", "$");
            ((MProperties)con.getProperties()).setString("7", "\t");
            ((MProperties)con.getProperties()).setString("8", " ");
            ((MProperties)con.getProperties()).setString("9", "\n");
            ((MProperties)con.getProperties()).setString("10", "#");
            ((MProperties)con.getProperties()).setString("11", "+");
            ContextImpl context = new ContextImpl(con, 0);
            List<String> args = new ArrayList<>();
            args.add("zero");
            args.add("@");
            String res = ConUtil.escapeArgumentsForShell(context, args);
            System.out.println(res);
            assertEquals("zero '\"' ''\\''' '\\' '[' ']' '*' '$' '\t' ' ' '\n' '#' '+'", res);
        } finally {
            System.setProperty("os.name", os);
        }
    }

    @Test
    public void testEscapedOptionsBash() {
        String os = System.getProperty("os.name");
        System.setProperty("os.name", "linux");
        try {
            ConductorImpl con = new ConductorImpl(new File("target"));
            ((MProperties)con.getProperties()).setString("0", "-one two");
            ((MProperties)con.getProperties()).setString("1", "-three\"four");
            ((MProperties)con.getProperties()).setString("2", "-five\\six");
            ContextImpl context = new ContextImpl(con, 0);
            List<String> args = new ArrayList<>();
            args.add("zero");
            args.add("@");
            String res = ConUtil.escapeArgumentsForShell(context, args);
            System.out.println(res);
            assertEquals("zero '-one two' '-three\"four' '-five\\six'", res);
        } finally {
            System.setProperty("os.name", os);
        }
    }

    @Test
    public void testEscapedGitCommitBash() {
        String os = System.getProperty("os.name");
        System.setProperty("os.name", "linux");
        try {
            ConductorImpl con = new ConductorImpl(new File("target"));
            ((MProperties)con.getProperties()).setString("0", "-m");
            ((MProperties)con.getProperties()).setString("1", "this is a message");
            ContextImpl context = new ContextImpl(con, 0);
            List<String> args = new ArrayList<>();
            args.add("commit");
            args.add("@");
            String res = ConUtil.escapeArgumentsForShell(context, args);
            System.out.println(res);
            assertEquals("commit -m 'this is a message'", res);
        } finally {
            System.setProperty("os.name", os);
        }
    }

    @Test
    public void testEscapedTickBash() {
        String os = System.getProperty("os.name");
        System.setProperty("os.name", "linux");
        try {
            ConductorImpl con = new ConductorImpl(new File("target"));
            ((MProperties)con.getProperties()).setString("0", "-m");
            ((MProperties)con.getProperties()).setString("1", "this is a ' tick");
            ContextImpl context = new ContextImpl(con, 0);
            List<String> args = new ArrayList<>();
            args.add("commit");
            args.add("@");
            String res = ConUtil.escapeArgumentsForShell(context, args);
            System.out.println(res);
            assertEquals("commit -m 'this is a '\\'' tick'", res);
        } finally {
            System.setProperty("os.name", os);
        }
    }

    // ---

    @Test
    public void testSimpleCmd() {
        String os = System.getProperty("os.name");
        System.setProperty("os.name", "Windows");
        try {
            ConductorImpl con = new ConductorImpl(new File("target"));
            ((MProperties) con.getProperties()).setString("0", "one");
            ((MProperties) con.getProperties()).setString("1", "two");
            ContextImpl context = new ContextImpl(con, 0);
            List<String> args = new ArrayList<>();
            args.add("zero");
            args.add("@");
            String res = ConUtil.escapeArgumentsForShell(context, args);
            System.out.println(res);
            assertEquals("zero one two", res);
        } finally {
            System.setProperty("os.name", os);
        }
    }

    @Test
    public void testSimpleEscapedCmd() {
        String os = System.getProperty("os.name");
        System.setProperty("os.name", "Windows");
        try {
            ConductorImpl con = new ConductorImpl(new File("target"));
            ((MProperties)con.getProperties()).setString("0", "one");
            ((MProperties)con.getProperties()).setString("1", "two");
            ContextImpl context = new ContextImpl(con, 0);
            List<String> args = new ArrayList<>();
            args.add("zero");
            args.add("\\@");
            String res = ConUtil.escapeArgumentsForShell(context, args);
            System.out.println(res);
            assertEquals("zero @", res);
        } finally {
            System.setProperty("os.name", os);
        }
    }

    @Test
    public void testEscapedArgumentsCmd() {
        String os = System.getProperty("os.name");
        System.setProperty("os.name", "Windows");
        try {
            ConductorImpl con = new ConductorImpl(new File("target"));
            ((MProperties)con.getProperties()).setString("0", "one two");
            ((MProperties)con.getProperties()).setString("1", "three\"four");
            ((MProperties)con.getProperties()).setString("2", "five\\six");
            ContextImpl context = new ContextImpl(con, 0);
            List<String> args = new ArrayList<>();
            args.add("zero");
            args.add("@");
            String res = ConUtil.escapeArgumentsForShell(context, args);
            System.out.println(res);
            assertEquals("zero \"one two\" \"three^\"four\" five\\six", res);
        } finally {
            System.setProperty("os.name", os);
        }
    }

    @Test
    public void testEscapedArgumentsAllCmd() {
        String os = System.getProperty("os.name");
        System.setProperty("os.name", "Windows");
        try {
            ConductorImpl con = new ConductorImpl(new File("target"));
            ((MProperties)con.getProperties()).setString("0", "\"");
            ((MProperties)con.getProperties()).setString("1", "'");
            ((MProperties)con.getProperties()).setString("2", "\\");
            ((MProperties)con.getProperties()).setString("3", "<");
            ((MProperties)con.getProperties()).setString("4", ">");
            ((MProperties)con.getProperties()).setString("5", "&");
            ((MProperties)con.getProperties()).setString("6", "$");
            ((MProperties)con.getProperties()).setString("7", "\t");
            ((MProperties)con.getProperties()).setString("8", " ");
            ((MProperties)con.getProperties()).setString("9", "\n");
            ((MProperties)con.getProperties()).setString("10", "#");
            ((MProperties)con.getProperties()).setString("11", "+");
            ((MProperties)con.getProperties()).setString("12", "^");
            ContextImpl context = new ContextImpl(con, 0);
            List<String> args = new ArrayList<>();
            args.add("zero");
            args.add("@");
            String res = ConUtil.escapeArgumentsForShell(context, args);
            System.out.println(res);
            assertEquals("zero \"^\"\" \"'\" \\ \"^<\" \"^>\" \"^&\" $ \"\t\" \" \" \"\n\" # + \"^^\"", res);
        } finally {
            System.setProperty("os.name", os);
        }
    }

    @Test
    public void testEscapedOptionsCmd() {
        String os = System.getProperty("os.name");
        System.setProperty("os.name", "Windows");
        try {
            ConductorImpl con = new ConductorImpl(new File("target"));
            ((MProperties)con.getProperties()).setString("0", "-one two");
            ((MProperties)con.getProperties()).setString("1", "-three\"four");
            ((MProperties)con.getProperties()).setString("2", "-five\\six");
            ContextImpl context = new ContextImpl(con, 0);
            List<String> args = new ArrayList<>();
            args.add("zero");
            args.add("@");
            String res = ConUtil.escapeArgumentsForShell(context, args);
            System.out.println(res);
            assertEquals("zero \"-one two\" \"-three^\"four\" -five\\six", res);
        } finally {
            System.setProperty("os.name", os);
        }
    }

    @Test
    public void testEscapedGitCommitCmd() {
        String os = System.getProperty("os.name");
        System.setProperty("os.name", "Windows");
        try {
            ConductorImpl con = new ConductorImpl(new File("target"));
            ((MProperties)con.getProperties()).setString("0", "-m");
            ((MProperties)con.getProperties()).setString("1", "this is a message");
            ContextImpl context = new ContextImpl(con, 0);
            List<String> args = new ArrayList<>();
            args.add("commit");
            args.add("@");
            String res = ConUtil.escapeArgumentsForShell(context, args);
            System.out.println(res);
            assertEquals("commit -m \"this is a message\"", res);
        } finally {
            System.setProperty("os.name", os);
        }
    }

    @Test
    public void testEscapedTickCmd() {
        String os = System.getProperty("os.name");
        System.setProperty("os.name", "Windows");
        try {
            ConductorImpl con = new ConductorImpl(new File("target"));
            ((MProperties)con.getProperties()).setString("0", "-m");
            ((MProperties)con.getProperties()).setString("1", "this is a ' tick");
            ContextImpl context = new ContextImpl(con, 0);
            List<String> args = new ArrayList<>();
            args.add("commit");
            args.add("@");
            String res = ConUtil.escapeArgumentsForShell(context, args);
            System.out.println(res);
            assertEquals("commit -m \"this is a ' tick\"", res);
        } finally {
            System.setProperty("os.name", os);
        }
    }
}
