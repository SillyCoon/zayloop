package sillycoon.zayloop

import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Position
import kotlin.collections.mutableListOf

enum class FitnessLevel(
    val value: Int,
) {
    Novice(0), Moderate(1), Amateur(2), Pro(3),
}

enum class GreenFactor(
    val value: Int,
) {
    Normal(0), PreferGreen(1),
}

enum class QuietFactor(
    val value: Int,
) {
    Normal(0), PreferQuiet(1),
}

enum class TravelMode(
    val value: String,
) {
    Car("driving-car"), CyclingRegular("cycling-regular"), CyclingRoad("cycling-road"), CyclingMountain("cycling-mountain"), CyclingElectric(
        "cycling-electric",
    ),
    FootWalking("foot-walking"), FootHiking("foot-hiking"),
}

data class RouteOptions(
    val highways: Boolean,
    val ferries: Boolean,
    val waypoints: List<Location>, // includes start endpoint
    val mode: TravelMode,
    val fitnessLevel: FitnessLevel,
    val greenFactor: GreenFactor,
    val quietFactor: QuietFactor,
    val distance: Double,
)

abstract class Directions {
    private fun removeDuplicates(locations: List<Position>): List<Position> =
        locations.fold(mutableListOf()) { acc, point ->
            if (acc.lastOrNull() != point) {
                acc.add(point)
            }
            acc
        }

    protected abstract suspend fun fetch(options: RouteOptions): Feature<LineString, Any?>

    suspend fun get(options: RouteOptions): Feature<LineString, Any?> {
        val result = fetch(options)
        return result.copy(
            geometry = result.geometry.copy(
                coordinates = removeDuplicates(result.geometry.coordinates),
            ),
        )
    }
}
