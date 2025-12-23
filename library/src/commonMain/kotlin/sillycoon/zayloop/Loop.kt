package sillycoon.zayloop

import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Position
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

fun interface RandomDirection {
    fun calculate(): Double
}

enum class Direction(
    val value: Int,
    val random: RandomDirection,
) {
    RANDOM(0, { Random.nextDouble() * PI * 2 }), NORTH(1, { (Random.nextDouble() * PI) / 4 + (3 * PI) / 8 }), NORTHEAST(
        3, { (Random.nextDouble() * PI) / 4 + (1 * PI) / 8 }),
    EAST(4, { (Random.nextDouble() * PI) / 4 - PI / 8 }), SOUTHEAST(
        5, { (Random.nextDouble() * PI) / 4 + (13 * PI) / 8 }),
    SOUTH(6, { (Random.nextDouble() * PI) / 4 + (11 * PI) / 8 }), SOUTHWEST(
        7, { (Random.nextDouble() * PI) / 4 + (9 * PI) / 8 }),
    WEST(8, { (Random.nextDouble() * PI) / 4 + (7 * PI) / 8 }), NORTHWEST(
        9, { (Random.nextDouble() * PI) / 4 + (5 * PI) / 8 }),
}

enum class Rotation(
    val sign: Int,
) {
    CLOCKWISE(1), COUNTERCLOCKWISE(-1),
}

data class RouteDescriptor(
    val start: Location,
    val length: Double,
    val direction: Direction,
    val rotation: Rotation,
)

data class Measurements(
    val length: Double,
) {
    private val ratio = Random.nextDouble() * DELTA_RATIO + RECT_MIN_RATIO
    val width = length / (2 * ratio + 2)
    val height = width * ratio
    val diagonal = sqrt(width * width + height * height)
    val theta = acos(height / diagonal)
}

private const val RECT_MAX_RATIO = 5; // max height:width ratio
private const val RECT_MIN_RATIO = 1 / RECT_MAX_RATIO; // min height:width ratio
private const val DELTA_RATIO = RECT_MAX_RATIO - RECT_MIN_RATIO

private const val METERS_PER_DEGREE_LAT = 110540
private const val METERS_PER_DEGREE_LNG_EQUATOR = 111320

private fun calculatePointOnRoute(
    location: Location,
    direction: Double,
    radius: Double,
): Location {
    val dx = radius * cos(direction)
    val dy = radius * sin(direction)
    val deltaLat = dy / METERS_PER_DEGREE_LAT
    val deltaLng = dx / (METERS_PER_DEGREE_LNG_EQUATOR * cos((location.lat * PI) / 180))
    return Location(
        lat = location.lat + deltaLat,
        lng = location.lng + deltaLng,
    )
}

private typealias LoopFn = (descriptor: RouteDescriptor) -> Feature<LineString, Nothing?>

private val rectangularLoop: LoopFn = { it ->
    val measurements = Measurements(it.length)
    val direction = it.direction.random.calculate()

    val points = listOf(
        it.start, calculatePointOnRoute(it.start, direction, measurements.height), calculatePointOnRoute(
            it.start,
            it.rotation.sign * measurements.theta + direction,
            measurements.diagonal,
        ), calculatePointOnRoute(
            it.start,
            (it.rotation.sign * PI) / 2 + direction,
            measurements.width,
        ), it.start
    )

    Feature(
        LineString(
            points.map { Position(it.lng, it.lat) },
        ),
        null,
    )
}

enum class LoopType {
    Random, Eight, Circle, Square,
}

class Loop(private val type: LoopType) {
    fun generate(
        descriptor: RouteDescriptor,
    ) = when (type) {
        else -> rectangularLoop(descriptor)
    }
}

