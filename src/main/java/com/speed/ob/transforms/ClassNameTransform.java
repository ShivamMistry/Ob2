package com.speed.ob.transforms;

import com.speed.ob.Config;
import com.speed.ob.api.ClassStore;
import com.speed.ob.api.ObfuscatorTransform;
import com.speed.ob.util.NameGenerator;
import jdk.internal.org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * See LICENSE.txt for license info
 */
public class ClassNameTransform extends ObfuscatorTransform implements Opcodes {

    private Map<String, String> namesMap = new HashMap<>();
    private NameGenerator nameGenerator = new NameGenerator();
    private boolean excludeMains;
    private List<String> excludedClasses;
    private boolean keepPackages;

    class ClassRemapper extends Remapper {
        @Override
        public String map(String type) {
            LOGGER.finest("To remap: " + type + " or not to");
            if (!namesMap.containsKey(type)) {
                return type;
            }
            String newName = namesMap.get(type);
            LOGGER.finest("Remapping: " + type + " to " + newName);
            return newName;
        }
    }

    public ClassNameTransform(Config config) {
        super(config);
        excludeMains = config.getBoolean("ClassNameTransform.exclude_mains");
        LOGGER.finer("Exclude mains: " + excludeMains);
        excludedClasses = config.getList("ClassNameTransform.excludes");
        LOGGER.finer("Excluding " + excludedClasses.size() + " classes");
        keepPackages = config.getBoolean("ClassNameTransform.keep_packages");
        LOGGER.finer("Keeping packages:" + keepPackages);
    }

    public void run(ClassStore store, Config config) {
        //generate map of old names -> new names
        for (ClassNode node : store.nodes()) {
            LOGGER.fine("Processing class: " + node.name);
            boolean hasMain = false;
            if (excludeMains) {
                for (MethodNode mn : (List<MethodNode>) node.methods) {
                    if (mn.name.equals("main") && mn.desc.equals("([Ljava/lang/String;)V")) {
                        LOGGER.fine("Not renaming " + node.name + ", has a main method");
                        //continue outer;
                        hasMain = true;
                        break;
                    }
                }
            }
            if (excludedClasses != null && excludedClasses.contains(node.name)) {
                LOGGER.fine("Not renaming " + node.name + ", is excluded");
                continue;
            }
            String newName;
            int ind = node.name.lastIndexOf('/');
            if (excludeMains && hasMain) {
                if (ind > -1) {
                    newName = node.name.substring(ind + 1);
                } else {
                    newName = node.name;
                }
            } else {
                if (keepPackages && ind > -1) {
                    newName = node.name.substring(0, ind) + '/' + nameGenerator.next();
                } else {
                    newName = nameGenerator.next();
                }
            }

            LOGGER.finer("Renaming " + node.name + " to " + newName);
            namesMap.put(node.name, newName);
        }
        Iterator<ClassNode> iterator = store.nodes().iterator();
        HashMap<String, ClassNode> newClasses = new HashMap<>();
        while (iterator.hasNext()) {
            ClassNode cn = iterator.next();
            ClassNode node = new ClassNode();
            RemappingClassAdapter remapping = new RemappingClassAdapter(node, new ClassRemapper());
            cn.accept(remapping);
            newClasses.put(node.name, node);
        }
        store.set(newClasses);
    }


    public void results() {
        for (String s : namesMap.keySet()) {
            LOGGER.fine(s + " replaced with " + namesMap.get(s));
        }
        LOGGER.info("Renamed " + namesMap.size() + " values");
    }


}
