/**
 * Created by jackjia on 5/1/16.
 */


import groovy.inspect.swingui.AstNodeToScriptAdapter
import groovy.inspect.swingui.AstNodeToScriptVisitor
import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.ClassHelper
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
import org.codehaus.groovy.control.customizers.builder.CompilerCustomizationBuilder
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import cfg.Helper

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

    MethodNode methodNode;

    public STAnalysisAST(Logger logger){
        super(CompilePhase.SEMANTIC_ANALYSIS)
        log = logger
        allCommands = new HashMap()
        allProps = new HashMap()
        allCommandsList = new ArrayList()
        allPropsList = new ArrayList()
        allCapsList = new ArrayList()
    }
    @Override
    void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
        BlockStatement block = source.getAST().getStatementBlock();
        ClassNode root = createClass("Temp");
        /*source.getAST().getMethods().each {
            it ->
                MethodCodeVisitor mcv = new MethodCodeVisitor()
                it.visit(mcv)
                //def statement = it.get
                //print statement.getLineNumber()+"\n"
        }*/
        MethodCodeVisitor mcv = new MethodCodeVisitor()
        classNode.visitContents(mcv)
        root.addMethod(methodNode);
        System.out.print(Helper.getSourceFromNode(root))
        //print "Length:"+mcv.bexpressions.size()
    }
    class MethodCodeVisitor extends ClassCodeVisitorSupport{
        public ArrayList<String> globals
        public ArrayList<DeclarationExpression> dexpressions
        public ArrayList<BinaryExpression> bexpressions

        public MethodCodeVisitor() {
            globals = new ArrayList<String>()
            dexpressions = new ArrayList<DeclarationExpression>()
            bexpressions = new ArrayList<BinaryExpression>()
        }
        @Override
        public void visitDeclarationExpression(DeclarationExpression dex)
        {
            //print "dex: "+dex.getText()
        }
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
        }
        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }
        @Override
        void  visitMethod(MethodNode node){
            print 'method:'+node.name+"\n"
            methodNode = node
            super.visitMethod(node)
        }
        @Override
        void	visitBlockStatement(BlockStatement block){
            /*
            def statements = block.getStatements()
            if (statements.size()>0){
                def exp = statements[0].asType(ExpressionStatement).getExpression()
                def exp_type = statements[0].asType(ExpressionStatement).getExpression().getClass().getSimpleName()
                switch(exp_type){
                    case MethodCallExpression.getSimpleName():
                        print exp.asType(MethodCallExpression).getMethodAsString()+' '+exp.getLineNumber()+'\n'
                        break
                    case DeclarationExpression.getSimpleName():
                        print exp.asType(DeclarationExpression).text+' '+exp.getLineNumber()+'\n'
                }
            }*/
            /*statements.each {
                stmt ->
                    def class_name = stmt.getClass().getSimpleName()
                    switch (class_name){
                        case 'ExpressionStatement':
                            def exp = stmt.asType(ExpressionStatement).getExpression()
                            def exp_type = exp.getClass().getSimpleName()
                            switch (exp_type){
                                case MethodCallExpression.getSimpleName():
                                    def methodCallExp = exp.asType(MethodCallExpression)
                                    if(methodCallExp.getMethodAsString().toLowerCase() in allCommandsList) {
                                        print '\t' + methodCallExp.getMethodAsString() + '\n'
                                        print '\t' + methodCallExp.getLineNumber() + '\n'
                                    }
                                    break
                                case ClassExpression.getSimpleName():
                                    break
                                case DeclarationExpression.getSimpleName():
                                    break
                                case MapExpression.getSimpleName():
                                    break
                                case VariableExpression.getSimpleName():
                                    break
                                case ArrayExpression.getSimpleName():
                                    break
                                case BinaryExpression.getSimpleName():
                                    break
                                case BitwiseNegationExpression.getSimpleName():
                                    break
                                default:
                                    print '\t'+exp_type
                                    break
                            }
                            break
                        default:
                            break
                    }
            }*/
            //super.visitBlockStatement(block)
        }
        @Override
        public void visitBinaryExpression(BinaryExpression bex)
        {
            bexpressions.add(bex)
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

        println "all commands full:" + allCommandsList.size()
        println "all attrs full:" + allPropsList.size()
    }
    /**
     * This method creates an empty class node with the qualified name passed as parameter
     *
     * @param qualifiedClassNodeName The qualified name of the ClassNode we want to create
     * @return a new ClassNode instance
     */
    def createClass(String qualifiedClassNodeName) {

        new AstBuilder().buildFromSpec {
            classNode(qualifiedClassNodeName, PUBLIC) {
                classNode Object
                interfaces { classNode GroovyObject }
                mixins { }
            }
        }.first()

    }
}
