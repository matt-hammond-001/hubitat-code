/* 
=============================================================================
Hubitat Elevation Application
Aqara Cube dimmer controller

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

definition(
	name: "Aqara Cube Dimmer",
	namespace: "matthammonddotorg",
	author: "Matt Hammond",
	description: "Uses Aqara Cube to control dimmers",
    documentationLink: "",
    singleInstance: true,
    iconUrl: "",
    iconX2Url: "",
)

@Field static def faceDescription = [
    0: " ('aqara' logo)",
    1: " (right of logo)",
    2: " (above logo)",
    3: " (opposite logo)",
    4: " (left of logo)",
    5: " (below logo)"
]
    
preferences {
    page(name: "mainPage", title: "Aqara Cube Dimmer", install: true, uninstall: true,submitOnChange: true) {
        
        section("Which aqara cube to use as controller") {
            input "cubeDev",
                "capability.pushableButton",
                title: "Use which Aqara cube?",
                multiple: true,
                required: true,
                submitOnChange: true
        }
        
        section("Dimmers to be controlled") {
            paragraph("Rotate left (anti-clockwise) dims.<br/>Rotate right (clockwise) brightens.<br/>Knocking twice on surface toggles on/off")
            for (n in 0..5) {
                input "dimmer${n}",
                    "capability.switchLevel",
                    title: "Dimmer for cube face ${n}${faceDescription[n]}",
                    multiple: false,
                    required: false,
                    submitOnChange: true
            }
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

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
}

def initialize() {
    unsubscribe()
    subscribe(cubeDev, "pushed", onButtonPush)
    state.lastLevel = [0,0,0,0,0,0]
    state.lastWhen  = [0,0,0,0,0,0]
    state.lastOtherWhen = 0
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
Reacting to button pushes
-----------------------------------------------------------------------------
*/

@Field static def cacheAgeMillis = 7*1000
@Field static def ignoreTimeoutMillis = 300

def onButtonPush(evt) {
    now = new Date().getTime()
    logDebug "now=${now} state.lastOtherWhen=${state.lastOtherWhen}"
    data = parseJson(evt.getData())
    logDebug "Received: ${evt} || ${evt.name} || ${evt.value} ~~ ${data}"
    if ((evt.value == "6" || evt.value == "7") && data.containsKey("angle") && data.containsKey("face")) {
        angle = data.angle
        face = data.face
        logInfo "Face ${face} rotated ${angle}"
        if (state.lastOtherWhen < now - ignoreTimeoutMillis) {
            doAdjust(face, angle)
        }
    } else if (data.containsKey("face") && evt.value=="5") {
        face = data.face
        logInfo "Face ${face} knocked"
        if (state.lastOtherWhen < now - ignoreTimeoutMillis) {
            doToggle(face)
        }
    } else {
        // unrecognised
        logInfo "Unrecognised - ${evt.value}"
        state.lastOtherWhen = now
    } 
}

def doAdjust(face, angle) {
    now = new Date().getTime()
    device = settings["dimmer${face}"]
    if (!device) {
        logDebug "No device assigned for face ${face}"
        return
    }
    logDebug "state = ${state}"
    logDebug "device = ${device}  device.level=${device.currentValue('level')}"
    
    if (state.lastWhen[face] == 0 || state.lastWhen[face] < now - cacheAgeMillis) {
        level = device.currentValue('level')
    } else {
        level = state.lastLevel[face]
    }
    
    if (angle>0) {
        level = level + 25
        if (level > 100) { level = 100 }
    } else if (angle<0) {
        level = level - 25
        if (level < 0) { level = 0 }
    }
    
    device.setLevel(level, 0)
    state.lastLevel[face] = level
    state.lastWhen[face] = now
}

def doToggle(face) {
    device = settings["dimmer${face}"]
    if (!device) {
        logDebug "No device assigned for face ${face}"
        return
    }
    device.toggle()
}
