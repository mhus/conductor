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
package org.summerclouds.conductor.core;

import de.mhus.conductor.api.meta.Version;
import org.reflections.Reflections;
import org.summerclouds.common.core.error.MException;
import org.summerclouds.common.core.error.NotFoundException;
import org.summerclouds.common.core.log.MLog;
import org.summerclouds.common.core.node.MProperties;
import org.summerclouds.common.core.tool.MString;
import org.summerclouds.conductor.api.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

public class MainCli extends MLog implements Cli {

    protected Map<String, MainOptionHandler> optionHandlers = new HashMap<>();
    protected Map<String, Scheme> schemes = new HashMap<>();
    protected Map<String, ConfigType> configTypes = new HashMap<>();
    protected Map<String, Validator> validators = new HashMap<>();
    protected Map<String, Plugin> defaultPlugins = new HashMap<>();
    
    protected File rootDir = new File(".");
    protected ConductorImpl con;
    protected String configFile;
    private MProperties overlayProperties = new MProperties();
    public LinkedList<String> defaultImports = new LinkedList<>();

    public static void main(String[] args) throws Exception {

        ConUtil.getConsole();

        if (args == null || args.length == 0) {
            System.out.println("Try -help");
            return;
        }

        LinkedList<String> queue = new LinkedList<>();
        for (String arg : args) queue.add(arg);

        new MainCli().execute(queue);
    }

    public MainCli()
            throws ClassNotFoundException, IOException, InstantiationException,
                    IllegalAccessException, IllegalArgumentException, InvocationTargetException,
                    NoSuchMethodException, SecurityException {
        Object[] pack = ConUtil.getMainPackageName();
        log().t("Scan Package", pack);

        Reflections reflections = new Reflections(pack);

        for (Class<?> clazz : reflections.getTypesAnnotatedWith(AOption.class)) {
            AOption def = clazz.getAnnotation(AOption.class);
            log().t("AOption", clazz, def);
            if (def != null) {
                Object inst = clazz.getConstructor().newInstance();
                for (String alias : def.alias()) {
                    MainOptionHandler old = optionHandlers.put(alias, (MainOptionHandler) inst);
                    if (old != null) log().w("Overwrite", alias, old.getClass(), clazz);
                }
            }
        }
        log().t("optionHandlers", optionHandlers);

        for (Class<?> clazz : reflections.getTypesAnnotatedWith(AScheme.class)) {
            AScheme def = clazz.getAnnotation(AScheme.class);
            log().t("AScheme", clazz, def);
            if (def != null) {
                Object inst = clazz.getConstructor().newInstance();
                for (String alias : def.name()) {
                    Scheme old = schemes.put(alias, (Scheme) inst);
                    if (old != null) log().w("Overwrite", alias, old.getClass(), clazz);
                }
            }
        }
        log().t("schemes",schemes);

        for (Class<?> clazz : reflections.getTypesAnnotatedWith(AConfigType.class)) {
            AConfigType def = clazz.getAnnotation(AConfigType.class);
            log().t("AConfigType", clazz, def);
            if (def != null) {
                Object inst = clazz.getConstructor().newInstance();
                for (String alias : def.name()) {
                    ConfigType old = configTypes.put(alias, (ConfigType) inst);
                    if (old != null) log().w("Overwrite", alias, old.getClass(), clazz);
                }
            }
        }
        log().t("configTypes",configTypes);

        for (Class<?> clazz : reflections.getTypesAnnotatedWith(AValidator.class)) {
            AValidator def = clazz.getAnnotation(AValidator.class);
            log().t("AValidator", clazz, def);
            if (def != null) {
                Object inst = clazz.getConstructor().newInstance();
                for (String alias : def.name()) {
                    Validator old = validators.put(alias, (Validator) inst);
                    if (old != null) log().w("Overwrite", alias, old.getClass(), clazz);
                }
            }
        }
        log().t("validators",validators);

        for (Class<?> clazz : reflections.getTypesAnnotatedWith(AMojo.class)) {
            AMojo def = clazz.getAnnotation(AMojo.class);
            log().t("AMojo", clazz, def);
            if (def != null) {
                if (MString.isSet(def.target())) {
                    PluginImpl impl = new PluginImpl();
                    impl.mojo = def.name();
                    impl.scope = def.scope();
                    impl.target = def.target();
                    impl.uri = "vm:" + def.name();
                    defaultPlugins.put(def.target(), impl);
                }
            }
        }
        log().t("defaultPlugins",defaultPlugins);
        
    }

