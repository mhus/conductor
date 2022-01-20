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
package de.mhus.con.api;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import de.mhus.conductor.api.meta.Version;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.MSystem.ExecuteControl;
import de.mhus.lib.core.MThread;
import de.mhus.lib.core.MValidator;
import de.mhus.lib.core.console.Console;
import de.mhus.lib.core.console.Console.COLOR;
import de.mhus.lib.core.logging.Log;
import de.mhus.lib.core.logging.Log.LEVEL;
import de.mhus.lib.core.util.MUri;
import de.mhus.lib.errors.NotFoundException;

public class ConUtil {

    public static final Object consoleLock = new Object();
    private static final Log log = Log.getLog(Conductor.class);
    public static final String PROPERTY_FAE = "conductor.fae";
    public static final String PROPERTY_CMD_PATH = "conductor.cmd.";
    public static final String PROPERTY_PATH = "conductor.path";
    public static final String DEFAULT_PATHES_UNIX = "/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin";
    public static final String DEFAULT_PATHES_WINDOWS =
            "C:\\Program Files;C:\\Winnt;C:\\Winnt\\System32";
    public static final String PROPERTY_VERSION = "conductor.version";
    public static final String PROPERTY_LIFECYCLE = "conductor.lifecycle";
    public static final String PROPERTY_DOWNLOAD_SNAPSHOTS = "conductor.downloadSnapshots";
    public static final String ENV_HOME = "CONDUCTOR_HOME";
    public static final String ENV_HOME_DEFAULT = ".conductor";
    public static final String PROPERTY_ROOT = "conductor.root";
    public static final String PROPERTY_HOME = "conductor.home";
    // private static Console console;
    public static final String PROPERTY_VALIDATORS = "conductor.validators";
    public static final String PROPERTY_PARALLEL = "conductor.parallel";
    public static final String PROPERTY_THREADS = "conductor.threads";
    private static final Object[] SCAN_PACKAGES = new Object[] {"de.mhus.con"};
    public static final String PROPERTY_STEP_IGNORE_RETURN_CODE = "step.ignoreReturnCode";
    public static final String PROPERTY_Y = "conductor.confirm.confirm";
    public static final String PROPERTY_CONFIRM_STEPS = "conductor.confirm.steps";
    public static final String PROPERTY_CONFIRM_CMDS = "conductor.confirm.cmds";
    public static final String PROPERTY_CONFIRM_BEEP = "conductor.confirm.beep";
    public static final String PROPERTY_VERBOSE = "conductor.verbose";

    public static void orderProjects(List<Project> projects, String order, boolean orderAsc) {
        projects.sort(
                new Comparator<Project>() {

                    @Override
                    public int compare(Project o1, Project o2) {
                        int ret =
                                compare(
                                        o1.getLabels().getOrNull(order),
                                        o2.getLabels().getOrNull(order));
                        if (!orderAsc) ret = ret * -1;
                        return ret;
                    }

                    private int compare(String[] o1, String[] o2) {
                        if (o1 == null && o2 == null) return 0;
                        if (o1 == null || o1.length == 0) return -1;
                        if (o2 == null || o2.length == 0) return 1;
                        if (MValidator.isNumber(o1[0]) && MValidator.isNumber(o2[0]))
                            return Double.compare(MCast.todouble(o1[0], 0), MCast.todouble(o2[0], 0));
                        return o1[0].compareTo(o2[0]);
                    }
                });
    }

    public static String[] execute(Conductor con, String name, File rootDir, String cmd, boolean infoOut)
            throws IOException {

        if (con != null && con.getProperties().getBoolean(PROPERTY_CONFIRM_CMDS, false)) {
            if (!confirmAction(con, null, null, "Press ENTER to execute " + cmd))
                return new String[] {"","","0"};
        }
        
        log.i(name, "execute", cmd, rootDir);

        final String shortName = MString.truncateNice(name, 40, 15);
        final Console console = getConsole();
        final boolean output = infoOut || log.isLevelEnabled(LEVEL.DEBUG);

        
        final StringBuilder stdOutBuilder = new StringBuilder();
        final StringBuilder stdErrBuilder = new StringBuilder();

        int exitCode =
                MSystem.execute(
                        shortName,
                        rootDir,
                        cmd,
                        new ExecuteControl() {

                            @Override
                            public void stdin(PrintWriter writer) {}

                            @Override
                            public void stdout(String line) {
                                if (output)
                                    synchronized (consoleLock) {
                                        console.print("[");
                                        console.setColor(COLOR.GREEN, null);
                                        console.print(shortName);
                                        console.cleanup();
                                        console.print("] ");
                                        console.println(line);
                                        console.flush();
                                    }
                                if (stdOutBuilder.length() > 0) stdOutBuilder.append("\n");
                                stdOutBuilder.append(line);
                            }

                            @Override
                            public void stderr(String line) {
                                if (output)
                                    synchronized (consoleLock) {
                                        console.print("[");
                                        console.setColor(COLOR.RED, null);
                                        console.print(shortName);
                                        console.cleanup();
                                        console.print("] ");
                                        console.println(line);
                                        console.flush();
                                    }
                                if (stdErrBuilder.length() > 0) stdErrBuilder.append("\n");
                                stdErrBuilder.append(line);
                            }
                        });

        String stderr = stdErrBuilder.toString();
        String stdout = stdOutBuilder.toString();
        log.i(name, "exitCode", exitCode);
        return new String[] {stdout, stderr, String.valueOf(exitCode)};
    }

