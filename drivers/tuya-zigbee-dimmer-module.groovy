/* 
=============================================================================
Hubitat Elevation Driver for
Tuya Zigbee dimmer modules (1-Gang and 2-Gang)

    https://github.com/matt-hammond-001/hubitat-code

-----------------------------------------------------------------------------
This code is licensed as follows:

BSD 3-Clause License

Copyright (c) 2020, Matt Hammond
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

import groovy.transform.Field

@Field static def modelConfigs = [
    "_TYZB01_v8gtiaed": [
        inClusters: "0000,0004,0005,0006,0008",
        outClusters: "0019,000A",
        model: "TS110F",
        numEps: 2,
        joinName: "Tuya Zigbee 2-Gang Dimmer module"
    ],
    "_TYZB01_qezuin6k": [
        inClusters: "0000,0004,0005,0006,0008",
        outClusters: "0019,000A",
        model: "TS110F",
        numEps: 1,
        joinName: "Tuya Zigbee 1-Gang Dimmer module"
    ],
    "_TZE200_e3oitdyu": [
        inClusters: "0000,0004,0005,EF00",
        outClusters: "0019,000A",
        model: "TS0601",
        numEps: 2,
        joinName: "Moes 2-Gang Dimmer module"
    ],
]
    
def config() {
    return modelConfigs[device.getDataValue("manufacturer")]
}

metadata {
    definition (
        name: "Tuya Zigbee dimmer module",
        namespace: "matthammonddotorg",
        author: "Matt Hammond",
        description: "Driver for Tuya zigbee dimmer modules",
        documentationLink: "https://github.com/matt-hammond-001/hubitat-code/blob/master/drivers/tuya-zigbee-dimmer-module.README.md"
    ) {

        capability "Configuration"
        capability "Refresh"
        capability "Light"
        capability "Switch"
        capability "SwitchLevel"
        
        command "toggle"
        
        modelConfigs.each{ data ->
            fingerprint profileId: "0104",
                inClusters: data.value.inClusters,
                outClusters: data.value.outClusters,
                model: data.value.model,
                manufacturer: data.key,
                deviceJoinName: data.value.joinName
        }
        
    }
    
    preferences {
        input "minLevel",
            "number",
            title: "Minimum level",
            description: "Minimum brightness level (%). 0% on the dimmer level is mapped to this.",
            required: true,
            multiple: false,
            defaultValue: 0
        
        if (minLevel < 0) {
            minLevel = 0
        } else if (minLevel > 99) {
            minLevel = 99
        }

        
        input "autoOn",
            "bool",
            title: "Turn on when level adjusted",
            description: "Switch turns on automatically when dimmer level is adjusted.",
            required: true,
            multiple: false,
            defaultValue: true
        
        input "debugEnable", "bool", title: "Enable debug logging", required: false
    }
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
    if (settings.debugEnable) {
        log.info msg
    }
}



/*
-----------------------------------------------------------------------------
Standard handlers
-----------------------------------------------------------------------------
*/

def installed() {
    return initialized()
}

def configure() {
    return initialized()
}

def initialize() {
    return initialized()
}

def updated() {
    if (isParent()) {
        logInfo "updated parent->child"
        getChildByEndpointId(indexToEndpointId(0)).onParentSettingsChange(settings)
    } else {
        logInfo "updated child->parent"
        parent?.onChildSettingsChange(device.deviceNetworkId, settings)
    }
    return initialized()
}

def initialized() {
    def cmds = []
    logDebug device.getData()
    
    if (isParent()) {
        createChildDevices()   
        cmds += listenChildDevices()
    }
    return cmds
}

/*
-----------------------------------------------------------------------------
Setting up child devices
-----------------------------------------------------------------------------
*/

def createChildDevices() {
    def numEps = config().numEps
    
    if (numEps == 1) {
        numEps = 0
    }
    
    while (getChildDevices().size() > numEps) {
        // delete child device
        def i = getChildDevices().size()-1
        def dni = indexToChildDni(i)
        
        logInfo "Deleting child ${i}"
        deleteChildDevice(dni)
    }
           
    while (getChildDevices().size() < numEps) {
        // create child devices
        def i = getChildDevices().size()
        def endpointId = indexToEndpointId(i)
        def dni = indexToChildDni(i)
        logInfo "Creating child ${i} with dni ${dni}"
        
        addChildDevice(
            "Tuya Zigbee dimmer module",
            dni,
            [
                completedSetup: true,
                label: "${device.displayName} (CH${endpointId})",
                isComponent: true,
                componentName: "ch${endpointId}",
                componentLabel: "Channel ${endpointId}"
            ]
        )
    }               
}


