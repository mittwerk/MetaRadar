package f.cking.software.domain.interactor

import f.cking.software.extract16BitUuid

object GetServiceNameFromBluetoothService {

    fun execute(serviceId: String): String? {
        return KNOWN_SERVICE_ID_TO_NAME[extract16BitUuid(serviceId)?.uppercase()]
    }

    private val KNOWN_SERVICE_ID_TO_NAME = mapOf(
        "1800" to "Generic Access",
        "1801" to "Generic Attribute",
        "1802" to "Immediate Alert",
        "1803" to "Link Loss",
        "1804" to "Tx Power",
        "1805" to "Current Time Service",
        "1806" to "Reference Time Update Service",
        "1807" to "Next DST Change Service",
        "1808" to "Glucose Service",
        "1809" to "Health Thermometer Service",
        "180A" to "Device Information Service",
        "180D" to "Heart Rate Service",
        "180E" to "Phone Alert Status Service",
        "180F" to "Battery Service",
        "1810" to "Blood Pressure Service",
        "1811" to "Alert Notification Service",
        "1812" to "Human Interface Device Service",
        "1813" to "Scan Parameters Service",
        "1814" to "Running Speed and Cadence Service",
        "1815" to "Automation IO Service",
        "1816" to "Cycling Speed and Cadence Service",
        "1818" to "Cycling Power Service",
        "1819" to "Location and Navigation Service",
        "181A" to "Environmental Sensing Service",
        "181B" to "Body Composition Service",
        "181C" to "User Data Service",
        "181D" to "Weight Scale Service",
        "181E" to "Bond Management Service",
        "181F" to "Continuous Glucose Monitoring Service",
        "1820" to "Internet Protocol Support Service",
        "1821" to "Indoor Positioning Service",
        "1822" to "Pulse Oximeter Service",
        "1823" to "HTTP Proxy Service",
        "1824" to "Transport Discovery Service",
        "1825" to "Object Transfer Service",
        "1826" to "Fitness Machine Service",
        "1827" to "Mesh Provisioning Service",
        "1828" to "Mesh Proxy Service",
        "1829" to "Reconnection Configuration Service",
        "183A" to "Insulin Delivery Service",
        "183B" to "Binary Sensor Service",
        "183C" to "Emergency Configuration Service",
        "183D" to "Authorization Control Service",
        "183E" to "Physical Activity Monitor Service",
        "183F" to "Elapsed Time Service",
        "1840" to "Generic Health Sensor Service",
        "1843" to "Audio Input Control Service",
        "1844" to "Volume Control Service",
        "1845" to "Volume Offset Control Service",
        "1846" to "Coordinated Set Identification Service",
        "1847" to "Device Time Service",
        "1848" to "Media Control Service",
        "1849" to "Generic Media Control Service",
        "184A" to "Constant Tone Extension Service",
        "184B" to "Telephone Bearer Service",
        "184C" to "Generic Telephone Bearer Service",
        "184D" to "Microphone Control Service",
        "184E" to "Audio Stream Control Service",
        "184F" to "Broadcast Audio Scan Service",
        "1850" to "Published Audio Capabilities Service",
        "1851" to "Basic Audio Announcement Service",
        "1852" to "Broadcast Audio Announcement Service",
        "1853" to "Common Audio Service",
        "1854" to "Hearing Access Service",
        "1855" to "Telephony and Media Audio Service",
        "1856" to "Public Broadcast Announcement Service",
        "1857" to "Electronic Shelf Label Service",
        "1858" to "Gaming Audio Service",
        "1859" to "Mesh Proxy Solicitation Service",
        "185A" to "Industrial Measurement Device Service",
        "185B" to "Ranging Service",
    )
}