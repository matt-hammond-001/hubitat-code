/* 
=============================================================================
Hubitat Elevation Application

Contact sensor log

Version 0.0.1

(c) 2026 Matt Hammond / matthammond.org

https://github.com/matt-hammond-001/hubitat-code

-----------------------------------------------------------------------------
This code is licensed as follows:

BSD 3-Clause License

Copyright (c) 2026, Matt Hammond
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

3. Neither the name of the copyright holder nor the names of its
   contributors may be used to endorse or promote products derived from
   this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
-----------------------------------------------------------------------------
*/

import java.text.SimpleDateFormat 
import groovy.transform.Field

definition(
	name: "Contact Sensor activity log",
	namespace: "matthammonddotorg",
	author: "Matt Hammond",
	description: "Logs the most recent times a contact sensor has opened",
    documentationLink: "",
    singleInstance: true,
    iconUrl: "",
    iconX2Url: "",
)

preferences {
    page(name: "mainPage")
}

def mainPage() {
    dynamicPage(name: "mainPage", title: "", install: true, uninstall: true,submitOnChange: true, containerClass: "w-full") {
        section("<h3>Contact sensor to watch</h3>") {
         	input "contactSensors",
                "capability.contactSensor",
                title: "Contact sensor to monitor",
                multiple: true,
                width: 4,
                required: true,
                submitOnChange: true
            
			input "granularitySecs",
                "number",
                title: "If contact sensor is closed for less than this many seconds then do not consider as a separate event",
            	required: true,
                submitOnChange: true,
                width: 4,
                defaultValue: 120
            
            input "maxLogSize",
                "number",
                title: "Maximum number of events to remember",
                required: true,
                submitOnChange: true,
                width: 4,
                defaultValue: 10
            
            input "pillColour",
                "color",
                title: "Tile time lozenge colour",
                required: true,
                submitOnChange: true,
                defaultValue: "#666622"
            
            input "rebuildTiles",
                "button",
                title: "Rebuild tiles"

            input "clearLogs",
                "button",
                title: "Clear logs"

        }
        
        section("<b>Logging</b>") {
            input "infoEnable",
                "bool",
                title: "Enable activity logging",
                required: false,
                defaultValue: false
        }
        
        section("<b>Debugging</b>") {
            input "debugEnable",
                "bool",
                title: "Enable debug logging", 
                required: false,
                defaultValue: false
        }    }
}

def appButtonHandler(String buttonName) {
    switch (buttonName) {
        case "clearLogs":
			ensureLogsAndChildren clearLogs:true;
        	rebuildTiles();
        	break
        case "rebuildTiles":
        	rebuildTiles();
        	break;
        default:
            break
    }
}

def installed() {
    log.debug "Installed"
    updated()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
	ensureLogsAndChildren()
    unsubscribe()
    subscribe(settings.contactSensors, "contact", "onContactSensor", [filterEvents:true])
}

def initialize() {
    log.debug "Initialized"
	updated()    
}

/*
-----------------------------------------------------------------------------
Logging output
-----------------------------------------------------------------------------
*/

def logDebug(msg) {
    if (settings.debugEnable) {
        log.debug msg
    }
}

def logInfo(msg) {
    if (settings.infoEnable) {
        log.info msg
    }
}


/*
-----------------------------------------------------------------------------
Logs
-----------------------------------------------------------------------------
*/
def defaultIfNull(x,y) {
    if (x==null) return y else return x
}

