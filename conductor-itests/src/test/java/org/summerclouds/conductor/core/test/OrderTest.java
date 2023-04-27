package org.summerclouds.conductor.core.test;

import org.junit.jupiter.api.Test;
import org.summerclouds.common.core.tool.MFile;
import org.summerclouds.common.junit.TestCase;
import org.summerclouds.conductor.core.MainCli;

import java.io.File;

public class OrderTest extends TestCase {

    @Test
    public void testDependencies() throws Exception {

        File to = new File("target/scenario");
        if (to.exists()) MFile.deleteDir(to);
        to.mkdirs();

        File from = new File("../example4");
        MFile.copyDir(from, to);

        File root = new File(to, "sample-parent");

        String[] args = new String[] {"-vvv", "-d", root.getAbsolutePath(), "-inspect","all","dependencies"};

        MainCli.main(args);

    }
}