def listenChildDevices() {
    def cmds = []
    getChildEndpointIds().each{ endpointId ->
        cmds += [
            //bindings
            "zdo bind 0x${device.deviceNetworkId} 0x${endpointId} 0x01 0x0006 {${device.zigbeeId}} {}", "delay 200",
            "zdo bind 0x${device.deviceNetworkId} 0x${endpointId} 0x01 0x0008 {${device.zigbeeId}} {}", "delay 200",
            //reporting
            "he cr 0x${device.deviceNetworkId} 0x${endpointId} 0x0006 0 0x10 0 0xFFFF {}","delay 200",
            "he cr 0x${device.deviceNetworkId} 0x${endpointId} 0x0008 0 0x20 0 0xFFFF {}", "delay 200",
        ] + cmdRefresh([endpointId])
    }
    return cmds
}

/*
-----------------------------------------------------------------------------
Propagate settings changes both ways between Parent and First Child
-----------------------------------------------------------------------------
*/

def onChildSettingsChange(childDni, childSettings) {
    def i = endpointIdToIndex(childDniToEndpointId(childDni))
    logInfo "onChildSettingsChange: ${i}"
    if (i==0) {
        device.updateSetting("minLevel",childSettings.minLevel)
        device.updateSetting("autoOn",childSettings.autoOn)
    }
}

def onParentSettingsChange(parentSettings) {
    device.updateSetting("minLevel",parentSettings.minLevel)
    device.updateSetting("autoOn",parentSettings.autoOn)
}
 
/*
-----------------------------------------------------------------------------
Command handlers

if child, then ask parent to act on its behalf
if parent, then act on endpoint 1
-----------------------------------------------------------------------------
*/

def refresh() {
    if (isParent()) {
        return cmdRefresh(indexToChildDni(0))
    } else {
        parent?.doActions( parent?.cmdRefresh(device.deviceNetworkId) )
    }
}
        
def on() {
    if (isParent()) {
        return cmdSwitch(indexToChildDni(0), 1)
    } else {
        parent?.doActions( parent?.cmdSwitch(device.deviceNetworkId, 1) )
    }
}

def off() {
    if (isParent()) {
        return cmdSwitch(indexToChildDni(0), 0)
    } else {
        parent?.doActions( parent?.cmdSwitch(device.deviceNetworkId, 0) )
    }
}

def toggle() {
    if (isParent()) {
        return cmdSwitchToggle(indexToChildDni(0))
    } else {
        parent?.doActions( parent?.cmdSwitchToggle(device.deviceNetworkId) )
    }
}

def setLevel(level, duration=0) {
    if (isParent()) {
        def value = levelToValue(level)
        return cmdSetLevel(indexToChildDni(0), value, duration)
    } else {
        def value = levelToValue(level)
        parent?.doActions( parent?.cmdSetLevel(device.deviceNetworkId, value, duration) )
    }
}



/*
-----------------------------------------------------------------------------
Hub Action (cmd) generators
-----------------------------------------------------------------------------
*/


def cmdRefresh(String childDni) {
    def endpointId = childDniToEndpointId(childDni)
    return [
        "he rattr 0x${device.deviceNetworkId} 0x${endpointId} 0x0006 0 {}",
        "delay 100",
        "he rattr 0x${device.deviceNetworkId} 0x${endpointId} 0x0008 0 {}",
        "delay 100"
    ]
}
    
def cmdSwitchToggle(String childDni) {
    def endpointId = childDniToEndpointId(childDni)
    return [
        "he cmd 0x${device.deviceNetworkId} 0x${endpointId} 0x0006 2 {}",
        "delay 500"
    ] + cmdRefresh(childDni)
}

def cmdSwitch(String childDni, onOff) {
    def endpointId = childDniToEndpointId(childDni)
    onOff = onOff ? "1" : "0"

    return [
        "he cmd 0x${device.deviceNetworkId} 0x${endpointId} 0x0006 ${onOff} {}"
    ]
}

def cmdSetLevel(String childDni, value, duration) {
    def endpointId = childDniToEndpointId(childDni)
    value = value.toInteger()
    value = value > 255 ? 255 : value
    value = value < 1 ? 0 : value

    duration = (duration * 10).toInteger()
    def child = getChildByEndpointId(endpointId)
        
    def cmd = [
        "he cmd 0x${device.deviceNetworkId} 0x${endpointId} 0x0008 4 { 0x${intTo8bitUnsignedHex(value)} 0x${intTo16bitUnsignedHex(duration)} }",
    ]
    
    if (child.isAutoOn()) {
        cmd += "he cmd 0x${device.deviceNetworkId} 0x${endpointId} 0x0006 1 {}"
    }
    return cmd
}

/*
-----------------------------------------------------------------------------
Parent only code
-----------------------------------------------------------------------------
*/

def doActions(List<String> cmds) {
    if (isParent()) {
        hubitat.device.HubMultiAction allActions = new hubitat.device.HubMultiAction()
        
        cmds.each { it ->
            if(it.startsWith('delay') == true) {
                allActions.add(new hubitat.device.HubAction(it, hubitat.device.Protocol.DELAY))
            } else {
                allActions.add(new hubitat.device.HubAction(it, hubitat.device.Protocol.ZIGBEE))
            }
        }
        logDebug "Sending actions: ${cmds}"
        sendHubCommand(allActions)
    } else {
        throw new Exception("doActions() called incorrectly by child")
    }
}


