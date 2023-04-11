package org.summerclouds.conductor.core.test;

import org.junit.jupiter.api.Test;
import org.summerclouds.common.core.node.MProperties;
import org.summerclouds.common.junit.TestCase;
import org.summerclouds.conductor.api.ConUtil;
import org.summerclouds.conductor.core.ConductorImpl;
import org.summerclouds.conductor.core.ContextImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArgumentsTest extends TestCase {

    @Test
    public void testSimple() {
        ConductorImpl con = new ConductorImpl(new File("target"));
        ((MProperties)con.getProperties()).setString("0", "one");
        ((MProperties)con.getProperties()).setString("1", "two");
        ContextImpl context = new ContextImpl(con, 0);
        List<String> args = new ArrayList<>();
        args.add("zero");
        args.add("@");
        String res = ConUtil.escapeArgumentsForShell(context, args);
        System.out.println(res);
        assertEquals("zero one two", res);
    }

    @Test
    public void testSimpleEscaped() {
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
    }

    @Test
    public void testEscapedArguments() {
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
//        assertEquals("zero one\\ two three\\\"four five\\\\six", res);
        assertEquals("zero 'one two' 'three\"four' 'five\\six'", res);
    }

    @Test
    public void testEscapedArgumentsAll() {
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
//        assertEquals("zero one\\ two three\\\"four five\\\\six", res);
        assertEquals("zero '\"' ''\\''' '\\' '[' ']' '*' '$' '\t' ' ' '\n' '#' '+'", res);
    }

    @Test
    public void testEscapedOptions() {
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
//        assertEquals("zero -one\\ two -three\"four -five\\\\six", res);
        assertEquals("zero '-one two' '-three\"four' '-five\\six'", res);
    }

    @Test
    public void testEscapedGitCommit() {
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
    }

    @Test
    public void testEscapedTick() {
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
    }

}
