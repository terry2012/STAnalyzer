package Utils

import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.SourceUnit

/**
 * Created by jackjia on 6/8/16.
 */
public class PrintLabelVisitor extends ClassCodeVisitorSupport{
    public PrintLabelVisitor(){

    }
    @Override
    public void visitExpressionStatement(ExpressionStatement statement){
        println statement.getLineNumber()+' '+statement.getStatementLabels()
       // println statement.getProperties().get('predecessor')
    }
    @Override
    protected SourceUnit getSourceUnit() {
        return null;
    }
}
