package com.cd.languageagent.transformer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;

public class StringTransformer implements ClassFileTransformer {
    public byte[] transform(
        final ClassLoader loader,
        final String className,
        final Class<?> classBeingRedefined,
        final ProtectionDomain protectionDomain,
        final byte[] classfileBuffer) throws IllegalClassFormatException {


        if (className.equals("java/lang/String")) {
            try
            {
                System.setErr(new PrintStream(new File("string-log.out")));
                final ClassPool classPool = ClassPool.getDefault();
                classPool.importPackage("java.nio");
                classPool.importPackage("java.io");

                final CtClass strClass = classPool.get("java.lang.String");

                for (CtConstructor constructor : strClass.getConstructors()) {
//                    constructor.insertAfter("ByteBuffer bb = ByteBuffer.allocate(16); " +
//                        "bb.putLong(System.currentTimeMillis()); " +
//                        "bb.putLong(Thread.currentThread().getId()); " +
//                        "System.err.write(bb.array());");
                    constructor.insertAfter("System.err.print(\"^\");");
                }

//                FileOutputStream fos = new FileOutputStream("string-log.bin");
//                fos.close();
                byte[] byteCode = strClass.toBytecode();
                strClass.detach();

                return byteCode;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return null;
    }
}
