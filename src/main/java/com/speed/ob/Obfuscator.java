package com.speed.ob;

import com.speed.ob.api.ClassStore;
import com.speed.ob.api.ObfuscatorTransform;
import com.speed.ob.transforms.ClassNameTransform;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarInputStream;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * See LICENSE.txt for license info
 */
public class Obfuscator {

    private final List<Class<? extends ObfuscatorTransform>> transforms;
    private final ClassStore store;
    private final File out, in;
    private final Config config;


    public Obfuscator(final Config config) {
        transforms = new LinkedList<>();
        store = new ClassStore();
        this.config = config;
        //populate transforms
        if (config.getBoolean("Obfuscator.all_transforms")) {
            transforms.add(ClassNameTransform.class);
        } else {
            if (config.getBoolean("Obfuscator.classname_obfuscation")) {
                transforms.add(ClassNameTransform.class);
            }
            if (config.getBoolean("Obfuscator.controlflow_obfuscation")) {

            }
            if (config.getBoolean("Obfuscator.string_obfuscation")) {

            }
            if (config.getBoolean("Obfuscator.fieldname_transforms")) {

            }
            if (config.getBoolean("Obfuscator.methodname_transforms")) {

            }
        }
        String inputFile = config.get("Obfuscator.input");
        String outFile = config.get("Obfuscator.out_dir");
        out = new File(outFile);
        if (inputFile == null || inputFile.isEmpty()) {
            throw new RuntimeException("Input file not specified");
        } else {
            in = new File(inputFile);
            if (!in.exists()) {
                throw new RuntimeException("Input file not found");
            }
            if (in.isDirectory()) {
                try {
                    store.init(in.listFiles(), false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (in.getName().endsWith(".class")) {
                try {
                    store.init(new File[]{in}, false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (in.getName().endsWith(".jar")) {
                try {
                    JarInputStream in = new JarInputStream(new FileInputStream(this.in));
                    store.init(in, out);
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (!out.exists()) {
            if (!out.mkdir())
                throw new RuntimeException("Could not create output dir: " + out.getAbsolutePath());
        } else if (!out.isDirectory()) {
            throw new RuntimeException(out.getName() + " is not a directory, cannot output there");
        } else {
            if (!out.canWrite()) {
                throw new RuntimeException("Cannot write to output dir: " + out.getAbsolutePath());
            }
        }
    }

    private static Level parseLevel(String lvl) {
        if (lvl.equalsIgnoreCase("info")) {
            return Level.INFO;
        } else if (lvl.equalsIgnoreCase("warning")) {
            return Level.WARNING;
        } else if (lvl.equalsIgnoreCase("fine")) {
            return Level.FINE;
        } else if (lvl.equalsIgnoreCase("finer")) {
            return Level.FINER;
        } else if (lvl.equalsIgnoreCase("finest")) {
            return Level.FINEST;
        } else if (lvl.equalsIgnoreCase("all")) {
            return Level.ALL;
        } else if (lvl.equalsIgnoreCase("severe")) {
            return Level.SEVERE;
        } else if (lvl.equalsIgnoreCase("config")) {
            return Level.CONFIG;
        }
        return Level.INFO;
    }

    public void execute() {
        String logLvl = config.get("Obfuscate.logging");
        String logDir = config.get("Obfuscate.log_dir");
        Level level = parseLevel(logLvl);
        File logs = new File(logDir);
        if (!logs.exists()) {
            if (!logs.mkdir())
                Logger.getLogger(this.getClass().getName()).warning("Could not create logging directory");
        }
        FileHandler fHandler = null;
        try {
            if (out.exists())
                fHandler = new FileHandler(logs.getAbsolutePath() + File.separator + "ob%g.log");
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Class<? extends ObfuscatorTransform> clazz : transforms) {
            try {
                ObfuscatorTransform transform = clazz.newInstance();
                if (fHandler != null) {
                    transform.addLogHandler(fHandler);
                }
                transform.setLogLevel(level, false);
                transform.run(store, config);
                transform.results();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("a", "all", false, "enable all obfuscations");
        options.addOption("s", "strings", false, "enable string obfuscation");
        options.addOption("l", "lexical", false, "enable lexical obfuscation");
        options.addOption("c", "control-flow", false, "enable control flow obfuscation");
        options.addOption("C", "config", true, "use <arg> as a config file");
        options.addOption("f", "file", true, "obfuscate file <arg>");
        options.addOption("o", "out", true, "output obfuscated file(s) to directory <arg>");
        options.addOption("h", "help", false, "shows this help message and then exits");
        CommandLineParser parser = new GnuParser();
        HelpFormatter formatter = new HelpFormatter();
        try {
            CommandLine cmd = parser.parse(options, args);
            parse(cmd, parser, options, formatter);
        } catch (MissingArgumentException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("java com.speed.ob.Obfuscate", options, true);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private static void parse(CommandLine cmd, CommandLineParser parser, Options options, HelpFormatter formatter) {
        Config config = new Config();
        if (cmd.getArgs().length == 0 || cmd.hasOption('h')) {
            formatter.printHelp("java com.speed.ob.Obfuscate", options, true);
            return;
        }
        if (cmd.hasOption("C")) {
            try {
                Config oneTrueConfig = new Config(cmd.getOptionValue('C'));
                Obfuscator obfuscator = new Obfuscator(oneTrueConfig);
                obfuscator.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if (cmd.hasOption("f")) {
                config.set("Obfuscator.input", (cmd.getOptionValue('f')));
            }
            if (cmd.hasOption("a")) {
                config.set("Obfuscator.all_transforms", "true");
            } else {
                if (cmd.hasOption('s')) {
                    config.set("Obfuscator.string_obfuscation", "true");
                }
                if (cmd.hasOption('c')) {
                    config.set("Obfuscator.controlﬂow_obfuscation", "true");
                }
                if (cmd.hasOption('l')) {
                    config.set("Obfuscator.fieldname_transforms", "true");
                    config.set("Obfuscator.methodname_transforms", "true");
                    config.set("Obfuscator.classname_transforms", "true");
                }
            }
            if (cmd.hasOption('o')) {
                config.set("Obfuscator.out_dir", cmd.getOptionValue('o'));
            }
            Obfuscator obfuscator = new Obfuscator(config);
            obfuscator.execute();
        }
    }

}
