package com.example

import com.example.data.models.HourlyItem
import com.example.engine.BioclimaticClothingEngine
import com.example.engine.ThermalLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BioclimaticClothingEngineTest {

    private fun createHourlyList(minTemp: Int, maxTemp: Int): List<HourlyItem> {
        val hours = (0..23).toList()
        return hours.map { h ->
            val fraction = if (h in 6..15) (h - 6) / 9.0 else if (h in 16..23) (23 - h) / 8.0 else 0.0
            val temp = (minTemp + fraction * (maxTemp - minTemp)).toInt()
            HourlyItem(
                rawTime = "2026-06-15T%02d:00:00".format(h),
                label = "%02d:00".format(h),
                temp = temp,
                apparentTemp = temp.toDouble(),
                humidity = 45,
                windSpeed = 10.0,
                windGusts = 15.0,
                rainProb = 0,
                weatherCode = 0,
                isDay = true,
                uvIndex = 6.0,
                precipitation = 0.0,
                cloudCover = 10
            )
        }
    }

    @Test
    fun testWarmDayNoLayersNoCoat() {
        // Warm day: 20°C to 30°C (minTemp >= 18°C)
        val hourly = createHourlyList(minTemp = 20, maxTemp = 30)
        val rec = BioclimaticClothingEngine.calculate(
            currentTemp = 25.0,
            currentHumidity = 40,
            currentWindSpeed = 8.0,
            currentApparentTemp = 25.0,
            hourlyItems = hourly,
            sunsetTime = "21:00"
        )

        // Layer strategy must be inactive when min temp >= 14°C and especially >= 18°C
        assertFalse("Layer strategy should be inactive when min temp >= 18°C", rec.layerStrategy.isActive)

        // Forbidden words: abrigo, jersey, chaqueta
        val combinedText = (rec.headline + " " + rec.milestones.joinToString(" ") { it.keyGarment } + " " +
                (rec.layerStrategy.outerLayer ?: "") + " " + (rec.layerStrategy.midLayer ?: "")).lowercase()
        assertFalse("Should not mention abrigo on warm day", combinedText.contains("abrigo"))
        assertFalse("Should not mention jersey on warm day", combinedText.contains("jersey"))
        assertFalse("Should not mention chaqueta on warm day", combinedText.contains("chaqueta"))
    }

    @Test
    fun testColdDayActivatesLayerStrategy() {
        // Cold morning: 8°C to 16°C (minTemp < 14°C)
        val hourly = createHourlyList(minTemp = 8, maxTemp = 16)
        val rec = BioclimaticClothingEngine.calculate(
            currentTemp = 10.0,
            currentHumidity = 70,
            currentWindSpeed = 12.0,
            currentApparentTemp = 9.0,
            hourlyItems = hourly,
            sunsetTime = "20:30"
        )

        assertTrue("Layer strategy should be active when min temp < 14°C", rec.layerStrategy.isActive)
        assertTrue("Total layers should be > 1 for cold day", rec.layerStrategy.totalLayers > 1)
    }

    @Test
    fun testSunsetRuleWarmVsCold() {
        // 1. Warm night at sunset: temp >= 22°C
        val warmHourly = createHourlyList(minTemp = 22, maxTemp = 32)
        val warmRec = BioclimaticClothingEngine.calculate(
            currentTemp = 28.0,
            currentHumidity = 40,
            currentWindSpeed = 5.0,
            currentApparentTemp = 28.0,
            hourlyItems = warmHourly,
            sunsetTime = "20:40"
        )
        val sunsetMilestone = warmRec.milestones.find { it.id == "TARDE" || it.id == "REGRESO" }
        assertTrue(sunsetMilestone != null)
        assertFalse(sunsetMilestone!!.keyGarment.lowercase().contains("abrigo"))

        // 2. Cold night at sunset: temp < 16°C
        val coldHourly = createHourlyList(minTemp = 10, maxTemp = 20)
        val coldRec = BioclimaticClothingEngine.calculate(
            currentTemp = 14.0,
            currentHumidity = 60,
            currentWindSpeed = 10.0,
            currentApparentTemp = 13.0,
            hourlyItems = coldHourly,
            sunsetTime = "20:40"
        )
        val coldSunset = coldRec.milestones.find { it.id == "REGRESO" }
        assertTrue(coldSunset != null)
    }

    @Test
    fun testThermalLevelThresholds() {
        assertEquals(ThermalLevel.CALOR_INTENSO, ThermalLevel.fromTemp(29.0))
        assertEquals(ThermalLevel.CALIDO, ThermalLevel.fromTemp(25.0))
        assertEquals(ThermalLevel.TEMPLADO_SUAVE, ThermalLevel.fromTemp(22.0))
        assertEquals(ThermalLevel.FRESCO_ENTRETIEMPO, ThermalLevel.fromTemp(17.0))
        assertEquals(ThermalLevel.FRIO_MODERADO, ThermalLevel.fromTemp(10.0))
        assertEquals(ThermalLevel.FRIO_INTENSO, ThermalLevel.fromTemp(5.0))
        assertEquals(ThermalLevel.GELIDO, ThermalLevel.fromTemp(0.0))
    }
}
