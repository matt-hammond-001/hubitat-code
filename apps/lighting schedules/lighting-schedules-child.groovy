/* 
=============================================================================
Hubitat Elevation Application
Lighting scheduler (child application)

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
	name: "Lighting Schedule",
	namespace: "matthammonddotorg",
    parent: "matthammonddotorg/parent:Lighting Schedules",
	author: "Matt Hammond",
	description: "Controls switches to a timing schedule",
    documentationLink: "https://github.com/matt-hammond-001/hubitat-code/blob/master/apps/lighting%20schedules/README.md",
    iconUrl: "",
    iconX2Url: "",
)

preferences {
	page(name: "main")
}

def main(){
    dynamicPage(name: "main", title: "Lighting Schedule", uninstall: true, install: true){
        section("<b>General</b>") {
            input "appLabel",
                "text",
                title: "Name for this application",
                multiple: false,
                required: true,
                submitOnChange: true

            input "paused",
                "bool",
                title: "Pause this schedule",
                multiple: false,
                required: true,
                defaultValue: false

            input "switches",
                "capability.switch",
                title: "Control which switches?",
                multiple: true,
                required: false,
                submitOnChange: true
        }
        section("<b>Switching behaviour</b>") {
            input "periodBehaviour",
                "enum",
                title: "During specified periods...",
                multiple: false,
                required: true,
                defaultValue:"duringOn",
                options: [
                    "duringOn" : "Check and turn ON regularly.",
                    "entryOn": "Check and turn ON only when was previously not in a period.",
                    "duringOff" : "Check and turn OFF regularly.",
                    "entryOff": "Check and turn OFF only when was previously not in a period.",
                    "ignore": "Do nothing",
                ]

            input "noPeriodBehaviour",
                "enum",
                title: "At other times...",
                multiple: false,
                required: true,
                defaultValue:"duringOff",
                options: [
                    "duringOn" : "Check and turn ON regularly.",
                    "entryOn": "Check and turn ON only when was previously in a period.",
                    "duringOff" : "Check and turn OFF regularly.",
                    "entryOff": "Check and turn OFF only when was previously in a period.",
                    "ignore": "Do nothing",
                ]

            input "activeAlways",
                "bool",
                title: "Active in all modes",
                multiple: false,
                required: true,
                defaultValue: true,
                submitOnChange: true
            
            if (activeAlways!=null && !activeAlways) {
                input "activeModes",
                    "mode",
                    title: "Active in these modes (can choose more than one)",
                    multiple: true,
                    required: false

                input "activateBehaviour",
                    "enum",
                    title: "When becoming ACTIVE due to a MODE change...",
                    multiple: false,
                    required: true,
                    defaultValue: "both",
                    options: [
                        "both": "Immediately turn ON or OFF according to the schedule",
                        "on": "Immediately turn ON only, according to the schedule",
                        "off": "Immediately turn OFF only, according to the schedule",
                        "nothing": "Do nothing immediately.",
                    ]

                input "deactivateBehaviour",
                    "enum",
                    title: "When becoming INACTIVE due to a MODE change...",
                    multiple: false,
                    required: true,
                    defaultValue: "nothing",
                    options: [
                        "allOn": "Turn all ON immediately",
                        "allOff": "Turn all OFF immediately",
                        "nothing": "Do nothing.",
                    ]
            }
        }
        section("<b>Timings</b>") {
            paragraph """\
                <div style="color: #888; background-color: #f8f8f8; margin-left: 1em; margin-right: 1em; padding: 0.5em;"><i>Specify timing periods to be used with the rules described above.\
                Here are some examples of how they can be written:<ul>\
                    <li> 0900-1120\
                    <li> 0900-1120; 1500-1600, 2300-0500\
                    <li> 12:00-13:00, 15:25-15:26\
                    <li> sunrise-12:00, 15:00-sunset+30\
                    <li> sunrise-30 - 0800, 1200-1300, sunset-25 - 2359\
                </ul></i></div>\
                """.stripIndent()
            
            switches.each {
                dev ->
                    def fieldName = "devTimings-$dev.id"
                    def info = ""
                    def value = settings[fieldName]
                    if (value != "" && value != null && !validateTimings(value)) {
                        info = "&nbsp;&nbsp;<span style='color: red'>Value not valid</span>"
                    }
                    def title = dev.label != null && dev.label != "" ? dev.label : dev.name
                    
                    input fieldName,
                        "text",
                        title: "<span style='font-size:smaller; color: #888'>${title}</span>"+info,
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
        }
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
    if (settings.infoEnable) {
        log.info msg
    }
}

/*
-----------------------------------------------------------------------------
Regex for matching time period patterns
-----------------------------------------------------------------------------

Matches: TIMESPEC-TIMESPEC REMAINDER

TIMESPEC = any of:
             HHMM
             HH:MM
             sunrise
             sunset
             sunrise[+-]OFFSET
             sunset[+-]OFFSET
HH     := [0-2][0-9]
MM     := [0-5][0-9]
OFFSET := 0|(?:[1-9][0-9]{0,2})    .... between 1 and 3 digits, always starting with a non zero, or just zero

restricting format of OFFSET in this way means it will never match the 4 digit pattern or HHMM or HH:MM ... hopefully
*/

