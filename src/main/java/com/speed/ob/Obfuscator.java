package com.speed.ob;

import org.apache.commons.cli.*;

/**
 * Created by shivam on 05/04/14.
 */
public class Obfuscator {
    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("a", "all", false, "enable all obfuscations");
        options.addOption("s", "strings", false, "enable string obfuscation");
        options.addOption("l", "lexical", false, "enable lexical obfuscation");
        options.addOption("c", "control-flow", false, "enable control flow obfuscation");
        options.addOption("C", "config", true, "use <arg> as a config file");
        options.addOption("f", "file", true, "obfuscate file <arg>");
        options.addOption("o", "out", true, "output obfuscated file(s) to <arg>");
        options.addOption("h", "help", false, "shows this help message and then exits");
        CommandLineParser parser = new GnuParser();
        HelpFormatter formatter = new HelpFormatter();
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.getArgs().length == 0) {
                formatter.printHelp("java com.speed.ob.Obfuscate", options, true);
            } else if (cmd.hasOption('h')) {
                formatter.printHelp("java com.speed.ob.Obfuscate", options, true);
                return;
            } else if (cmd.hasOption("C")) {
                System.out.println("Feature not yet implemented");
            }
        } catch (MissingArgumentException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("java com.speed.ob.Obfuscate", options, true);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}