    public static boolean confirmAction(Conductor con, Step step, List<Project> projects, String msg) {
        final Console console = getConsole();
        console.cleanup();
        console.setColor(COLOR.BRIGHT_RED, COLOR.BRIGHT_BLACK);
        console.println("================================================");
        console.println(" ENTER - run, c - cancel, s - skip, i - inspect ");
        console.println(" x - run and disable configrmation mode");
        console.println("================================================");
        console.cleanup();
        console.print(msg);
        console.flush();
        if (con != null && con.getProperties().getBoolean(PROPERTY_CONFIRM_BEEP, false))
            for (int i = 0; i < 3; i++) {
                console.beep();
                MThread.sleep(200);
            }
        while (true) {
            int c = console.read();
            if (c == '\n') {
                console.println();
                return true;
            }
            if (c == 'x') {
                console.println();
                if (con != null) {
                    ((MProperties)con.getProperties()).setBoolean(ConUtil.PROPERTY_CONFIRM_STEPS, false);
                    ((MProperties)con.getProperties()).setBoolean(ConUtil.PROPERTY_CONFIRM_CMDS, false);
                }
                return true;
            }
            if (c == 'i') {
                console.println();
                if (projects != null) {
                    for (Project project : projects) {
                        System.out.println("- Project " + project.getName());
                        System.out.println("    Directory : " + project.getRootDir());
                        System.out.println("    Path      : " + project.getPath());
                        System.out.println("    Labels    : " + project.getLabels());
                        System.out.println("    Properties: " + project.getProperties());
                    }
                }
                if (step != null) {
                    System.out.println("- Step " + step.getTitle());
                    System.out.println("    Target    : " + step.getTarget());
                    System.out.println("    Condition : " + step.getCondition());
                    System.out.println("    Selector  : " + step.getSelector());
                    System.out.println(
                            "    Order     : "
                                    + step.getSortBy()
                                    + " "
                                    + (step.isOrderAsc() ? "ASC" : "DESC"));
                    System.out.println("    Properties: " + step.getProperties());
                }
            }
            if (c == 's') {
                console.println();
                return false;
            }
            if (c == 'c') {
                console.println();
                throw new StopLifecycleException(null, "Canceled by user");
            }
        }
    }

    public static Console getConsole() {
        Console ret = Console.get();
        log.t("Console", ret.getClass());
        return ret;
        //	    if (console == null) {
        //	        String term = System.getenv("TERM");
        //            if (term != null) {
        //                term = term.toLowerCase();
        //                if (term.indexOf("xterm") >= 0) {
        //                    try {
        //                        console = new XTermConsole() {
        //                            @Override
        //                            public boolean isSupportColor() {
        //                                return true;
        //                            }
        //
        //                        };
        //                    } catch (IOException e) {
        //                        // TODO Auto-generated catch block
        //                        e.printStackTrace();
        //                    }
        //                }
        //            }
        //            if (console == null) console = Console.get();
        //            Console.set(console);
        //	    }
        //        return console;
    }

    public static String cmdLocationOrNull(Conductor con, String cmd) {
        try {
            return cmdLocation(con, cmd);
        } catch (NotFoundException e) {
            return null;
        }
    }

    // TODO cache findings
    public static String cmdLocation(Conductor con, String cmd) throws NotFoundException {
        if (con != null) {
            // check direct configuration
            String path =
                    con.getProperties()
                            .getString(ConUtil.PROPERTY_CMD_PATH + cmd.toUpperCase(), null);
            if (path != null) return path;
        }
        String[] pathes = null;
        String systemPath = System.getenv("PATH");
        if (MSystem.isWindows())
            pathes =
                    con.getProperties()
                            .getString(
                                    ConUtil.PROPERTY_PATH,
                                    DEFAULT_PATHES_WINDOWS
                                            + (systemPath == null ? "" : ";" + systemPath))
                            .split(";");
        else
            pathes =
                    con.getProperties()
                            .getString(
                                    ConUtil.PROPERTY_PATH,
                                    DEFAULT_PATHES_UNIX
                                            + (systemPath == null ? "" : ":" + systemPath))
                            .split(":");

        for (String path : pathes) {
            File file = new File(path + File.separator + cmd);
            if (file.exists() && file.isFile() && file.canExecute() && file.canRead())
                return file.getAbsolutePath();
        }
        throw new NotFoundException("Command not found", cmd);
    }

    public static MUri getDefaultConfiguration(String name) {
        String ext = MString.afterLastIndex(name, '.');
        name = MString.beforeLastIndex(name, '.');
        MUri uri =
                MUri.toUri(
                        "mvn:de.mhus.conductor/conductor-plugin/"
                                + Version.VERSION
                                + "/"
                                + ext
                                + "/"
                                + name);
        return uri;
    }

    public static File getFile(File root, String path) {
        File f = new File(path);
        if (!f.isAbsolute()) f = new File(root, path);
        return f;
    }

    public static File getHome() {
        String home = System.getenv(ENV_HOME);
        if (MString.isEmpty(home))
            return new File(MSystem.getUserHome(), ENV_HOME_DEFAULT).getAbsoluteFile();
        return new File(home);
    }

    public static File createTempFile(Conductor con, Class<?> owner, String suffix)
            throws IOException {
        File tmp = new File(getHome(), "tmp");
        if (tmp.exists() && tmp.isDirectory()) {
            File file =
                    new File(tmp, owner.getSimpleName() + "-" + UUID.randomUUID() + "." + suffix);
            file.deleteOnExit();
            return file;
        }
        File file = File.createTempFile(owner.getSimpleName(), suffix);
        file.deleteOnExit();
        return file;
    }

    public static Object[] getMainPackageName() {
        return SCAN_PACKAGES;
    }
}