@Field static def timeRangePattern = ~/^[;, ]*(?:(?:([0-2][0-9])[:.]?([0-5][0-9]))|(?:(sunrise|sunset)( *[+-] *(?:0|(?:[1-9][0-9]{0,2})))?)) *(?:-|to) *(?:(?:([0-2][0-9])[:.]?([0-5][0-9]))|(?:(sunrise|sunset)( *[+-] *(?:0|(?:[1-9][0-9]{0,2})))?))[;, ]*(.*)$/

/*
-----------------------------------------------------------------------------
Standard handlers, and mode-change handler
-----------------------------------------------------------------------------
*/

def installed() {
    logDebug "installed()"
    state.wasInPeriod = [:]
    state.wasActive = null
    initialize()
}


def updated() {
    logDebug "updated()"
    unsubscribe()
    initialize()
}


def initialize() {
    logDebug "initialize()"
    subscribe location, "mode", modeChangeHandler
    update(true)
}

def uninstalled() {
    logDebug "uninstalled()"
}


def modeChangeHandler(evt) {
    update(true)
}

/**
 * if a switch becomes unselected by the user, the dynamically created settings entry is not automatically deleted by hubitat
 * so this method cleans them up, both permanently by calling app.removeSeting, and also immediately for the subsequent update() code
 * by also removing from the temporary current settings object
 */
def scrubUnusedSettings() {
    settings.findAll {
        it -> it.key =~ /devTimings-[0-9]+/
    }.findAll {
        it -> ! switches.any {
            dev -> it.key == "devTimings-$dev.id"
        }
    }.each {
        it ->
            app.removeSetting it.key
            settings.remove it.key
    }
}

/*
-----------------------------------------------------------------------------
Whenever there is a change/update
-----------------------------------------------------------------------------
*/

def update(modeChanged=false) {
    def pauseText = "";
    if (settings.paused) {
        pauseText = ' <span style="color: red;">(Paused)</span>'
    }
    if (settings.appLabel) {
        app.updateLabel("${settings.appLabel}${pauseText}")
    } else {
        app.updateLabel("Schedule${pauseText}")
    }
    scrubUnusedSettings()

    logDebug "update() - paused=${paused} activeAlways=${activeAlways} mode=${location.mode} modeChanged=${modeChanged}"
    def isActive = !paused && (activeAlways || activeModes.any {v -> v == location.mode})
    
    if (isActive) {
        logDebug "Active in mode $location.mode"
        if (modeChanged) {
            runEvery1Minute(updateLights)
        }
    } else {
        logDebug "Inactive in mode $location.mode"
        unschedule(updateLights)
    }

    updateLights()
}

/*
-----------------------------------------------------------------------------
Determine and make changes
-----------------------------------------------------------------------------
*/

