package com.speed.ob.api;

import com.speed.ob.Config;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

/**
 * See LICENSE.txt for license info
 */
public class ClassStore {
    protected Map<String, ClassNode> store;

    public ClassStore() {
        store = new HashMap<>();
    }

    public void init(File[] classFiles, boolean recursive) throws IOException {
        for (File file : classFiles) {
            if (file.getName().endsWith(".class")) {
                try {
                    FileInputStream in = new FileInputStream(file);
                    ClassReader reader = new ClassReader(in);
                    ClassNode cn = new ClassNode();
                    reader.accept(cn, ClassReader.EXPAND_FRAMES);
                    store.put(cn.name, cn);
                    in.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else if (recursive && file.isDirectory()) {
                init(file.listFiles(), true);
            }
        }
    }

    public void init(JarInputStream jarIn, File output, File in) throws IOException {
        ZipEntry entry;
        JarOutputStream out = new JarOutputStream(new FileOutputStream(new File(output, in.getName())));
        while ((entry = jarIn.getNextEntry()) != null) {
            byte[] data = IOUtils.toByteArray(jarIn);
            if (entry.getName().endsWith(".class")) {
                ClassReader reader = new ClassReader(data);
                ClassNode cn = new ClassNode();
                reader.accept(cn, ClassReader.EXPAND_FRAMES);
                store.put(cn.name, cn);
            } else if (!entry.isDirectory()) {
                Logger.getLogger(getClass().getName()).finer("Storing " + entry.getName() + " in output file");
                JarEntry je = new JarEntry(entry.getName());
                out.putNextEntry(je);
                out.write(data);
                out.closeEntry();
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

    public Collection<ClassNode> nodes() {
        return store.values();
    }

    public void dump(File in, File out, Config config) throws IOException {
        if (in.isDirectory()) {
            for (ClassNode node : nodes()) {
                String[] parts = node.name.split("\\.");
                String dirName = node.name.substring(0, node.name.lastIndexOf("."));
                dirName = dirName.replace(".", "/");
                File dir = new File(out, dirName);
                if (!dir.exists()) {
                    if (!dir.mkdirs())
                        throw new IOException("Could not make output dir: " + dir.getAbsolutePath());
                }
                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                node.accept(writer);
                byte[] data = writer.toByteArray();
                FileOutputStream fOut = new FileOutputStream(new File(dir, node.name.substring(node.name.lastIndexOf(".") + 1)));
                fOut.write(data);
                fOut.flush();
                fOut.close();
            }
        } else if (in.getName().endsWith(".jar")) {
            File output = new File(out, in.getName());
            JarFile jf = new JarFile(in);
            HashMap<JarEntry, Object> existingData = new HashMap<>();
            if (output.exists()) {
                try {
                    JarInputStream jarIn = new JarInputStream(new FileInputStream(output));
                    JarEntry entry;
                    while ((entry = jarIn.getNextJarEntry()) != null) {
                        if (!entry.isDirectory()) {
                            byte[] data = IOUtils.toByteArray(jarIn);
                            existingData.put(entry, data);
                            jarIn.closeEntry();
                        }
                    }
                    jarIn.close();
                } catch (IOException e) {
                    Logger.getLogger(this.getClass().getName()).
                            log(Level.SEVERE, "Could not read existing output file, overwriting", e);
                }
            }
            FileOutputStream fout = new FileOutputStream(output);
            Manifest manifest = null;
            if (jf.getManifest() != null) {
                manifest = jf.getManifest();
                if (!config.getBoolean("ClassNameTransform.keep_packages")
                        && config.getBoolean("ClassNameTransform.exclude_mains")) {
                    manifest = new Manifest(manifest);
                    if (manifest.getMainAttributes().getValue("Main-Class") != null) {
                        String manifestName = manifest.getMainAttributes().getValue("Main-Class");
                        if (manifestName.contains(".")) {
                            manifestName = manifestName.substring(manifestName.lastIndexOf(".") + 1);
                            manifest.getMainAttributes().putValue("Main-Class", manifestName);
                        }
                    }
                }
            }
            jf.close();
            JarOutputStream jarOut = manifest == null ? new JarOutputStream(fout) : new JarOutputStream(fout, manifest);
            Logger.getLogger(getClass().getName()).fine("Restoring " + existingData.size() + " existing files");
            if (!existingData.isEmpty()) {
                for (Map.Entry<JarEntry, Object> entry : existingData.entrySet()) {
                    Logger.getLogger(getClass().getName()).fine("Restoring " + entry.getKey().getName());
                    jarOut.putNextEntry(entry.getKey());
                    jarOut.write((byte[]) entry.getValue());
                    jarOut.closeEntry();
                }
            }
            for (ClassNode node : nodes()) {
                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                node.accept(writer);
                byte[] data = writer.toByteArray();
                int index = node.name.lastIndexOf("/");
                String fileName;
                if (index > 0) {
                    fileName = node.name.substring(0, index + 1).replace(".", "/");
                    fileName += node.name.substring(index + 1).concat(".class");
                } else {
                    fileName = node.name.concat(".class");
                }
                JarEntry entry = new JarEntry(fileName);
                jarOut.putNextEntry(entry);
                jarOut.write(data);
                jarOut.closeEntry();
            }
            jarOut.close();
        } else {
            if (nodes().size() == 1) {
                File outputFile = new File(out, in.getName());
                ClassNode node = nodes().iterator().next();
                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                byte[] data = writer.toByteArray();
                FileOutputStream stream = new FileOutputStream(outputFile);
                stream.write(data);
                stream.close();
            }
        }
    }

    public void set(HashMap<String, ClassNode> newClasses) {
        this.store = newClasses;
    }
}
