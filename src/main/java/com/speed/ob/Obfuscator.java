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

/**
 * See LICENSE.txt for license info
 */
public class Obfuscator {

    private final List<Class<? extends ObfuscatorTransform>> transforms;
    private final ClassStore store;

    public Obfuscator(File file, File outFile, List<Class<? extends ObfuscatorTransform>> transforms) {
        this.transforms = transforms;
        store = new ClassStore();
        if (file.isDirectory()) {
            try {
                store.init(file.listFiles(), false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (file.getName().endsWith(".class")) {
            try {
                store.init(new File[]{file}, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (file.getName().endsWith(".jar")) {
            try {
                JarInputStream in = new JarInputStream(new FileInputStream(file));
                store.init(in, outFile);
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void execute() {
        for (Class<? extends ObfuscatorTransform> clazz : transforms) {
            try {
                ObfuscatorTransform transform = clazz.newInstance();
                transform.run(store);
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
        if (cmd.getArgs().length == 0) {
            formatter.printHelp("java com.speed.ob.Obfuscate", options, true);
        } else if (cmd.hasOption('h')) {
            formatter.printHelp("java com.speed.ob.Obfuscate", options, true);
            return;
        }
        if (cmd.hasOption("C")) {
            System.out.println("Config file use not yet implemented");
            return;
        }
        File file = null, outFile = null;
        List<Class<? extends ObfuscatorTransform>> list = new LinkedList<>();
        if (cmd.hasOption("f")) {
            file = new File(cmd.getOptionValue('f'));
            if (!file.exists()) {
                System.out.println("File not found");
                return;
            }
        }
        if (cmd.hasOption("a")) {
            list.add(ClassNameTransform.class);
        }
        if (cmd.hasOption('s')) {
            System.out.println("String encryption not implemented yet.");
        }
        if (cmd.hasOption('c')) {
            System.out.println("Control flow obfuscation not implemented yet.");
        }
        if (cmd.hasOption('l')) {
            list.add(ClassNameTransform.class);
        }
        if (cmd.hasOption('o')) {
            outFile = new File(cmd.getOptionValue('o'));
            if (!outFile.exists()) {
                outFile.mkdir();
            } else if (!outFile.isDirectory()) {
                System.out.println("Cannot output to a file, must select directory");
                return;
            }
        } else {
            outFile = new File(System.getProperty("user.dir"));
        }
        Obfuscator obfuscator = new Obfuscator(file, outFile, list);
        obfuscator.execute();
    }

}
