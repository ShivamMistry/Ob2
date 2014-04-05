package com.speed.ob;

import org.apache.commons.cli.*;

/**
 * Created by shivam on 05/04/14.
 */
public class Obfuscator {
    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("a", false, "enable all obfuscations");
        options.addOption("s", false, "enable string obfuscation");
        options.addOption("l", false, "enable lexical obfuscation");
        options.addOption("cf", false, "enable control flow obfuscation");
        options.addOption("c", true, "use <arg> as a config file");
        options.addOption("f", true, "obfuscate file <arg>");
        options.addOption("o", true, "output obfuscated file(s) to <arg>");
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
