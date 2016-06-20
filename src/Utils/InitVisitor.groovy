package Utils

import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.ArrayExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.SourceUnit

/**
 * Created by jackjia on 6/10/16.
 */
public class InitVisitor extends ClassCodeVisitorSupport {
    Integer counter = 1
    List allCommandsList
    List<MethodNode> methods
    public InitVisitor(List list){
        allCommandsList = list
        methods = new ArrayList<MethodNode>()
    }
    String currentMethod
    /*public void visitMethod(MethodNode node){

    }*/
    /*@Override
    public void visitStatement(Statement statement){
        //println statement.getLineNumber()+' '+ statement.getClass()+' '+ statement.getProperties().get('nodeMetaData')
        if(statement.getLineNumber()> 0) {
            Helper.setStatementId(statement, counter++)
            switch (statement.getClass()) {
                case BlockStatement:
                    break
                case ExpressionStatement:
                    def exp = statement.asType(ExpressionStatement).getExpression()
                    switch (exp.getClass()) {
                        case MethodCallExpression:
                            def methodCallExp = exp.asType(MethodCallExpression)
                            if (methodCallExp.getMethodAsString().toLowerCase() in allCommandsList) {
                                Helper.setStatementTag(statement,'sink')
                            }
                            else if (methodCallExp.getMethodAsString().equals('subscribe')){
                                Helper.setStatementTag(statement,'subscribe')
                            }
                            break
                        default:
                            break
                    }
                    break
                default:
                    break
            }
        }
    }*/

    @Override
    public void visitExpressionStatement(ExpressionStatement statement){
        //println statement.getLineNumber()+' '+ statement.getExpression().getClass()+' '+ statement.getExpression().getProperties()
        print statement.getExpression().getAnnotations()
        //super.visitExpressionStatement(statement)
    }
    public Integer get123(){
        return 123
    }
    @Override
    protected SourceUnit getSourceUnit() {
        return null;
    }
}
