package com.cd.languageagent;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

import com.cd.languageagent.reporter.StringCountReporter;
import com.cd.languageagent.transformer.DispatcherServletTransformer;
import com.cd.languageagent.transformer.StringTransformer;
import org.springframework.web.servlet.DispatcherServlet;

public class LanguageAgent {
    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new StringTransformer(), true);
        inst.addTransformer(new DispatcherServletTransformer(), true);
        try {
            inst.retransformClasses(String.class);
            inst.retransformClasses(DispatcherServlet.class);
        } catch (UnmodifiableClassException ex) {
            ex.printStackTrace();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> StringCountReporter.getInstance().stop()));
    }
}
