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
import java.util.logging.Handler;
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
    private final Logger LOGGER;
    private final Level level;
    private FileHandler fHandler;


    public Obfuscator(final Config config) {
        transforms = new LinkedList<>();
        store = new ClassStore();
        this.config = config;
        //set up logging
        this.LOGGER = Logger.getLogger(this.getClass().getName());
        LOGGER.info("Ob2 is starting");
        String logLvl = config.get("Obfuscator.logging");
        String logDir = config.get("Obfuscator.log_dir");
        level = parseLevel(logLvl);
        LOGGER.info("Logger level set to " + level.getName());
        Logger topLevel = Logger.getLogger("");
        topLevel.setLevel(level);
        File logs = new File(logDir);
        if (!logs.exists()) {
            if (!logs.mkdir())
                Logger.getLogger(this.getClass().getName()).warning("Could not create logging directory");
        }
        try {
            if (logs.exists()) {
                fHandler = new FileHandler(logs.getAbsolutePath() + File.separator + "ob%g.log");
                topLevel.addHandler(fHandler);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Handler handler : topLevel.getHandlers()) {
            handler.setLevel(level);
        }
        //populate transforms
        LOGGER.info("Configuring Ob");
        LOGGER.fine("Parsing config");
        if (config.getBoolean("Obfuscator.all_transforms")) {
            LOGGER.fine("Adding all transforms");
            transforms.add(ClassNameTransform.class);
        } else {
            if (config.getBoolean("Obfuscator.classname_obfuscation")) {
                LOGGER.fine("Adding class name transform");
                transforms.add(ClassNameTransform.class);
            }
            if (config.getBoolean("Obfuscator.controlflow_obfuscation")) {
                LOGGER.fine("Control flow obfuscation not added, transform does not exist");
            }
            if (config.getBoolean("Obfuscator.string_obfuscation")) {
                LOGGER.fine("String obfuscation not added, transform does not exist");

            }
            if (config.getBoolean("Obfuscator.fieldname_transforms")) {
                LOGGER.fine("Field name obfuscation not added, transform does not exist");

            }
            if (config.getBoolean("Obfuscator.methodname_transforms")) {
                LOGGER.fine("Method name obfuscation not added, transform does not exist");

            }
        }
        LOGGER.info("Loaded " + transforms.size() + " transforms");
        String inputFile = config.get("Obfuscator.input");
        LOGGER.fine("Checking input file(s) and output directory");
        String outFile = config.get("Obfuscator.out_dir");
        out = new File(outFile);
        if (inputFile == null || inputFile.isEmpty()) {
            LOGGER.severe("Input file not specified in config");
            throw new RuntimeException("Input file not specified");
        } else {
            in = new File(inputFile);
            if (!in.exists()) {
                LOGGER.severe("Input file not found");
                throw new RuntimeException("Input file not found");
            }
            LOGGER.fine("Attempting to initialise classes");
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
                    store.init(in, out, this.in);
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            LOGGER.info("Loaded " + store.nodes().size() + " classes");
        }
        if (!out.exists()) {
            LOGGER.fine("Attempting to make output directory");
            if (!out.mkdir()) {
                LOGGER.severe("Could not make output directory");
                throw new RuntimeException("Could not create output dir: " + out.getAbsolutePath());
            }
        } else if (!out.isDirectory()) {
            LOGGER.severe("Output directory is a file");
            throw new RuntimeException(out.getName() + " is not a directory, cannot output there");
        } else {
            if (!out.canWrite()) {
                LOGGER.severe("Cannot write to output directory");
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
        LOGGER.info("Starting obfuscation");
        for (Class<? extends ObfuscatorTransform> clazz : transforms) {
            try {
                ObfuscatorTransform transform = clazz.getConstructor(Config.class).newInstance(config);
                LOGGER.info("Processing transform: " + clazz.getCanonicalName());
                if (fHandler != null) {
                    transform.addLogHandler(fHandler);
                }
                transform.setLogLevel(level, false);
                LOGGER.info("Running: " + clazz.getCanonicalName());
                transform.run(store, config);
                LOGGER.info("Finished processing: " + clazz.getCanonicalName());
                transform.results();
            } catch (Exception e) {
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
        if (cmd.getOptions().length == 0 || cmd.hasOption('h')) {
            formatter.printHelp("java com.speed.ob.Obfuscate", options, true);
            return;
        }
        if (cmd.hasOption("C")) {
            try {
                Config oneTrueConfig = new Config(cmd.getOptionValue('C'));
                Obfuscator obfuscator = new Obfuscator(oneTrueConfig);
                obfuscator.execute();
                obfuscator.postExecute();
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
            obfuscator.postExecute();
        }
    }

    private void postExecute() {
        //output everything
        LOGGER.info("Finished obfuscation. Dumping files");
        try {
            store.dump(in, out, config);
        } catch (IOException e) {
            e.printStackTrace();
        }
        fHandler.close();
    }

}
