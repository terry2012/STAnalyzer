package Utils

import org.codehaus.groovy.ast.stmt.Statement

/**
 * Created by jackjia on 6/11/16.
 */
class IAST {
    private List<Statement> statements
    public IAST(List<Statement> stats){
        statements = stats
    }
    public size(){
        return statements.size()
    }
    public add(Statement statement){
        statements.add(statement)
    }
    public get(int id){
        Statement result
        statements.each {
            if(Helper.getStatementId(it) == id){
                result = it
            }
        }
        result
    }
    public getByName(String name){

    }
}
