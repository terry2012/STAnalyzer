/**
 * Created by jackjia on 5/1/16.
 */


import Utils.Builder
import Utils.Helper
import Utils.InitVisitor
import Utils.PatchVisitor
import cfg.CFG
import cfg.ICFG
import cfg.ICFGNode
import cfg.CFGNode
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ArrayExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.MapEntryExpression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.NamedArgumentListExpression
import org.codehaus.groovy.ast.expr.TupleExpression
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

    List<ICFGNode> nodes
    ICFG cfg
    String appId
    String appName
    String appDescription
    String appCategory
    List<String> validMethods
    public STAnalysisAST(Logger logger){
        super(CompilePhase.SEMANTIC_ANALYSIS)
        log = logger
        allCommands = new HashMap()
        allProps = new HashMap()
        allCommandsList = new ArrayList()
        allPropsList = new ArrayList()
        allCapsList = new ArrayList()
        validMethods = new ArrayList<String>()
        nodes = new ArrayList<ICFGNode>()
    }
    @Override
    void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
//        source.getAST().getMethods().each {
//            it ->
//                if(it.getName() == 'installed'){
//                    List existingStatements = it.getCode().getStatements()
//                    existingStatements.add(Builder.buildAssignmentStatement('data','haha'))
//                }
//                //def statement = it.get
//                //print statement.getLineNumber()+"\n"
//        }

        ConstructVisitor cv = new ConstructVisitor()
        InitVisitor inv = new InitVisitor(allCommandsList)
        classNode.visitContents(cv)
        classNode.visitContents(inv)
        cfg = new ICFG(nodes)
        AnnotationVisitor anv = new AnnotationVisitor(cfg)
        classNode.visitContents(anv)

        Patch(source,classNode)

        Helper.getSourceFromNode(classNode)
    }
    private void Patch(SourceUnit source,ClassNode classNode){
        appId = Helper.getUniqueKeyUsingUUID()
        classNode.getMethods().each {
            method->
                if(method.getName() == 'run'){
                    List existingStatements = method.getCode().getStatements()
                    existingStatements.add(Builder.buildCallbackMappings('onPermissionResponse'))
                }
                if(method.getName() == 'installed'){
                    List existingStatements = method.getCode().getStatements()
                    existingStatements.add(Builder.buildAssignment2State('appId',appId))
                    existingStatements.add(Builder.buildAssignmentList2State('actionQueue',[]))
                    existingStatements.add(Builder.buildAssignment2State('appName',appName))
                    existingStatements.add(Builder.buildAssignment2State('appDescription',appDescription))
                    existingStatements.add(Builder.buildAssignment2State('appCategory',appCategory))
                    /* Add variables to log predecessor for each function*/
                    validMethods.each {
                        existingStatements.add(Builder.buildAssignment2State("$it"+"_predecessor",""))
                    }
                }
        }
        PatchVisitor pv = new PatchVisitor(validMethods)
        classNode.visitContents(pv)
    }
    class ConstructVisitor extends ClassCodeVisitorSupport{
        public ArrayList<String> globals
        public ArrayList<DeclarationExpression> dexpressions
        public ArrayList<BinaryExpression> bexpressions
        private Tuple currentMethod
        //private Tuple<String,List<String>> currentMethod
        public ConstructVisitor() {
        }
        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }
        @Override
        void  visitMethod(MethodNode node){
            currentMethod = new Tuple(node.name,node.getParameters().size())
            if(node.getName() != 'main' && node.getName() != 'run'){
                validMethods.add(node.getName())
            }
            super.visitMethod(node)
        }
        @Override
        void visitStatement(Statement statement) {
            if (statement.getClass() != BlockStatement && statement.getLineNumber()>0) {
                ICFGNode node = new ICFGNode(statement)
                node.setParent(currentMethod)
                if(node.getExpressionType()=='MethodCallExpression'){
                    MethodCallExpression exp = node.getStatement().asType(ExpressionStatement).getExpression().asType(MethodCallExpression)
                    if(exp.getMethodAsString()?.toLowerCase() in allCommandsList){
                        node.setTag('sink')
                        node.setMetaData(exp.text)
                    }
                    if(exp.getMethodAsString()=='definition'){
                        switch (exp.getArguments().getType()) {
                            case ArgumentListExpression:
                                ArgumentListExpression arglistexp = exp.getArguments()
                                MapExpression map = arglistexp.first()
                                List<MapEntryExpression> entrys = map.getMapEntryExpressions()
                                entrys.each {
                                    entry->
                                        def key = entry.getKeyExpression().text
                                        def value = entry.getValueExpression().text
                                        if(key.equalsIgnoreCase('name')){
                                            appName = value
                                        }
                                        else if (key.equalsIgnoreCase('description')){
                                            appDescription = value
                                        }
                                        else if (key.equalsIgnoreCase('category')){
                                            appCategory = value
                                        }
                                }
                                break
                            default:
                                TupleExpression tupleExpression = exp.getArguments()
                                NamedArgumentListExpression namedArgumentListExpression = tupleExpression.first()
                                namedArgumentListExpression.getMapEntryExpressions().each {
                                    entry->
                                        def key = entry.getKeyExpression().text
                                        def value = entry.getValueExpression().text
                                        if(key.equalsIgnoreCase('name')){
                                            appName = value
                                        }
                                        else if (key.equalsIgnoreCase('description')){
                                            appDescription = value
                                        }
                                        else if (key.equalsIgnoreCase('category')){
                                            appCategory = value
                                        }
                                }
                        }
                    }
                }
                nodes.add(node)
            }
        }
    }
    class AnnotationVisitor extends ClassCodeVisitorSupport{
        public ICFG icfg
        public AnnotationVisitor(ICFG cfg) {
            icfg = cfg
        }
        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }
        @Override
        void visitStatement(Statement statement){
            if(statement.getClass() != BlockStatement && statement.getLineNumber()>0){
                def jsonBuilder = new JsonBuilder()
                ICFGNode node = icfg.getNodeByLineNumber(statement.getLineNumber())
                def node_id = node?.getId()
                def node_tag = node?.getTag()
                def node_data = node?.getMetaData()
                def node_parent = node?.getParent()
                def node_predecessor = node?.getPredecessors()?.size()>0? node.getPredecessors().first():null
                def map = ['id':node_id,'tag':node_tag,'metadata':node_data,'parent':node_parent,'predecessor':node_predecessor]
                jsonBuilder(map)
                def annotation = jsonBuilder.toString()
                println  node_id+' '+node_tag+' '+annotation.replaceAll("\"","\'")
                statement.setStatementLabel(annotation)
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
        List<String> httpSinks = ['httpDelete','httpGet','httpHead','httpPost','httpPostJson','httpPut','httpPutJson',]
        List<String> notificationSinks = ['sendLocationEvent','sendNotification','sendNotificationEvent','sendNotificationContacts','sendPushMessage','sendSMS','sendSMSMessage']
        List<String> changeSettingSinks = ['setLocationMode'] //subscribeToCommand()
        httpSinks.each { allCommandsList.add(it.toLowerCase())}
        notificationSinks.each{ allCommandsList.add(it.toLowerCase())}
        changeSettingSinks.each {allCommandsList.add(it.toLowerCase())}
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
    def toLower
    /**
     * This method creates an empty class node with the qualified name passed as parameter
     *
     * @param qualifiedClassNodeName The qualified name of the ClassNode we want to create
     * @return a new ClassNode instance
     */
}
