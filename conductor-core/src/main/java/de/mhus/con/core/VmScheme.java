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
package de.mhus.con.core;

import de.mhus.con.api.*;
import org.reflections.Reflections;
import org.summerclouds.common.core.error.NotFoundException;
import org.summerclouds.common.core.log.MLog;
import org.summerclouds.common.core.tool.MFile;
import org.summerclouds.common.core.util.MUri;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

@AScheme(name = "vm")
public class VmScheme extends MLog implements DirectLoadScheme {

    @Override
    public File load(Conductor con, MUri uri) throws IOException, NotFoundException {
        return null;
    }

    @Override
    public ConductorPlugin loadPlugin(MUri uri, String mojoName)
            throws NotFoundException, IOException {
        Object[] pack = ConUtil.getMainPackageName();
        log().t("Scan Package", pack);

        Reflections reflections = new Reflections(pack);

        for (Class<?> clazz : reflections.getTypesAnnotatedWith(AMojo.class)) {
            AMojo def = clazz.getAnnotation(AMojo.class);
            log().t("AMojo", clazz, def);
            if (def != null && def.name().equals(mojoName)) {
                try {
                    Object inst = clazz.getConstructor().newInstance();
                    return (ConductorPlugin) inst;
                } catch (InstantiationException
                        | IllegalAccessException
                        | IllegalArgumentException
                        | InvocationTargetException
                        | NoSuchMethodException
                        | SecurityException e) {
                    throw new IOException(e);
                }
            }
        }
        throw new NotFoundException("Plugin not found", "vm", mojoName);
    }

    @Override
    public String loadContent(MUri uri) {
        String content = MFile.readFile(getClass().getResourceAsStream(uri.getPath()));
        return content;
    }
}