def ensureLogsAndChildren(Map options=[:]) {
    options = [clearLogs:false, *:options]
    
    Map sensorDefaults = [:]
    settings.contactSensors.each { sensorDev -> 
        sensorDefaults[sensorDev.getId()] = [
            openPeriods: [],
            childDni: "${app.id}-${sensorDev.getId()}",
            childLabel: "${app.id}-${sensorDev.getLabel()}"
        ]
	}

    logDebug("ensureLogsAndChildren(): sensorDefaults = ${sensorDefaults}");
    logDebug("ensureLogsAndChildren(): state.contactSensorLogs = ${state?.contactSensorLogs}");
    
    // ensure state logs map exists
    if (state?.contactSensorLogs == null) {
        state.contactSensorLogs = [:]
    }

    // go through state.contactSensorLogs and delete any not expected
    def keysToDelete = state.contactSensorLogs.keySet().findAll { key -> !sensorDefaults.containsKey(key) }
    keysToDelete.each { key -> state.contactSensorLogs.remove(key) }
    
    // go through and ensure each log we expect exists and has correct metadata and log size
    sensorDefaults.each { sensorId, defaults ->
        Map existing = state.contactSensorLogs[sensorId] ?: [:] ;
        if (options.clearLogs) {
			existing.openPeriods = []
        }
            
 		state.contactSensorLogs[sensorId] = [:] << defaults << existing;
        logDebug("Ensuring state.contactSensorLogs[${sensorId}] = ${state.contactSensorLogs[sensorId]} == [:] << ${defaults} << ${existing}")
        
        // trim logs if too many
        List openPeriods = state.contactSensorLogs[sensorId].openPeriods;
        while (openPeriods.size() > settings.maxLogSize) {
            openPeriods.remove(openPeriods.size()-1)
        }
    }
    
    // build map of expected child device Ids mapping to labels
    Map childDefaults = [:]
    sensorDefaults.each { sensorId, defaults -> 
        childDefaults[defaults.childDni] = defaults.childLabel
    }
    logDebug("ensureLogsAndChildren() : childDefaults = ${childDefaults}")
             
    // go through existing child devices
    getChildDevices().each { dev ->
        String childDni = "${dev.getDeviceNetworkId()}"
        logDebug("ensureLogsAndChildren() : examining existing child device dni = ${childDni}. childDefaults contains key? ${childDefaults.containsKey(childDni)}")
        if (childDefaults[childDni] == null) {
            // child should not exist, delete it
            logDebug("ensureLogsAndChildren() : deleting child device ${childDni}")
        	deleteChildDevice(childDni)
        } else {
            // child exists. remove from childDefaults so we don't create it in next step
            logDebug("ensureLogsAndChildren() : alreadt exists - child device ${childDni}")
	        childDefaults.remove(childDni)
        }
    }
    
    // create any child devices that do not yet exist - childDefaults will contain ones not yet seen
    logDebug("ensureLogsAndchildren() : childDefaults = ${childDefaults}")
    childDefaults.each { childDni,label ->
    	def cd = addChildDevice("matthammonddotorg", "Generic Custom Tile Component", childDni, [label:label, isComponent: true])
        cd.parse([[name:"tile",value:""]])
    }
}

def rebuildTiles() {
    logDebug("rebuilding tiles ${state.contactSensorLogs}")
    state.contactSensorLogs.each { sensorId, data ->
        logDebug "${sensorId} : ${data}"
        rebuildTile(data.childDni, data.openPeriods)
    }
}


/*
-----------------------------------------------------------------------------
Handle incoming contact sensor events
-----------------------------------------------------------------------------
*/

def onContactSensor(event) {
    logDebug("onContactSensor: ${event}")
    switch (event.name) {
        case "contact":
        	boolean isOpen = "${event.value}".toLowerCase() == "open"
            def device = event.getDevice();
        	def id = device.getId()
			long when = event.getDate().getTime() / 1000;

        	def data = state.contactSensorLogs[id]
        	def openPeriods = data["openPeriods"]
			long mergeInterval = settings.granularitySecs

	        logDebug("contact event: id=${id} when=${when} isOpen=${isOpen} data=${data} openPeriods=${openPeriods} mergeInterval = ${mergeInterval}")
        
        	if (!isOpen) {
                // contact closed ... add close time to most recent if not already got one
                if (openPeriods.size > 0) {
                    long opened = openPeriods[0][0]
                    long closed = openPeriods[0][1]
                    if (closed == -1L) {
                        openPeriods[0][1] = when
                    }
                }
                
            } else {
                // contact opened ... create new record ... unless too close to previous record or previous record does not include a "closed" time
                if (openPeriods.size == 0) {
                    // create new period
                    openPeriods.add(0, [ when, -1L ] )                    
                } else {
                    // check most recent previous event
                    long prevClosed = openPeriods[0][1]
                    if (prevClosed == -1L || prevClosed + mergeInterval >= when) {
                        // previous does not have a closed time, or it is too close ... merge by removing close time
                     	openPeriods[0][1] = -1L
                    } else {
                        // create new period
                        openPeriods.add(0, [ when, -1L ] )
                    }
                }
            }
            trimLogs()
        
        	rebuildTile(data.childDni, openPeriods)
        	break;
        
        default:
            break
    }
}

