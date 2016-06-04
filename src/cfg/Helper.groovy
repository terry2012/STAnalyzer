package cfg

import groovy.inspect.swingui.AstNodeToScriptVisitor
import org.codehaus.groovy.ast.ClassNode


/**
 * Created by jackjia on 6/2/16.
 */
class Helper {
    public static String getSourceFromNode(ClassNode classNode){
        java.io.StringWriter writer = new java.io.StringWriter();
        AstNodeToScriptVisitor visitor = new AstNodeToScriptVisitor(writer);
        visitor.visitClass(classNode); // replace with proper visit****
        System.out.println(writer.toString());
    }
}
