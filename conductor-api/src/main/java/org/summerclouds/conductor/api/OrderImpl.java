package de.mhus.conductor.api;

import de.mhus.common.core.console.Console;
import de.mhus.common.core.tool.MValidator;

import java.util.List;

public class OrderImpl {

    public static enum ORDER_TYPE {DEPENDENCY, INDEX}

    private ORDER_TYPE type;

    private String name;
    private boolean orderAsc;

    public OrderImpl(Conductor con, String definition, List<Project> projects) {
        this.name = definition;

        orderAsc = true;
        if (name.endsWith(" desc")) {
            orderAsc = false;
            name = name.substring(0, name.length()-5);
        } else
        if (name.endsWith(" asc")) {
            name = name.substring(0, name.length()-4);
        }

        name = name.trim();
        type = ORDER_TYPE.INDEX;
        if (name.startsWith("@")) {
            name = name.substring(1);
            type = ORDER_TYPE.DEPENDENCY;
        } else
        if (name.startsWith("#")) {
            name = name.substring(1);
            type = ORDER_TYPE.INDEX;
        } else {
            final ORDER_TYPE[] newType = new ORDER_TYPE[1];
            newType[0] = ORDER_TYPE.DEPENDENCY;
            for (Project p : con.getProjects()) {
                String[] value = p.getLabels().getOrNull(name);
                if (value == null || value.length == 0) continue;
                if (MValidator.isInteger(value[0]))
                    newType[0] = ORDER_TYPE.INDEX;
                break;
            }
            type = newType[0];
            if (con.isVerboseOutput())
                Console.get().println("--- Found order type " + type + " for label " + name);
        }

    }

    public ORDER_TYPE getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public boolean isOrderAsc() {
        return orderAsc;
    }

}
