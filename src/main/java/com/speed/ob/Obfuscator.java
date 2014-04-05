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
        CommandLineParser parser = new GnuParser();
        HelpFormatter formatter = new HelpFormatter();
        try {
            CommandLine cmd = parser.parse(options, args);
            formatter.printHelp("java com.speed.ob.Obfuscate", options, true);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
