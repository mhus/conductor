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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.mhus.con.core.MainCli;
import de.mhus.con.core.MainOptionConsole;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.console.Console;
import de.mhus.lib.core.console.SimpleConsole;
import de.mhus.lib.core.io.YOutputStream;
import de.mhus.lib.errors.MException;
import de.mhus.lib.tests.TestCase;

public class PluginsTest extends TestCase {

    private static MainCli cli;
    private static MainOptionConsole console;
    private static ByteArrayOutputStream out;
    private static PrintStream orgOut;

    @Test
    void testConfirm() throws MException {
        
        InputStream in = System.in;
        BufferedInputStream newIn = new BufferedInputStream(new ByteArrayInputStream("y\n".getBytes()));
        System.setIn(newIn);
        try {
            out.reset();
            console.execute(
                    "- title: Confirm\n"
                  + "  target: confirm\n"
                  + "  properties:\n"
                  + "    prompt: Choose\n"
                  + "    message: Message\n"
                  + "");
    
            String str = new String(out.toByteArray()).trim();
            System.out.println("out> " + str);
            
            String[] parts = str.split("------------------------------------------------------------------------");
            
            assertEquals(
                    "Message\n" + 
                    "Choose (y/n) y", parts[2].trim());
        } finally {
            System.setIn(in);
        }
    }
    
//    @Test
    void testLoop() throws MException {
        out.reset();
        console.execute(
                "- title: Loop\n"
              + "  target: loop\n"
              + "  properties:\n"
              + "    range: 1-6\n"
              + "    step: 2\n"
              + "  steps:\n"
              + "  - title: Step 1\n"
              + "    condition: ${#1} == 2\n"
              + "    target: con.test\n"
              + "    properties:\n"
              + "      message: Step 1\n"
              + "  - title: Step 2\n"
              + "    target: con.test\n"
              + "    properties:\n"
              + "      message: Step 2 ${index}\n"
              + "");

        String str = new String(out.toByteArray()).trim();
        System.out.println("out> " + str);
        
        String[] parts = str.split("  ---");
        
        assertEquals(
                "Test: Step 2 1.0", parts[1].split("\n")[1].trim());
        assertEquals(
                "Test: Step 2 3.0", parts[2].split("\n")[1].trim());
        assertEquals(
                "Test: Step 2 5.0", parts[3].split("\n")[1].trim());
    
    }
    
//    @Test
    void testSwitch() throws MException {
        out.reset();
        console.execute(
                "- title: Switch\n"
              + "  target: switch\n"
              + "  steps:\n"
              + "  - title: Step 1\n"
              + "    condition: ${#1} == 2\n"
              + "    target: con.test\n"
              + "    properties:\n"
              + "      message: Step 1\n"
              + "  - title: Step 2\n"
              + "    target: con.test\n"
              + "    properties:\n"
              + "      message: Step 2\n"
              + "");

        String str = new String(out.toByteArray()).trim();
        System.out.println("out> " + str);
        
        String[] parts = str.split("------------------------------------------------------------------------");
        
        assertEquals(
                "Test: Step 2", parts[4].trim());
    }

//    @Test
    void testExecute() throws MException {
        out.reset();
        console.execute(
                "- title: Execute\n"
              + "  target: execute\n"
              + "  steps:\n"
              + "  - title: Step 1\n"
              + "    target: con.test\n"
              + "    properties:\n"
              + "      message: Step 1\n"
              + "  - title: Step 2\n"
              + "    target: con.test\n"
              + "    properties:\n"
              + "      message: Step 2\n"
              + "");

        String str = new String(out.toByteArray()).trim();
        System.out.println("out> " + str);
        
        String[] parts = str.split("  ---");
        
        assertEquals(
                "Test: Step 1", parts[1].split("\n")[1].trim());
        assertEquals(
                "Test: Step 2", parts[2].split("\n")[1].trim());
    }
    
//    @Test
    void testGitVersion() throws MException {
        out.reset();
        console.execute("print xxx${git.version()}xxx");
        String str = new String(out.toByteArray()).trim();
        System.out.println("version> " + str);
        assertTrue(str.length() > 0);
        str = str.split("xxx")[1].trim();
        assertEquals(2, MString.countCharacters(str, '.'));
    }
    
//    @Test
    void testMavenVersion() throws MException {
        {
            out.reset();
            console.execute("print xxx${maven.version()}xxx");
            String str = new String(out.toByteArray()).trim();
            System.out.println("version> " + str);
            assertTrue(str.length() > 0);
            str = str.split("xxx")[1].trim();
            assertEquals(2, MString.countCharacters(str, '.'));
        }
        {
            out.reset();
            console.execute("print ${maven.version(os.name)}");
            String str = new String(out.toByteArray()).trim();
            System.out.println("os.name> " + str);
            assertTrue(str.length() > 0);
        }
        {
            out.reset();
            console.execute("print ${maven.version(os.arch)}");
            String str = new String(out.toByteArray()).trim();
            System.out.println("os.arch> " + str);
            assertTrue(str.length() > 0);
        }
        {
            out.reset();
            console.execute("print ${maven.version(os.version)}");
            String str = new String(out.toByteArray()).trim();
            System.out.println("os.version> " + str);
            assertTrue(str.length() > 0);
        }
    }

    @BeforeAll
    public static void init() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException {
        out = new ByteArrayOutputStream();
        YOutputStream swt = new YOutputStream(System.out, out);
        orgOut = System.out;
        System.setOut(new PrintStream(swt));
        
        Console.resetConsole();
        Console.set(new SimpleConsole());
        cli = new MainCli();
        console = new MainOptionConsole();
        console.init(cli);
    }
    
    @AfterAll
    public static void deinit() {
        System.setOut(orgOut);
        Console.resetConsole();
    }
    
}
