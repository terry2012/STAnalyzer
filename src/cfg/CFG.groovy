package cfg

import Utils.Helper
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.CaseStatement
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.stmt.SwitchStatement

import java.lang.reflect.Method

/**
 * Created by jackjia on 5/2/16.
 */
public class CFG {
    List<CFGNode> cfg
    List<CFGNode> IfStatements
    List<CFGNode> MethodCallExpressions
    List<CFGNode> SwithCaseStatements
    /* Temp variables*/
    int counter = 0
    SwitchStatement currentSwitch
    public CFG(List<CFGNode> nodes){
        cfg = nodes
        IfStatements = new ArrayList<CFGNode>()
        MethodCallExpressions = new ArrayList<CFGNode>()
        SwithCaseStatements = new ArrayList<CFGNode>()
        init()
        sinkPrinter()

    }
    private void init(){
        /* Initialize Id*/
        String currentSwitch
        cfg.each {
            node->
                node.setId(++counter)
                if(node.getStatementType()=='IfStatement'){
                    handleIf(node)
                }
                else if (node.getExpressionType()=='MethodCallExpression'){
                    MethodCallExpression exp = node.getStatement().asType(ExpressionStatement).getExpression().asType(MethodCallExpression)
                    if(exp.getMethodAsString()?.toLowerCase() == 'subscribe'){
                        handleSubscribe(node)
                    }
                    else if(exp.getMethodAsString()?.toLowerCase() == 'schedule'){
                        handleSchedule(node)
                    }
                    else{
                        handleNormalCall(node)
                    }
                }
                else if (node.getStatementType()=='SwitchStatement' || node.getStatementType()=='CaseStatement'){
                    //handleSwitchCase(node)
                }
        }
    }
    private void handleIf(CFGNode ifnode){
        IfStatement ifstmt = ifnode.getStatement().asType(IfStatement)
        Integer if_id = ifnode.getId()
        def predecessors = ifnode.getPredecessors()
        if(ifstmt.getIfBlock().getClass()!=EmptyStatement){
            ifstmt.getIfBlock().asType(BlockStatement).statements.each {
                CFGNode node = getNodeByLineNumber(it.getLineNumber())
                def tuple = new Tuple(if_id,'IfStatement','IfBlock')
                node.addPredecessor(tuple)
                node.joinPredecessors(predecessors)
            }
        }
        if(ifstmt.getElseBlock().getClass()==IfStatement){
            IfStatement nestedIf = ifstmt.getElseBlock().asType(IfStatement)
            CFGNode node = getNodeByLineNumber(nestedIf.getLineNumber())
            def tuple = new Tuple(if_id,'IfStatement','ElseBlock')
            node.addPredecessor(tuple)
            node.joinPredecessors(predecessors)
        }
        else if(ifstmt.getElseBlock().getClass()!=EmptyStatement && ifstmt.getElseBlock().getClass()!=IfStatement){
            ifstmt.getElseBlock().asType(BlockStatement).statements.each {
                CFGNode node = getNodeByLineNumber(it.getLineNumber())
                def tuple = new Tuple(if_id,'IfStatement','ElseBlock')
                node.addPredecessor(tuple)
                node.joinPredecessors(predecessors)
            }
        }
    }
    private void handleSubscribe(CFGNode methodCallNode){
        MethodCallExpression exp = methodCallNode.getStatement().asType(ExpressionStatement).getExpression().asType(MethodCallExpression)
        def predecessors = methodCallNode.getPredecessors()
        methodCallNode.setTag('subscribe')
        methodCallNode.setMetaData(exp.getText())
        String triggerName
        String handlerName
        if(exp.getArguments().getAt(2)==null){
            def trigger = exp.getArguments().getAt(0)
            triggerName = trigger.getText()
            def handler = exp.getArguments().getAt(1)
            handlerName = handler.getText()
        }
        else {
            def trigger = exp.getArguments().getAt(1)
            triggerName = trigger.getText()
            def handler = exp.getArguments().getAt(2)
            handlerName = handler.getText()
        }
        cfg.each {
            node->
                if(node.getParent()==handlerName){
                    def tuple = new Tuple(methodCallNode.getId(),'Subscribe',triggerName)
                    node.addPredecessor(tuple)
                    node.joinPredecessors(predecessors)
                }
        }
    }
    private void handleNormalCall(CFGNode methodCallNode){
        MethodCallExpression exp = methodCallNode.getStatement().asType(ExpressionStatement).getExpression().asType(MethodCallExpression)
        methodCallNode.setMetaData(exp.getText())
        Integer argcount = 0
        def argument = exp.getArguments().getAt(0)
        while(argument!=null){
            argcount++
            argument = exp.getArguments().getAt(argcount)
        }
        Tuple<String,Integer> tuple = new Tuple(exp.getMethodAsString(),argcount)
        cfg.each {
            node->
                if(node.getParent()==tuple){
                    def tup = new Tuple(methodCallNode.getId(),'MethodCall',exp.getMethodAsString())
                    node.addPredecessor(tup)
                }
        }
    }
    private void handleSchedule(CFGNode methodCallNode){
        MethodCallExpression exp = methodCallNode.getStatement().asType(ExpressionStatement).getExpression().asType(MethodCallExpression)
        def predecessors = methodCallNode.getPredecessors()
        methodCallNode.setTag('schedule')
        methodCallNode.setMetaData(exp.getText())
        def trigger = exp.getArguments().getAt(0)
        String triggerName = trigger.getText()
        VariableExpression handler = exp.getArguments().getAt(1)
        String handlerName = handler.getText()
        cfg.each {
            node->
                if(node.getParent()==handlerName){
                    def tuple = new Tuple(methodCallNode.getId(),'Schedule',triggerName)
                    node.addPredecessor(tuple)
                    node.joinPredecessors(predecessors)
                }
        }
    }
    private void handleSwitchCase(CFGNode node){
        if(node.getStatementType() == 'SwitchStatement'){
            SwitchStatement stmt = node.getStatement()
            currentSwitch = stmt
        }
        else {
            CaseStatement stmt = node.getStatement()
            println '\t'+stmt.getExpression()
        }
    }
    private CFGNode getNodeByLineNumber(Integer number){
        CFGNode result
        cfg.each {
            node->
                if(node.getLineNumber()==number){
                    result = node
                }
        }
        return result
    }
    private CFGNode getNodeById(Integer id){
        CFGNode result
        cfg.each {
            node->
              if(node.getId()==id){
                  result = node
              }
        }
        result
    }
    public CFGNode get(Integer id){
        CFGNode result
        cfg.each {
            it->
                if(it.getId()==id){
                    result = it
                }
        }
        result
    }
    public void visitPrinter(){
        cfg.each {
            node->
                println node.getLineNumber()+'\t'+node.getStatementType()+'\t'+node.getId()+'\t'+node.getTag()+'\t'+node.getMetaData()+'\t'+node.getParent()+'\t'+node.getPredecessors().sort()
        }
    }
    public void getSinks(){
        List<Integer> result = new ArrayList<Integer>()
        cfg.each {
            node->
                if(node.getTag()=='sink'){
                    result.add(node.getId())
                }
        }
        result
    }
    public void sinkPrinter(){
        cfg.each {
            node->
                if(node.getTag()=='sink') {
                    println node.getLineNumber() + '\t'+node.getMetaData()+ '\t' + node.getId() + '\t' + node.getTag() + '\t' + node.getParent() + '\t' + node.getPredecessors().sort()
                }
        }
    }
    /*public List<CFGNode> getTriggerNodes(Integer id){
        List<Integer> triggers =
    }*/
}