    protected void execute(LinkedList<String> queue) throws MException {

        MProperties execProperties = null;
        String execLifecycle = null;
        int directPropIndex = 0;

        // prepare
        execProperties = new MProperties();
        while (queue.size() > 0) {
            String next = queue.removeFirst();
            if (execLifecycle == null) {
                if (next.startsWith("-")) {
                    // option
                    executeOption(next, queue);
                } else if (next.contains("=")) {
                    // propertiy
                    execProperties.put(
                        MString.beforeIndex(next, '=').trim(), MString.afterIndex(next, '='));
                } else {
                    // lifecycle name
                    execLifecycle = next;
                }
            } else {
                if (overlayProperties.getBoolean(ConUtil.PROPERTY_CONCATENATE, false) &&
                (next.equals("-") || next.equals("--") ) )  {
                    // concatenated lifecycle, execute and reset
                    prepareDirectPropAll(execProperties, directPropIndex);
                    executeLifecycle(execLifecycle, execProperties);
                    execLifecycle = null;
                    execProperties = new MProperties();
                    directPropIndex = 0;
                    if (next.equals("--")) {
                        executeOption("--", null);
                    }
                } else {
                    // argument
                    execProperties.put(String.valueOf(directPropIndex), next);
                    directPropIndex++;
                }
            }
        }

        // execute last lifecycle - not concatenated
        if (execLifecycle != null) {
            prepareDirectPropAll(execProperties, directPropIndex);
            executeLifecycle(execLifecycle, execProperties);
            execLifecycle = null;
            execProperties = null;
            directPropIndex=0;
        }

        resetCon();
    }

    private void prepareDirectPropAll(MProperties execProperties, int directPropIndex) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < directPropIndex; i++) {
            if (buf.length() > 0) buf.append(" ");
            buf.append(execProperties.get(String.valueOf(i)));
        }
        execProperties.put("@", buf.toString());
    }

    private void executeOption(String next, LinkedList<String> queue) throws NotFoundException {
        MainOptionHandler handler = optionHandlers.get(next);
        if (handler == null) throw new NotFoundException("option", next);
        handler.execute(this, next, queue);
    }

    private void executeLifecycle(String execLifecycle, MProperties execProperties)
            throws MException {

        createConductor();

        ((MProperties) con.getProperties()).putReadProperties(execProperties);
        ExecutorImpl executor = new ExecutorImpl();

        executor.execute(con, execLifecycle);
    }

    private void createConductor() throws MException {
        if (con != null) return;
        log().d("Create conductor object");
        ConfiguratorImpl config = new ConfiguratorImpl();

        for (Entry<String, Scheme> entry : schemes.entrySet())
            ((SchemesImpl) config.getSchemes()).put(entry.getKey(), entry.getValue());
        for (Entry<String, ConfigType> entry : configTypes.entrySet())
            ((ConfigTypesImpl) config.getTypes()).put(entry.getKey(), entry.getValue());
        for (Entry<String, Validator> entry : validators.entrySet())
            config.getValidators().put(entry.getKey(), entry.getValue());

        if (configFile == null) {
            // set default
            configFile = findDefaultFile(rootDir);
        }
        URI uri = URI.create(configFile);

        con = new ConductorImpl(rootDir);
        
        defaultPlugins.values().forEach(i -> ((PluginImpl)i).con = con);
        con.plugins.collection.putAll(defaultPlugins);

        config.configure(uri, con, overlayProperties, defaultImports);
    }

    public static String findDefaultFile(File rootDir) {
        // set default
        File file = new File(rootDir, "conductor.yml");
        String config = null;
        if (file.exists() && file.isFile()) config = "file:conductor.yml";
        else
            config =
                    "mvn:" + MString.beforeLastIndex(MString.beforeLastIndex(MainCli.class.getPackageName(), '.'), '.') + "/conductor-plugin/"
                            + Version.VERSION
                            + "/yml/configuration-default";
        return config;
    }

    @Override
    public Map<String, MainOptionHandler> getOptions() {
        return optionHandlers;
    }

    public void resetCon() {
        if (con == null) return;
        con.close();
        con = null;
        overlayProperties.clear();
        defaultImports.clear();
    }

    @Override
    public Map<String, Scheme> getSchemes() {
        return schemes;
    }

    @Override
    public Map<String, ConfigType> getConfigTypes() {
        return configTypes;
    }

    @Override
    public Map<String, Validator> getValidators() {
        return validators;
    }

    @Override
    public Conductor getConductor() {
        try {
            createConductor();
        } catch (MException e) {
            log().w(e);
        }
        return con;
    }

    public MProperties getOverlayProperties() {
        return overlayProperties;
    }

    public boolean isConductor() {
        return con != null;
    }
    
}
