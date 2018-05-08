package com.cd.languageagent.transformer;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

public class DispatcherServletTransformer implements ClassFileTransformer {

    public byte[] transform(
        final ClassLoader loader,
        final String className,
        final Class<?> classBeingRedefined,
        final ProtectionDomain protectionDomain,
        final byte[] classfileBuffer) throws IllegalClassFormatException {

        if (className.equals("org/springframework/web/servlet/DispatcherServlet")) {
            try {
                final ClassPool classPool = ClassPool.getDefault();
                classPool.importPackage("com.cd.languageagent.reporter.DiagnosticsReporter");
                classPool.importPackage("java.util.UUID");
                classPool.importPackage("java.io.File");
                final CtClass cc = classPool.get("org.springframework.web.servlet.DispatcherServlet");

                final CtMethod method = cc.getDeclaredMethod("doDispatch");
                method.addLocalVariable("startTime", CtClass.longType);
                method.addLocalVariable("totalTime", CtClass.longType);
                method.addLocalVariable("startStringLogBytes", CtClass.longType);
                method.addLocalVariable("totalStringLogBytes", CtClass.longType);
                method.addLocalVariable("startMemory", CtClass.longType);
                method.addLocalVariable("totalMemory", CtClass.longType);

                method.insertBefore("startTime = System.currentTimeMillis();" +
                    "startStringLogBytes = new File(\"string-log.out\").length();" +
                    "startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();" +
                    "response.addHeader(\"language-agent-id\", UUID.randomUUID().toString());");
//                    "StringCountReporter.getInstance().init();");
                method.insertAfter("totalTime = System.currentTimeMillis() - startTime;" +
                    "totalStringLogBytes = new File(\"string-log.out\").length() - startStringLogBytes;" +
                    "totalMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) - startMemory;" +
                    "DiagnosticsReporter.getInstance().report(response.getHeader(\"language-agent-id\"), request.getRequestURI(), totalStringLogBytes, totalTime, totalMemory);");
//                    "StringCountReporter.getInstance().report(response.getHeader(\"language-agent-id\"), Thread.currentThread().getId(), startTime, endTime);");

                byte[] byteCode = cc.toBytecode();
                cc.detach();

                return byteCode;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return null;
    }
}
