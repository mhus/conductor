package org.summerclouds.conductor.core;

import org.summerclouds.conductor.api.AOption;
import org.summerclouds.conductor.api.Cli;
import org.summerclouds.conductor.api.Lifecycle;
import org.summerclouds.conductor.api.MainOptionHandler;

import java.util.LinkedList;

@AOption(alias = "-desc")
public class MainOptionDescribe implements MainOptionHandler {

    @Override
    public void execute(Cli cli, String cmd, LinkedList<String> queue) {
        String name = queue.removeFirst();
        Lifecycle lf = cli.getConductor().getLifecycles().get(name);
        System.out.println("--------------------------------------");
        System.out.println("Lifecycle: " + lf.getName());
        System.out.println(lf.getDescription());
        for (String arg : lf.getUsage())
            System.out.println("- " + arg);
        System.out.println("--------------------------------------");
    }

    @Override
    public String getUsage(String cmd) {
        return "<lifecycle>";
    }

    @Override
    public String getDescription(String cmd) {
        return "Print lifecycle description";
    }

}