def parse(String description) {
    logInfo "Received raw: ${description}"

    if (isParent()) {
        def descMap = zigbee.parseDescriptionAsMap(description)
      logDebug "Received parsed: ${descMap}"

        if (description.startsWith("catchall")) return
    
        def value = Integer.parseInt(descMap.value, 16)
        def child = getChildByEndpointId(descMap.endpoint)
        def isFirst = 0 == endpointIdToIndex(descMap.endpoint)
            
        switch (descMap.clusterInt) {
            case 0x0006: // switch state
                child.onSwitchState(value)
                if (isFirst && child != this) {
                    logDebug "Replicating switchState in parent"
                    onSwitchState(value)
                } else {
                    logDebug "${isFirst} ${this} ${child} ${value}"
                }
                break
            case 0x0008: // switch level state
                child.onSwitchLevel(value)
                if (isFirst && child != this) {
                    logDebug "Replicating switchLevel in parent"
                    onSwitchLevel(value)
                }
                break
        }
    } else {
        throw new Exception("parse() called incorrectly by child")
    }
}

/*
-----------------------------------------------------------------------------
Child only code
-----------------------------------------------------------------------------
*/

def onSwitchState(value) {
    def valueText = value==1?"on":"off"
    sendEvent(name:"switch", value: valueText, descriptionText:"${device.displayName} set ${valueText}", unit: null)
}

def onSwitchLevel(value) {
    logDebug "onSwitchLevel: value=${value}"
    def level = valueToLevel(value.toInteger())
    logDebug "onSwitchLevel: Value=${value} level=${level}"
    
    sendEvent(name:"level", value: level, descriptionText:"${device.displayName} set ${level}%", unit: "%")
}
    
/*
-----------------------------------------------------------------------------
Parent only helper functions
-----------------------------------------------------------------------------
*/
    
@Field static def childDniPattern = ~/^([0-9A-Za-z]+)-([0-9A-Za-z]+)$/

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// mappings between index, endpointId and childDni
// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

def indexToEndpointId(i) {
    return intTo8bitUnsignedHex(i+1)
}

def endpointIdToIndex(i) {
    return Integer.parseInt(i,16) - 1
}

def endpointIdToChildDni(endpointId) {
    return "${device.deviceNetworkId}-${endpointId}"
}

def indexToChildDni(i) {
    return endpointIdToChildDni(indexToEndpointId(i))
}

def childDniToEndpointId(childDni) {
    def match = childDni =~ childDniPattern
    if (match) {
        if (match[0][1] == device.deviceNetworkId) {
            return match[0][2]
        }
    }
    return null
}

def childDnisToEndpointIds(List<String> childDnis) {
    return childDnis.collect{cDni -> childDniToEndpointId(cDni)}
}

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// retrieving children, or retrieving parent if no children and endpointId
// is first index
// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

def getChildByEndpointId(endpointId) {
    if (endpointIdToIndex(endpointId) == 0 && getChildDevices().size() == 0) {
        return this
    } else {
        return getChildDevice(endpointIdToChildDni(endpointId))
    }
}

def getChildEndpointIds() {
    if (getChildDevices().size() == 0) {
        return indexToEndpointId(0)
    } else {
        return getChildDevices().collect{childDniToEndpointId(it.getDni())}
    }
}

/*
-----------------------------------------------------------------------------
Child only helper functions
-----------------------------------------------------------------------------
*/

def getDni() {
    return device.deviceNetworkId
}

def isAutoOn() {
    return settings.autoOn
}



def levelToValue(BigDecimal level) {
    return levelToValue(level.toInteger())
}

def levelToValue(Integer level) {
    Integer minValue = Math.round(settings.minLevel*2.55)
    return rescale(level, 0, 100, minValue, 255)
}

def valueToLevel(BigDecimal value) {
    return valueToLevel(value.toInteger())
}

def valueToLevel(Integer value) {
    Integer minValue = Math.round(settings.minLevel*2.55)
    if (value < minValue) {
        return 0
    } else {
        return rescale(value, minValue, 255, 0, 100)
    }
}


/*
-----------------------------------------------------------------------------
Shared helper functions
-----------------------------------------------------------------------------
*/

def isParent() {
    return getParent() == null
}

def rescale(value, fromLo, fromHi, toLo, toHi) {
    return Math.round((((value-fromLo)*(toHi-toLo))/(fromHi-fromLo)+toLo))
}

def intTo16bitUnsignedHex(value) {
    def hexStr = zigbee.convertToHexString(value.toInteger(),4)
    return new String(hexStr.substring(2, 4) + hexStr.substring(0, 2))
}

def intTo8bitUnsignedHex(value) {
    return zigbee.convertToHexString(value.toInteger(), 2)
}
