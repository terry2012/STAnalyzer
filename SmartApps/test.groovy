definition(
    name: "PaperDemo",
    namespace: "jyh0082007",
    author: "Yunhan Jia",
    description: "An app that automatically unlocks door when owner is back.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png") 


preferences {
    section("Title") {
        input "lock1","capability.lock",title:"Select a lock"
    }
}
mappings {
    path("/command/:cmd"){
        action:[
            PUT: "onReceived"
        ]
    }
}
def installed() {
    log.info "Installed with settings: ${settings}"
    //Additional info that the app should maintain 
    state.actionQueue = []
    state.appName = "Jack First App"
    state.appDescription = "My first smart app"
    state.category = "Safety & Security"
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
}

def initialize() {
    subscribe(location, "mode", locationHandler)
}
def locationHandler(evt){
    def startTime = now()
    //log.debug "startTime:$startTime"
    
    log.debug "location mode = $location.mode"
    if(location.mode == 'Home'){
        lock1.unlock()
        runIn (5,lockDoor())
    }
    def endTime = now()
    //log.debug "endTime:$endTime"
    log.debug 
}
def lockDoor()
{
    lock1.lock()
}
def onReceived()
{
    def command = params.cmd
    lock1."$command"()
}