def trimLogs() {
    state.contactSensorLogs.each { sensorId, data ->
        logDebug "trimLogs() : ${sensorId} : ${data}"
        def openPeriods = data["openPeriods"]
        // purge oldest periods to keep list within size
        logDebug "trimLogs() : openPeriods.size() = ${openPeriods.size()} settings.maxLogSize = ${settings.maxLogSize}"
        while (openPeriods.size() > settings.maxLogSize) {
	        logDebug "trimLogs() : trimming"
            openPeriods.remove(openPeriods.size()-1)
        }
    }   
}

def friendlyDateFormatter(Date t) {
    def dateFormatter =  new SimpleDateFormat("EEE (d MMM)");
    def amPm = new SimpleDateFormat("a")
    boolean isAm = amPm.format(t) == "AM";
    def timeFormatter = new SimpleDateFormat( (isAm?"K":"h") + ":mm a")
    
    Date now = new Date()
    def dateToday = dateFormatter.format(now)
    def dateYesterday = dateFormatter.format(new Date(now.getTime() - 86400000L))

    def result = [
        date: dateFormatter.format(t),
        time: timeFormatter.format(t).toLowerCase()
    ]
    switch (result.date) {
        case dateToday:
	        result.date = "Today"
        	break
        case dateYesterday:
            result.date = "Yesterday"
        	break
        default:
            result.date = result.date
	        break
    }
    return result
}

def rebuildTile(String childDni, List openPeriods) {
	Map entries = [:]
    openPeriods.each { opened, closed ->
     	def d = friendlyDateFormatter(new Date(opened*1000L))
        logDebug("${childDni} opened at ${d}")
        if (!entries.containsKey(d.date))
        	entries.put(d.date, [])          // rely on Map remembering order that keys were put to create descending order by date
        entries[d.date].add(0, "<i>${d.time}</i>")       // sort in ascending chronological order within the day
    }
    
    String prefix = "<style>" +
        ".tile-primary:has(._csLog){vertical-align:top;}" +
        "._csLog {width:calc(100% - 16px);height:100%;overflow-y:scroll;position:absolute;}" +
        "._csLog p{text-align:left;white-space:normal;line-height:2em;margin:0.5em;font-size:0.8em;font-style:normal;}" +
        "._csLog i{background-color:${settings.pillColour};border-radius:0.6em;padding:0.1em 0.5em;font-style:normal;}" +
        "._csLog b,._csLog i{white-space:nowrap;}" +
        "</style>" +
    "<div class=\"_csLog\">"
    String postfix = "</div>"
    String body = entries.collect{date,times -> "<p><b>${date}:</b> ${times.join(" ")}"}.join("");
    int maxBodyLen = 1024 - prefix.size() - postfix.size()
    if (maxBodyLen < body.size()) {
        body = body.substring(0, maxBodyLen)
        body = body.substring(0, body.lastIndexOf("</i>") + "</i>".size())
    }
    String contents = prefix + body + postfix;
            
    logDebug("Updating child ${childDni} with: tile length=${contents.size()} contents = ${contents}")
    
    def cd = getChildDevice(childDni);
    logDebug(cd)
    cd.parse([[
        name:"tile",
        value:contents
    ]])
}

/*
-----------------------------------------------------------------------------
Child devices
-----------------------------------------------------------------------------
*/
def componentRefresh(cd) {
    return null
}
