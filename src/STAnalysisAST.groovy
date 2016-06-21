/**
 * Created by jackjia on 5/1/16.
 */


import Utils.Helper
import Utils.InitVisitor
import Utils.PrintLabelVisitor
import cfg.CFG
import cfg.CFGNode
import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.ArrayExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.customizers.CompilationCustomizer
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.stmt.ExpressionStatement

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class STAnalysisAST extends CompilationCustomizer{

    static final VALUE = 'value'
    static final DOLLAR = '$'
    static final PUBLIC = ClassNode.ACC_PUBLIC

    Logger log
    Map allCommands
    Map allProps

    List allCommandsList
    List allPropsList
    List allCapsList

    List<CFGNode> nodes
    CFG cfg
    public STAnalysisAST(Logger logger){
        super(CompilePhase.SEMANTIC_ANALYSIS)
        log = logger
        allCommands = new HashMap()
        allProps = new HashMap()
        allCommandsList = new ArrayList()
        allPropsList = new ArrayList()
        allCapsList = new ArrayList()
        nodes = new ArrayList<CFGNode>()
    }
    @Override
    void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
        /*source.getAST().getMethods().each {
            it ->
                it.getCode().asType(BlockStatement).getStatements().each {
                    n-> println n.getLineNumber()
                }
                //def statement = it.get
                //print statement.getLineNumber()+"\n"
        }*/

        MethodCodeVisitor mcv = new MethodCodeVisitor()
        PrintLabelVisitor plv = new PrintLabelVisitor()
        InitVisitor inv = new InitVisitor(allCommandsList)
        //classNode.visitContents(inv)
        classNode.visitContents(mcv)
        cfg = new CFG(nodes)
    }
    class MethodCodeVisitor extends ClassCodeVisitorSupport{
        public ArrayList<String> globals
        public ArrayList<DeclarationExpression> dexpressions
        public ArrayList<BinaryExpression> bexpressions
        private Tuple currentMethod
        //private Tuple<String,List<String>> currentMethod
        public MethodCodeVisitor() {
            globals = new ArrayList<String>()
            dexpressions = new ArrayList<DeclarationExpression>()
            bexpressions = new ArrayList<BinaryExpression>()
        }
        /*
        @Override
        void visitMethodCallExpression(MethodCallExpression mce){
            def mceText
            if (mce.getMethodAsString() == null){
                mceText = mce.getText()
                print "Dynamic:" + mceText+"\n"
            }
            else{
                mceText = mce.getMethodAsString()
                def name = mce.methodAsString
                def parameters = mce?.arguments
                //print "Static:" + name+" "+parameters?.text+"\n"
            }
            super.visitMethodCallExpression(mce)
        }*/
        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }
        @Override
        void  visitMethod(MethodNode node){
            currentMethod = new Tuple(node.name,node.getParameters().size())
            super.visitMethod(node)
        }
        @Override
        void visitStatement(Statement statement) {
            if (statement.getClass() != BlockStatement && statement.getLineNumber()>0) {
                CFGNode node = new CFGNode(statement)
                node.setParent(currentMethod)
                if(node.getExpressionType()=='MethodCallExpression'){
                    MethodCallExpression exp = node.getStatement().asType(ExpressionStatement).getExpression().asType(MethodCallExpression)
                    if(exp.getMethodAsString()?.toLowerCase() in allCommandsList){
                        node.setTag('sink')
                        node.setMetaData(exp.text)
                    }
                }
                nodes.add(node)
            }
        }
    }
    def loadCapRefAll(def file)
    {
        file.splitEachLine(",") { fields ->
            allCommands[fields[0]?.toLowerCase()] = fields[2]?.toLowerCase()
            allProps[fields[0]?.toLowerCase()] = fields[1]?.toLowerCase()
        }

        allCommands.each { k, v ->
            def values = v?.split(" ")
            values?.each { allCommandsList.add(it.toLowerCase()) }
            allCapsList.add(k.toLowerCase())
        }

        allProps.each { k, v ->
            def values = v?.split(" ")
            values?.each { allPropsList.add(it.toLowerCase()) }
        }

    }
    def createClass(String qualifiedClassNodeName) {

        new AstBuilder().buildFromSpec {
            classNode(qualifiedClassNodeName, PUBLIC) {
                classNode Object
                interfaces { classNode GroovyObject }
                mixins { }
            }
        }.first()

    }
    /**
     * This method creates an empty class node with the qualified name passed as parameter
     *
     * @param qualifiedClassNodeName The qualified name of the ClassNode we want to create
     * @return a new ClassNode instance
     */
}
