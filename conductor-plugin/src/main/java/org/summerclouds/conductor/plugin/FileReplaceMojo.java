package de.mhus.conductor.plugin;

import de.mhus.common.core.error.MException;
import de.mhus.common.core.log.MLog;
import de.mhus.common.core.tool.MFile;
import de.mhus.conductor.api.AMojo;
import de.mhus.conductor.api.Context;
import de.mhus.conductor.api.ExecutePlugin;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

@AMojo(name = "file.replace", target = "file.replace")
public class FileReplaceMojo extends MLog implements ExecutePlugin  {
    @Override
    public boolean execute(Context context) throws Exception {
        Set<File> files = findFiles(context);
        boolean found = false;
        for (File file : files)
            if (replaceInFile(context, file))
                found = true;
        return found;
    }

    private boolean replaceInFile(Context context, File file) {
        if (!file.exists() || !file.isFile())
            return false;
        log().d("Replace in file {}", file);
        var content = MFile.readFile(file);
        var newContent = content;
        boolean found = false;
        for (String key : context.getStep().getProperties().keys()) {
            if (key.startsWith("regex_")) {
                found = true;
                var regex = context.getStep().getProperties().getString(key,"");
                var replace = context.getStep().getProperties().getString("replace_" + key.substring(6),"");
                newContent = newContent.replaceAll(regex, replace);
            }
        }
        if (!found) {
            for (String key : context.getProperties().keys()) {
                if (key.startsWith("replace.regex_")) {
                    found = true;
                    var regex = context.getProperties().getString(key,"");
                    var replace = context.getProperties().getString("replace.replace_" + key.substring(14),"");
                    newContent = newContent.replaceAll(regex, replace);
                }
            }
        }
        if (!content.equals(newContent)) {
            log().i("Save {}", file);
            MFile.writeFile(file, newContent);
            return true;
        }
        return false;
    }

    private Set<File> findFiles(Context context) {
        Set<File> list = new HashSet<>();
        for (String start : context.getStep().getProperties().getString("start", ".").split(":"))
            findFiles(context, list, new File(context.getProject().getRootDir(), start));
        return list;
    }

    private void findFiles(Context context, Set<File> list, File start) {
        if (!start.exists() || !start.isDirectory())
            return;
        if (context.getStep().getProperties().isProperty("files")) {
            for (String path : context.getStep().getProperties().getString("files", "").split(":"))
                list.add(new File(start, path));
            return;
        }
        // TODO more options
    }
}
