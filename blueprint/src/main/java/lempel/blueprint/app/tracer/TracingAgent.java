package lempel.blueprint.app.tracer;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

//VM Args to turn this on:
//-Xbootclasspath/a:raventools_inst.jar -javaagent:raventools_inst.jar

public class TracingAgent {
    public static void premain(String agentArgs, Instrumentation inst) {
        MethodLogger.log("TracingAgent.premain() was called.");
        ClassFileTransformer trans = new TracingXformer();
        inst.addTransformer(trans);
    }
}
