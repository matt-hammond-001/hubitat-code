/* 
=============================================================================
Hubitat Elevation Application
Komfovent C8 auto dehumidifier

-----------------------------------------------------------------------------
*/

import groovy.transform.Field

definition(
	name: "Komfovent C8 MVHR auto dehumidifier",
	namespace: "matthammonddotorg",
	author: "Matt Hammond",
	description: "Uses Komfovent C* MVHR to dehumidify",
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
        section("") {
	        paragraph("This application controls a Komfovent MVHR system (that has a C8 controller) to dehumidify certain rooms that do not have a humidity sensor connected to the Komfovent unit.")
            paragraph("It does this by switching into AUTO mode and setting the humidity set point artifically low, compared to the reading it is getting from the Panel sensor.") 
	        paragraph("It is triggered either by a hard threshold humidity, or by comparing against other humidity sensors in other rooms to see if the humidity is significantly higher. Which humidity senors are used as the baseline reference, and which are in rooms that may need dehumidifying is configured below, along with the thresholds.")
	        paragraph("Use the <b>Auto dehumidify</b> switch device to turn this app on or off. You can also use the <b>Force dehumidify</b> switch device to force dehumidifying behaviour (provided auto-dehumidify is turned on of course)")
        }    
        section("<h3>MVHR to be controlled</h3>") {
         	input "mvhrDev",
                "device.KomfoventC8ModbusTCP",
                title: "MVHR to be controlled",
                multiple: false,
                width: 4,
                required: true,
                submitOnChange: true
            
            paragraph((
                (mvhrDev ? ["""MVHR current mode: ${mvhrDev.currentValue("currentMode")}<br/>"""] : [""])
                ).join("<br/>"),
                width:8
            )

        }
        
        section("<h3>Baseline Humidity sensors</h3>") {
            input "baselineRhSensors",
                "capability.relativeHumidityMeasurement",
                title: "Which humidity sensors as reference baseline?",
                multiple: true,
                required: true,
                submitOnChange: true,
                width: 4
            
            paragraph((
                (baselineRhSensors ? [readingsTable(baselineRhSensors)] : [""])
                ).join("<br/>"),
                width:8
            )
            
            input "baselineRef",
                "enum",
                title: "How to calculate a baseline reference",
                options: [ "min", "lower quartile", "mean", "median", "upper quartile", "max"],
                required: true,
                width: 4,
                submitOnChange: true,
                defaultValue: "median"

            paragraph((
                ((baselineRhSensors && baselineRef) ? ["Baseline Reference: ${calcRef(baselineRhSensors, baselineRef)} %RH"] : [""])
                ).join("<br/>"),
                width:8
            )
        }
        
        section("<h3>Trigger Humidity sensors</h3>") {

            input "extractRhSensors",
                "capability.relativeHumidityMeasurement",
                title: "Which humidity sensors to trigger dehumidification?",
                multiple: true,
                required: true,
                submitOnChange: true,
                width: 4

            paragraph((
                (extractRhSensors ? [readingsTable(extractRhSensors)] : [""])
            	).join("<br/>"),
                width:8
            )
            input "extractRef",
                "enum",
                title: "What reading from triggering sensors to be used to trigger/cancel?",
                options: [ "min", "lower quartile", "mean", "median", "upper quartile", "max"],
                required: true,
                submitOnChange: true,
                width: 4,
                defaultValue: "median"

            paragraph((
                ((extractRhSensors && extractRef) ? ["Triggering Reference: ${calcRef(extractRhSensors, extractRef)} %RH"] : [])
                ).join("<br/>"),
                width:8
            )
        }

        section("<h3>Trigger thresholds</h3>") {
            paragraph("If the triggering referene goes above ANY of these thresholds, then dehumidification begins")

			input "triggerOffset",
                "number",
                title: "How much above to reference to trigger dehumidification?",
            	required: true,
                submitOnChange: true,
                width: 4,
                defaultValue: 20    

            input "absTrigger",
                "number",
                title: "Fixed %RH above which to trigger, irrespective of baseline",
                required: true,
                submitOnChange: true,
                width: 4,
                defaultValue: 72

            paragraph((
                ((baselineRhSensors && baselineRef!=null && triggerOffset!=null) ? ["Reference based Trigger threshold: ${calcRef(baselineRhSensors, baselineRef)+triggerOffset} %RH"] : []) +
                ((absTrigger!=null) ? ["Fixed triggering threshold: ${absTrigger} %RH"] : [])
                ).join("<br/>"),
                width: 4
            )
        }

        section("<h3>Cancel thresholds</h3>") {
            paragraph("Only once the triggering referene goes below ALL of these thresholds, then dehumidification stops")

            input "cancelOffset",
                "number",
                title: "How much above to reference to cancel dehumidification?",
            	required: true,
                submitOnChange: true,
                width: 4,
                defaultValue: 10

            input "absCancel",
                "number",
                title: "Fixed %RH above which to cancel, irrespective of baseline",
                required: true,
                submitOnChange: true,
                width: 4,
                defaultValue: 70

            paragraph((
                ((baselineRhSensors && baselineRef!=null && cancelOffset!=null) ? ["Reference based Cancel threshold: ${calcRef(baselineRhSensors, baselineRef)+cancelOffset} %RH"] : []) + 
                ((absCancel!=null) ? ["Fixed cancel threshold: ${absCancel} %RH"] : [])
                ).join("<br/>"),
                width: 4
            )
            

            
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

def readingsTable(deviceList) {
    [ "<table>",
	    *deviceList.collect { dev ->
            """<tr><td><em>${dev.label ?: dev.name}</em></td>""" +
            """<td><span class="device-current-state-${dev.deviceId}-humidity">""" + 
            "${dev.currentValue('humidity')}" +
            """</span> %RH</td></tr>"""
        },
	    "</table>"
    ].join("\n")
}

def calcRef(deviceList, refType) {
    List readings = deviceList.collect { dev -> dev.currentValue("humidity") }
    switch (refType) {
    case "min":
        return readings.min { it }
    case "max":
        return readings.max { it }
    case "mean":
        return readings.sum { it } / readings.size()
    case "median":
    case "lower quartile":
    case "upper quartile":
    	readings = readings.sort()
        double pos = ["median":0.5,"lower quartile":0.25,"upper quartile":0.75][refType]
    	double middle = (readings.size()-1) * pos
        double rem = middle % 1
        int lo = Math.floor(middle) as int
        int hi = Math.ceil(middle) as int
        return readings[lo] * (1-rem) + readings[hi] * rem
    }
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
    state.relTriggeredDehumidifying=false
    state.absTriggeredDehumidifying=false
	fetchChild("Force dehumidify")
	fetchChild("Auto dehumidify")
    runIn(10, "doControl")
    subscribe(settings.baselineRhSensors, "humidity", "schedule_doControl")
    subscribe(settings.extractRhSensors, "humidity", "schedule_doControl")
    subscribe(settings.mvhrHumidityRef, "humidity", "schedule_doControl")
    
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
Control logic
-----------------------------------------------------------------------------
*/
def schedule_doControl(event=null) {
    runIn 1, "doControl"
}

def doControl() {
    try {
        def autoEnabled = fetchChild("Auto dehumidify").currentValue("switch") == "on"
        def forceDehumidify = fetchChild("Force dehumidify").currentValue("switch") == "on"
        
        // update state machine for relative humidity triggering
        def baseline = calcRef(settings.baselineRhSensors, settings.baselineRef)
        def triggeringRh = calcRef(settings.extractRhSensors, settings.extractRef)
        if (!state.relTriggeredDehumidifying) {
	        // is not dehumidifying
            if (triggeringRh >= (baseline + settings.triggerOffset)) {
                state.relTriggeredDehumidifying = true
            }
        } else {
            // is already dehumidifying
            if (triggeringRh <= (baseline + settings.cancelOffset)) {
                state.relTriggeredDehumidifying = false
            }
        }

        // update state machine for absolute humidity triggering
        if (!state.absTriggeredDehumidifying) {
	        // is not dehumidifying
            if (triggeringRh >= settings.absTrigger) {
                state.absTriggeredDehumidifying = true
            }
        } else {
            // is already dehumidifying
            if (triggeringRh <= settings.absCancel) {
                state.absTriggeredDehumidifying = false
            }
        }
        
        // overall decision as to whether to dehumidify
        boolean shouldDehumidify = forceDehumidify || (autoEnabled && (state.relTriggeredDehumidifying || state.absTriggeredDehumidifying))

        boolean isDehumidifying = settings.mvhrDev.currentValue("auto") == "on" && settings.mvhrDev.currentValue("currentMode") == "air quality" && settings.mvhrDev.currentValue("airQualityHumidityControl") == "enabled"

        
        // ensure MVHR is configured to match what we want
		def humiditySetpoint
        // def ctrlRefRH = settings.mvhrHumidityRef.currentValue("humidity")
        if (shouldDehumidify) {
            humiditySetpoint = 10 // ctrlRefRH - Math.max(10 as int, (triggeringRh-cancelThreshold) as int)
            if (humiditySetpoint < 0) humiditySetpoint=0
            mvhrDev.configureAirQuality(null, null, null, humiditySetpoint, null, null, null, null, null, "enabled", null)
            if (!isDehumidifying) {
                state.lastMode = mvhrDev.currentValue
               mvhrDev.setAuto("on")
            }
        } else {
            if (isDehumidifying) {
	            mvhrDev.setAuto("off")
            }
        }

        logDebug "doControl : forceDehumidify=${forceDehumidify} triggerThreshold=${triggerThreshold} cancelThreshold=${cancelThreshold} triggeringRh=${triggeringRh} isDehumidifying=${isDehumidifying} shouldDehumidify=${shouldDehumidify} humiditySetpoint=${humiditySetpoint}"
    } catch (e) {
        log.error "doControl() : Exception raised: ${e}"
    }
    
//    runIn(10, "doControl")
}

def componentOn(cd) {
    def dev = fetchChild("Force dehumidify")
    if (cd.id == dev.id) {
        dev.parse([[name:"switch", value:"on"]])
        schedule_doControl()
    }
    dev = fetchChild("Auto dehumidify")
    if (cd.id == dev.id) {
        dev.parse([[name:"switch", value:"on"]])
        schedule_doControl()
    }
}

def componentOff(cd) {
    def dev = fetchChild("Force dehumidify")
    if (cd.id == dev.id) {
        dev.parse([[name:"switch", value:"off"]])
        schedule_doControl()
    }
    dev = fetchChild("Auto dehumidify")
    if (cd.id == dev.id) {
        dev.parse([[name:"switch", value:"off"]])
        schedule_doControl()
    }
}

/*
-----------------------------------------------------------------------------
Child devices
-----------------------------------------------------------------------------
*/

def fetchChild(String childName, boolean create=true) {
	String thisId = app.id
    def cd = getChildDevice("${thisId}-${childName}")
    if (!cd && create) {
		String deviceType
        List<Map> defaultValues = []
        switch (childName) {
            case "Auto dehumidify":
            	namespace = "hubitat"
            	deviceType = "Generic Component Switch"
	            defaultValues += [[name:"switch", value:"on"]]
            	break
            case "Force dehumidify":
            	namespace = "hubitat"
            	deviceType = "Generic Component Switch"
	            defaultValues += [[name:"switch", value:"off"]]
            	break
            
            default:
                logError("fetchChild() unrecognised name: ${childName}")
        }
        if (deviceType) {
            cd = addChildDevice(namespace, deviceType, "${thisId}-${childName}", [name: "${childName}", isComponent: true])
            cd.parse(defaultValues)
        }
    }
    return cd 
}

def componentRefresh(cd) {
    return null
}
