package com.speed.ob.api;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * See LICENSE.txt for license info
 */
public class ClassStore {
    protected Map<String, ClassNode> store;

    public ClassStore() {
        store = new HashMap<>();
    }

    public void init(JarInputStream jarIn, File output) throws IOException {
        ZipEntry entry;
        JarOutputStream out = new JarOutputStream(new FileOutputStream(output));
        while ((entry = jarIn.getNextEntry()) != null) {
            byte[] data = IOUtils.toByteArray(jarIn);
            if (entry.getName().endsWith(".class")) {
                ClassReader reader = new ClassReader(data);
                ClassNode cn = new ClassNode();
                reader.accept(cn, ClassReader.SKIP_FRAMES);
                store.put(cn.name, cn);
            } else {
                JarEntry je = new JarEntry(entry);
                out.putNextEntry(je);
                out.write(data);
            }
        }
        out.close();
    }


    public void put(Map<String, ClassNode> map) {
        store.putAll(map);
    }

    public ClassNode getCn(final String name) {
        return store.get(name);
    }
}