def updateLights() {
    if (paused) {
        return
    }
    def isActive = activeAlways || activeModes.any {v -> v == location.mode}
    def activeChanged = isActive != state.wasActive

    def timings = buildTimings()
    def now = Calendar.getInstance()
    
    def nowInPeriod = [:]
    
    switches.each{ dev ->
        def ID = ""+dev.deviceId

        def inPeriod = isInPeriod(timings[ID], now)
        def isOn = dev.currentValue('switch', true) == 'on'
        def isOff = !isOn

        def wasInPeriod;
        if (state.wasInPeriod == null) {
            wasInPeriod = inPeriod
        } else {
            wasInPeriod = state.wasInPeriod[ID]
        }

        nowInPeriod[ID] = inPeriod
        
        logDebug "Checking for device ID $ID ... isOn: $isOn, inPeriod: $inPeriod, wasInPeriod: $wasInPeriod"
        
        if (activeChanged) {
            if (isActive) {  // has been activated
                if (inPeriod) {
                    switch (settings.periodBehaviour) {
                        case "duringOn":
                        case "entryOn":
                            if (isOff && settings.activateBehaviour == "both" || settings.activateBehaviour == "on") {
                                logInfo "Turning ON $dev.deviceId : $dev.name / $dev.label (activation during period)"
                                dev.on();
                            }
                            break;
                        case "duringOff":
                        case "entryOff":
                            if (isOn && settings.activateBehaviour == "both" || settings.activateBehaviour == "off") {
                                logInfo "Turning OFF $dev.deviceId : $dev.name / $dev.label (activation during period)"
                                dev.off();
                            }
                            break;
                        case "ignore":
                            break;
                    }
                } else { // not in period
                    switch (settings.noPeriodBehaviour) {
                        case "duringOn":
                        case "entryOn":
                            if (isOff && settings.activateBehaviour == "both" || settings.activateBehaviour == "on") {
                                logInfo "Turning ON $dev.deviceId : $dev.name / $dev.label (activation outside period)"
                                dev.on();
                            }
                            break;
                        case "duringOff":
                        case "entryOff":
                            if (isOn && settings.activateBehaviour == "both" || settings.activateBehaviour == "off") {
                                logInfo "Turning OFF $dev.deviceId : $dev.name / $dev.label (activation outside period)"
                                dev.off();
                            }
                            break;
                        case "ignore":
                            break;
                    }
                }
            } else { // has been deactivated
                switch (settings.deactivateBehaviour) {
                    case "allOn":
                        logInfo "Turning ON $dev.deviceId : $dev.name / $dev.label (deactivation)"
                        dev.on();
                        break;
                    case "allOff":
                        logInfo "Turning OFF $dev.deviceId : $dev.name / $dev.label (deactivation)"
                        dev.off();
                        break;
                    case "nothing":
                        break;
                }
            }
        } else { // no change of activation state
            if (isActive) {
                if (inPeriod) {
                    if (wasInPeriod) {
                        // during period
                        if (settings.periodBehaviour == "duringOn" && isOff) {
                            logInfo "Turning ON $dev.deviceId : $dev.name / $dev.label (during period)"
                            dev.on()
                        } else if (settings.periodBehaviour == "duringOff" && isOn) {
                            logInfo "Turning OFF $dev.deviceId : $dev.name / $dev.label (during period)"
                            dev.off()
                        }
                    } else {
                        // transitioned into period
                        if ((settings.periodBehaviour == "entryOn" || settings.periodBehaviour == "duringOn") && isOff) {
                            logInfo "Turning ON $dev.deviceId : $dev.name / $dev.label (entering into period)"
                            dev.on()
                        } else if ((settings.periodBehaviour == "entryOff" || settings.periodBehaviour == "duringOff") && isOn) {
                            logInfo "Turning OFF $dev.deviceId : $dev.name / $dev.label (entering into period)"
                            dev.off()
                        }
                    }
                } else {
                    if (wasInPeriod) {
                        // transitioned out of period
                        if ((settings.noPeriodBehaviour == "entryOn" || settings.noPeriodBehaviour == "duringOn") && isOff) {
                            logInfo "Turning ON $dev.deviceId : $dev.name / $dev.label (entering out of period)"
                            dev.on()
                        } else if ((settings.noPeriodBehaviour == "entryOff" || settings.noPeriodBehaviour == "duringOff") && isOn) {
                            logInfo "Turning OFF $dev.deviceId : $dev.name / $dev.label (entering out of period)"
                            dev.off()
                        }
                    } else {
                        // during out of period
                        if (settings.noPeriodBehaviour == "duringOn" && isOff) {
                            logInfo "Turning ON $dev.deviceId : $dev.name / $dev.label (during out of period)"
                            dev.on()
                        } else if (settings.noPeriodBehaviour == "duringOff" && isOn) {
                            logInfo "Turning OFF $dev.deviceId : $dev.name / $dev.label (during out of period)"
                            dev.off()
                        }
                    }
                }
            } else {
                // inactive. do nothing
            }
        }
    }
    state.wasInPeriod = nowInPeriod
    state.wasActive = isActive
}

