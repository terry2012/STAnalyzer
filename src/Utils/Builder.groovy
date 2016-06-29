package Utils

import groovyjarjarasm.asm.Opcodes
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.syntax.Types

/**
 * Created by jackjia on 6/26/16.
 */
public class Builder {
    public static ASTNode buildMethodCall(String methodName, String... args){
        List<ASTNode> nodes = new AstBuilder().buildFromSpec {
            expression{
                methodCall{
                    variable "advice"
                    constant "before"
                    argumentList {
                        constant methodName
                        list {
                            args.each { variable it }
                        }
                    }
                }
            }
        }
        nodes.first()
    }
    public static ASTNode buildStatement(String stat){
        String s = stat
        List<ASTNode> nodes = new AstBuilder().buildFromString(stat)
        nodes.first()
    }
    public static ASTNode buildAssignment2State(String left, String right){
        def leftExpression = new PropertyExpression(
                new VariableExpression('state'),
                new ConstantExpression(left)
        )
        def assignment = new BinaryExpression(
                leftExpression,
                Token.newSymbol(Types.EQUAL,0,0),
                new ConstantExpression(right)
        )
        ExpressionStatement exp = new ExpressionStatement(assignment)
        exp
    }
    public static ASTNode buildAssignmentList2State(String left, List right){
        def leftExpression = new PropertyExpression(
                new VariableExpression('state'),
                new ConstantExpression(left)
        )
        def assignment = new BinaryExpression(
                leftExpression,
                Token.newSymbol(Types.EQUAL,0,0),
                new ListExpression(right)
        )
        ExpressionStatement exp = new ExpressionStatement(assignment)
        exp
    }
}
