/**
 * =============================================================================
 * 
 *  Custom Doorbell Device Driver for Hubitat Elevation hub
 *
 *  Version 0.0.2
 *
 *  This driver is for an ESP32-C6 based zigbee doorbell hack
 *  https://github.com/matt-hammond-001/zigbee-esp32c6-doorbell
 *
 *  (c) 2026 Matt Hammond / matthammond.org
 * 
 *  https://github.com/matt-hammond-001/hubitat-code
 *
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

metadata {
	definition (
		name: "matthammond.org Doorbell",
		namespace: "matthammonddotorg",
		author: "Matt Hammond",
		description: "Custom Zigbee doorbell driver",
		documentationLink: "https://github.com/matt-hammond-001/hubitat-code/blob/master/drivers/matthammond.org-doorbell.README.md"
	) {
		capability "PresenceSensor"
        capability "MotionSensor"
        capability "PushableButton"
        capability "ReleasableButton"
        capability "Configuration"
        capability "Sensor"

        attribute "lastCheckinEpoch", "String"
        attribute "lastCheckinTime", "String"
        attribute "switch", "String"
        attribute "motion", "String"
        attribute "presence", "String"

        fingerprint profileId: "0104", inClusters: "0000,0003,0006", outClusters: "", manufacturer: "matthammond.org", model: "doorbell.v1", deviceJoinName: "Doorbell v1"
	}

	preferences {
        input "debugEnable", "bool", title: "Enable debug logging", required: false
        input "autoRelease", "bool", title: "Automatically release when pushed manually", required: true, defaultValue: true
        input "autoReleaseDelay", "number", title: "Auto release delay (seconds)", required: true, defaultValue: 5
	}
}

/*
-----------------------------------------------------------------------------
Logging output
-----------------------------------------------------------------------------
*/

def logDebug(msg) {
    if (settings.debugEnable) {
        log.debug "${device.displayName}: ${msg}"
    }
}

def logInfo(msg) {
    if (settings.debugEnable) {
        log.info "${device.displayName}: ${msg}"
    }
}


/*
-----------------------------------------------------------------------------
Standard handlers
-----------------------------------------------------------------------------
*/

def installed() {
    return cmdConfigure()
}

def configure() {
    return cmdConfigure()
}

def initialize() {
    return cmdConfigure()
}

def updated() {
    if (settings.autoReleaseDelay < 1) {
        settings.autoReleaseDelay = 1
    }
}

/*
-----------------------------------------------------------------------------
Command handlers in UI
-----------------------------------------------------------------------------
*/

void push(buttonNumber) {
    doPush()
    if (settings.autoRelease) {
        def millis = settings.autoReleaseDelay * 1000
        if (millis < 1000) {
            millis = 1000
        }
	    runInMillis(millis, doRelease, [overwrite:true])
    }
}

void release(buttonNumber) {
    runInMillis(50, doRelease, [overwrite:true]);
}

/*
-----------------------------------------------------------------------------
Action implementations
-----------------------------------------------------------------------------
*/


def cmdRefresh() {
    return [
        "he rattr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x0006 0 {}",
        "delay 100",
    ]
}

def cmdConfigure() {
    def cmds = []
    logDebug device.getData()
    
    sendEvent(name: "numberOfButtons", value: 1)
    sendEvent(name: "switch", value: "off")
    sendEvent(name: "presence", value: "not present")

    return [
        // bindings
        "zdo bind 0x${device.deviceNetworkId} 0x${device.endpointId} 0x01 0x0006 {${device.zigbeeId}} {}", "delay 200",

        // reporting
        "he cr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x0006 0 0x10 0 0xFFFF {}","delay 200",

    ] + cmdRefresh()
}

def doPush() {
    doPushOrRelease("pushed", "on", "active", "present")
}

def doRelease() {
    doPushOrRelease("released", "off", "inactive", "not present")
}

def doPushOrRelease(eventName, switchState, motionState, presenceState) {
    logDebug("doPushOrRelease( ${eventName}, ${switchState}, ${motionState}, ${presenceState} )")
    logDebug("switch currentValue = '${device.currentValue("switch")}'")
//    def eventName = pushed ? "pushed" : "released"
//    def switchState = pushed ? "on" : "off"
//    def motionState = pushed ? "active" : "inactive"
//    def presenceState = pushed ? "present" : "not present"
    if (device.currentValue("switch") != switchState) {
        logDebug("doPushOrRelease - changing state and sending events")
        sendEvent([ name: "${eventName}", value: 1, isStateChange: true /* value always same, so force it to notify */, descriptionText: "Button was ${eventName}" ])
        sendEvent([ name: "switch", value: "${switchState}", descriptionText: "Switch was ${switchState}" ])
        sendEvent([ name: "motion", value: "${motionState}", descriptionText: "Motion was ${motionState}" ])
        sendEvent([ name: "presence", value: "${presenceState}", descriptionText: "Presence was ${presenceState}" ])
    }
    return [:]
}

/*
-----------------------------------------------------------------------------
Parse incoming device messages to generate events
-----------------------------------------------------------------------------
*/

def parse(String description) {
    sendEvent(name: "lastCheckinEpoch", value: now())
	sendEvent(name: "lastCheckinTime", value: new Date().toLocaleString())
    
	logDebug "Parsing message: ${description}"
    if (description.startsWith("catchall")) return
    
    def descMap = zigbee.parseDescriptionAsMap(description)
    def rawValue = Integer.parseInt(descMap.value,16)
    
    switch (descMap.clusterInt) {
        case 6: // switch
            if (rawValue == 1) {
                return doPush()
            } else {
                return doRelease()
            }
			break
        default: // unknown
            logDebug "Cannot parse for unknown cluster ${descMap.clusterInt}"
        	break
    }
    
    return [:]
}