/*
-----------------------------------------------------------------------------
Helper functions
-----------------------------------------------------------------------------
*/

/* given array of [start,end] timings, and time now, check if should be on
*/
def isInPeriod(timings, now) {
    return true == timings.any{ start,end -> 
        start.before(now) && end.after(now)
    }
}


/* return a map of device IDs to parsed timings (where timings is a list of [start,end] timings */
def buildTimings() {
    def timings = [:]
    
     // build timings map from settings entries
    getDeviceTimingStrings { devId, key, value ->
        try {
            timings[devId] = parseTimings(value)
        } catch (e) {
            logDebug "Failure parsing: $value : "+e.getMessage()
            timings[devId] = []
        }
    }
    return timings
}

/* Get time periods string for each device, calling callpack with the device ID, the settings key, and the string */
def getDeviceTimingStrings(cb) {
    for (entry in settings) {
        def match = entry.key =~ /^devTimings-([0-9]+)$/
        if (match) {
           def devId = "" + match[0][1]
           cb(devId, entry.key, entry.value)
        }
    }
}



def validateTimings(timingString) {
    while (timingString) {
        def match = timingString =~ timeRangePattern
        if (!match) {
            return false
        }
        timingString = match[0][9]
    }
    return true
}

def parseTimings(timingString) {
    def timings = []
    
    def srss = getSunriseAndSunset()
    today = {}
    today.now = Calendar.getInstance()
    today.midday = today.now.clone()
    today.midday.set(Calendar.HOUR_OF_DAY, 12)
    today.midday.set(Calendar.MINUTE, 0)
    today.midday.set(Calendar.SECOND, 0)
    today.sunrise = Calendar.getInstance()
    today.sunrise.setTime(srss.sunrise)
    today.sunset = Calendar.getInstance()
    today.sunset.setTime(srss.sunset)
    
    while (timingString) {
        def match = timingString =~ timeRangePattern
        if (match) {
            (_, sH, sM, sR, sO, eH, eM, eR, eO, remainder) = match[0]
            (startTime, endTime) = calcStartEndTimes(today, sH, sM, sR, sO, eH, eM, eR, eO)
            
            if (startTime.before(endTime)) {
                timings += [[ startTime, endTime ]]
            }
            timingString = remainder

        } else {
            timingString = ""
        }
    }
    timings.sort()
    return timings
}

@Field static def ABS = 0
@Field static def SUNRISE = 1
@Field static def SUNSET = 2
    

def calcStartEndTimes(today, sH, sM, sR, sO, eH, eM, eR, eO) {
    (start, sType) = calcTime(today, sH, sM, sR, sO)
    (end, eType)   = calcTime(today, eH, eM, eR, eO)

    // intelligently handle edge cases when one timespec is relative to sunrise or sunset
    if (sType == ABS && eType == SUNRISE) {
        if (start.after(today.midday)) {
            start.add(Calendar.DAY_OF_YEAR, -1)
        }
    } else if (sType == SUNRISE && eType == ABS) {
        // no changes
    } else if (sType == ABS && eType == SUNSET) {
        // no changes
    } else if (sType == SUNSET && eType == ABS) {
        if (start.before(today.midday)) {
            start.add(Calendar.DAY_OF_YEAR, +1)
        }
    }
    
    // if it was in the past, make it the future
    if (end.before(today.now)) {
        start.add(Calendar.DAY_OF_YEAR, 1)
        end.add(Calendar.DAY_OF_YEAR, 1)
    }
    return [start,end]
}
            
def calcTime(today, hrs, mins, relTo, offsetMins) {
    def t = Calendar.getInstance()
    def type = ABS
    
    if (hrs != null && mins != null) {
        t.set(Calendar.HOUR_OF_DAY, hrs.toInteger())
        t.set(Calendar.MINUTE, mins.toInteger())
        t.set(Calendar.SECOND, 0)

    } else if (relTo != null) {
        switch (relTo) {
            case "sunrise":
                type=SUNRISE
                break
            case "sunset":
                type=SUNSET
                break
            default:
                throw new Exception("Unrecognised relative-to timespec")
        }

    } else {
        throw new Exception("Timespec not valid")
    }

    if (type != ABS) {
        t = today[relTo].clone()
        if (offsetMins != null && offsetMins != "") {
            t.add(Calendar.MINUTE, offsetMins.toInteger())
        }
    }
    
    return [t, type]
}
