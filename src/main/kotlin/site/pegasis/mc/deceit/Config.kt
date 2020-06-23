package site.pegasis.mc.deceit

import org.bukkit.DyeColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Banner
import org.bukkit.block.Block
import org.bukkit.block.banner.Pattern
import org.bukkit.block.banner.PatternType
import org.bukkit.entity.Entity

data class BannerState(val baseColor: DyeColor, val patterns: List<Pattern>) {
    fun isMatch(banner: Banner): Boolean {
        if (banner.baseColor != baseColor) return false
        return listsEqual(banner.patterns, patterns)
    }

    companion object {
        fun Banner.applyState(bannerState: BannerState) {
            baseColor = bannerState.baseColor
            patterns = bannerState.patterns
        }
    }
}

data class BlockPos(val x: Int, val y: Int, val z: Int) {
    fun toEntityPos() = EntityPos(x.toDouble(), y.toDouble(), z.toDouble())
}

fun World.getBlockAt(pos: BlockPos) = getBlockAt(pos.x, pos.y, pos.z)

val Block.blockPos: BlockPos
    get() = BlockPos(x, y, z)

data class EntityPos(val x: Double, val y: Double, val z: Double) {
    fun toLocation(): Location {
        return Location(Game.world, x, y, z)
    }
}

val Entity.entityPos: EntityPos
    get() = EntityPos(location.x, location.y, location.z)


data class ThreePair<out A, out B, out C>(
    val first: A,
    val second: B,
    val third: C
)

data class Level(
    val maxY: Int, // included
    val minY: Int, // included
    val lightTime: Int,
    val darkTime: Int,
    val rageTime: Int,
    val runTime: Int,
    val bloodBagPosesCount: Int,
    val bloodBagPoses: List<EntityPos>,
    val requiredFuses: Int,
    val fuseCount: Int,
    val fusePositions: List<BlockPos>,
    val fuseSocketCount: Int,
    val fuseSocketPositions: List<BlockPos>,
    val doorPositions: List<BlockPos>,
    val objAs: List<Pair<BlockPos, BlockPos>>, // objective wait (center floor block), lever
    val objBs: List<ThreePair<BlockPos, BlockPos, BlockPos>>, // objective tall (bottom block), lever, item frame
    val objCs: List<ThreePair<BlockPos, BlockPos, BlockPos>>, // objective two person (half block), lever, pressure plate
    val itemSpawnPlaces: List<EntityPos>,
    val itemSpawnCount: Int,
    val spawnPoses: List<EntityPos>
)

object Config {
    val debuggerName = "Pegasis"
    val removeEntityWaitSecond = 0.2
    val lobbyLocation = Location(null, 731.0, 66.0, 28351.0)
    val originalSkinOverride = mapOf("Pegasis" to "yEco")
    val infectedSkin = "GGsf2" //https://namemc.com/skin/f9b5ba2c8cd83d55
    val worldName = "world"
    val transformDuration = 5 //Seconds
    val bloodPackRestoreTime = 2 //Seconds
    val transformMaterial = Material.MUSIC_DISC_11
    val fuseMaterial = Material.YELLOW_STAINED_GLASS_PANE
    val fuseBaseMaterial = Material.IRON_BARS
    val trackerMaterial = Material.STONE_BUTTON
    val cameraMaterial = Material.ITEM_FRAME
    val inspectionKitMaterial = Material.DAYLIGHT_DETECTOR
    val healthPackMaterial = Material.GOLDEN_APPLE
    val antidoteMaterial = Material.EMERALD
    val lethalInjectionMaterial = Material.PUFFERFISH
    val torchMaterial = Material.LEVER
    val torchDistance = 10.0
    val torchBrightness = 13
    val torchAngle = 40.0
    val cameraDistance = 7.0
    val cameraAngle = 50.0
    val cameraBrightness = 15
    val cameraDuration = 0.2
    val cameraCoolDown = 1.0
    val highLightDistance = 16
    val doorMaterial = Material.ANVIL
    val trackerDuration = 5.0
    val inspectionKitDelay = 3.0
    val objADuration = 3.0
    val objAUnactivatedBlock = Material.BIRCH_PLANKS
    val objANormalBlock = Material.WHITE_STAINED_GLASS
    val objAProgressBlock = Material.LIME_STAINED_GLASS
    val objBL0Interval = 0.8
    val objBL1Interval = 0.6
    val objBL2Interval = 0.4
    val objBButtonMaterial = Material.BIRCH_BUTTON
    val objCPressurePlateMaterial = Material.HEAVY_WEIGHTED_PRESSURE_PLATE
    val objCCoverMaterial = Material.DARK_OAK_PLANKS
    val lightSources = listOf(
        BlockPos(x = 688, y = 92, z = 28339),
        BlockPos(x = 688, y = 92, z = 28347),
        BlockPos(x = 689, y = 75, z = 28339),
        BlockPos(x = 690, y = 70, z = 28351),
        BlockPos(x = 690, y = 79, z = 28351),
        BlockPos(x = 691, y = 79, z = 28350),
        BlockPos(x = 691, y = 81, z = 28336),
        BlockPos(x = 691, y = 86, z = 28342),
        BlockPos(x = 691, y = 86, z = 28345),
        BlockPos(x = 692, y = 79, z = 28351),
        BlockPos(x = 696, y = 86, z = 28336),
        BlockPos(x = 696, y = 86, z = 28350),
        BlockPos(x = 697, y = 55, z = 28345),
        BlockPos(x = 697, y = 70, z = 28343),
        BlockPos(x = 697, y = 70, z = 28351),
        BlockPos(x = 697, y = 78, z = 28343),
        BlockPos(x = 697, y = 78, z = 28351),
        BlockPos(x = 701, y = 55, z = 28345),
        BlockPos(x = 701, y = 70, z = 28343),
        BlockPos(x = 701, y = 70, z = 28351),
        BlockPos(x = 701, y = 78, z = 28343),
        BlockPos(x = 701, y = 78, z = 28351),
        BlockPos(x = 702, y = 91, z = 28343),
        BlockPos(x = 703, y = 70, z = 28341),
        BlockPos(x = 703, y = 78, z = 28337),
        BlockPos(x = 703, y = 78, z = 28341),
        BlockPos(x = 703, y = 91, z = 28342),
        BlockPos(x = 703, y = 91, z = 28344),
        BlockPos(x = 680, y = 77, z = 28335),
        BlockPos(x = 681, y = 72, z = 28329),
        BlockPos(x = 681, y = 72, z = 28333),
        BlockPos(x = 682, y = 81, z = 28335),
        BlockPos(x = 683, y = 81, z = 28334),
        BlockPos(x = 684, y = 81, z = 28335),
        BlockPos(x = 686, y = 75, z = 28331),
        BlockPos(x = 687, y = 77, z = 28328),
        BlockPos(x = 673, y = 75, z = 28347),
        BlockPos(x = 674, y = 81, z = 28351),
        BlockPos(x = 674, y = 86, z = 28344),
        BlockPos(x = 675, y = 71, z = 28344),
        BlockPos(x = 675, y = 81, z = 28350),
        BlockPos(x = 676, y = 81, z = 28351),
        BlockPos(x = 678, y = 70, z = 28346),
        BlockPos(x = 681, y = 70, z = 28351),
        BlockPos(x = 681, y = 78, z = 28351),
        BlockPos(x = 683, y = 63, z = 28344),
        BlockPos(x = 683, y = 81, z = 28336),
        BlockPos(x = 683, y = 86, z = 28342),
        BlockPos(x = 683, y = 86, z = 28345),
        BlockPos(x = 684, y = 75, z = 28337),
        BlockPos(x = 685, y = 70, z = 28351),
        BlockPos(x = 685, y = 78, z = 28351),
        BlockPos(x = 685, y = 86, z = 28340),
        BlockPos(x = 686, y = 75, z = 28339),
        BlockPos(x = 686, y = 92, z = 28339),
        BlockPos(x = 686, y = 92, z = 28347),
        BlockPos(x = 687, y = 77, z = 28342),
        BlockPos(x = 687, y = 92, z = 28338),
        BlockPos(x = 687, y = 92, z = 28340),
        BlockPos(x = 687, y = 92, z = 28346),
        BlockPos(x = 687, y = 92, z = 28348),
        BlockPos(x = 673, y = 75, z = 28355),
        BlockPos(x = 675, y = 71, z = 28358),
        BlockPos(x = 675, y = 81, z = 28352),
        BlockPos(x = 676, y = 75, z = 28360),
        BlockPos(x = 678, y = 82, z = 28361),
        BlockPos(x = 678, y = 82, z = 28363),
        BlockPos(x = 678, y = 82, z = 28365),
        BlockPos(x = 678, y = 86, z = 28366),
        BlockPos(x = 678, y = 89, z = 28366),
        BlockPos(x = 679, y = 89, z = 28353),
        BlockPos(x = 679, y = 89, z = 28357),
        BlockPos(x = 680, y = 91, z = 28363),
        BlockPos(x = 681, y = 70, z = 28359),
        BlockPos(x = 681, y = 78, z = 28359),
        BlockPos(x = 685, y = 70, z = 28359),
        BlockPos(x = 685, y = 78, z = 28359),
        BlockPos(x = 685, y = 86, z = 28363),
        BlockPos(x = 686, y = 86, z = 28363),
        BlockPos(x = 686, y = 92, z = 28363),
        BlockPos(x = 687, y = 70, z = 28361),
        BlockPos(x = 687, y = 70, z = 28365),
        BlockPos(x = 687, y = 78, z = 28361),
        BlockPos(x = 687, y = 78, z = 28365),
        BlockPos(x = 687, y = 86, z = 28363),
        BlockPos(x = 687, y = 89, z = 28353),
        BlockPos(x = 687, y = 89, z = 28357),
        BlockPos(x = 689, y = 75, z = 28331),
        BlockPos(x = 690, y = 81, z = 28335),
        BlockPos(x = 691, y = 81, z = 28334),
        BlockPos(x = 692, y = 81, z = 28335),
        BlockPos(x = 694, y = 71, z = 28331),
        BlockPos(x = 697, y = 70, z = 28335),
        BlockPos(x = 697, y = 78, z = 28335),
        BlockPos(x = 701, y = 78, z = 28335),
        BlockPos(x = 703, y = 70, z = 28329),
        BlockPos(x = 703, y = 78, z = 28329),
        BlockPos(x = 703, y = 78, z = 28333),
        BlockPos(x = 688, y = 86, z = 28363),
        BlockPos(x = 688, y = 92, z = 28363),
        BlockPos(x = 691, y = 79, z = 28352),
        BlockPos(x = 695, y = 70, z = 28361),
        BlockPos(x = 695, y = 70, z = 28365),
        BlockPos(x = 695, y = 78, z = 28361),
        BlockPos(x = 695, y = 78, z = 28365),
        BlockPos(x = 695, y = 89, z = 28353),
        BlockPos(x = 695, y = 89, z = 28357),
        BlockPos(x = 697, y = 55, z = 28357),
        BlockPos(x = 697, y = 70, z = 28359),
        BlockPos(x = 697, y = 78, z = 28359),
        BlockPos(x = 698, y = 88, z = 28363),
        BlockPos(x = 699, y = 92, z = 28361),
        BlockPos(x = 699, y = 92, z = 28365),
        BlockPos(x = 701, y = 55, z = 28357),
        BlockPos(x = 701, y = 70, z = 28359),
        BlockPos(x = 701, y = 78, z = 28359),
        BlockPos(x = 703, y = 70, z = 28361),
        BlockPos(x = 703, y = 70, z = 28365),
        BlockPos(x = 703, y = 78, z = 28361),
        BlockPos(x = 703, y = 78, z = 28365),
        BlockPos(x = 703, y = 89, z = 28353),
        BlockPos(x = 703, y = 89, z = 28357),
        BlockPos(x = 705, y = 78, z = 28335),
        BlockPos(x = 709, y = 70, z = 28335),
        BlockPos(x = 711, y = 70, z = 28329),
        BlockPos(x = 711, y = 70, z = 28333),
        BlockPos(x = 711, y = 78, z = 28329),
        BlockPos(x = 713, y = 70, z = 28335),
        BlockPos(x = 717, y = 70, z = 28335),
        BlockPos(x = 717, y = 78, z = 28335),
        BlockPos(x = 718, y = 55, z = 28333),
        BlockPos(x = 718, y = 56, z = 28333),
        BlockPos(x = 718, y = 57, z = 28333),
        BlockPos(x = 719, y = 55, z = 28333),
        BlockPos(x = 719, y = 56, z = 28333),
        BlockPos(x = 719, y = 57, z = 28333),
        BlockPos(x = 719, y = 70, z = 28329),
        BlockPos(x = 719, y = 70, z = 28333),
        BlockPos(x = 719, y = 78, z = 28329),
        BlockPos(x = 719, y = 78, z = 28333),
        BlockPos(x = 704, y = 79, z = 28351),
        BlockPos(x = 704, y = 91, z = 28343),
        BlockPos(x = 709, y = 67, z = 28351),
        BlockPos(x = 710, y = 71, z = 28351),
        BlockPos(x = 710, y = 75, z = 28348),
        BlockPos(x = 710, y = 81, z = 28351),
        BlockPos(x = 710, y = 86, z = 28336),
        BlockPos(x = 710, y = 86, z = 28350),
        BlockPos(x = 711, y = 67, z = 28349),
        BlockPos(x = 711, y = 70, z = 28337),
        BlockPos(x = 711, y = 70, z = 28341),
        BlockPos(x = 711, y = 71, z = 28350),
        BlockPos(x = 711, y = 78, z = 28341),
        BlockPos(x = 711, y = 79, z = 28344),
        BlockPos(x = 711, y = 81, z = 28350),
        BlockPos(x = 712, y = 71, z = 28351),
        BlockPos(x = 712, y = 81, z = 28351),
        BlockPos(x = 713, y = 67, z = 28351),
        BlockPos(x = 718, y = 79, z = 28351),
        BlockPos(x = 719, y = 61, z = 28351),
        BlockPos(x = 705, y = 89, z = 28359),
        BlockPos(x = 705, y = 89, z = 28367),
        BlockPos(x = 708, y = 75, z = 28352),
        BlockPos(x = 709, y = 89, z = 28359),
        BlockPos(x = 709, y = 89, z = 28367),
        BlockPos(x = 711, y = 67, z = 28353),
        BlockPos(x = 711, y = 70, z = 28361),
        BlockPos(x = 711, y = 70, z = 28365),
        BlockPos(x = 711, y = 71, z = 28352),
        BlockPos(x = 711, y = 78, z = 28361),
        BlockPos(x = 711, y = 78, z = 28365),
        BlockPos(x = 711, y = 79, z = 28358),
        BlockPos(x = 711, y = 81, z = 28352),
        BlockPos(x = 712, y = 86, z = 28352),
        BlockPos(x = 712, y = 86, z = 28358),
        BlockPos(x = 712, y = 86, z = 28360),
        BlockPos(x = 713, y = 70, z = 28367),
        BlockPos(x = 713, y = 78, z = 28367),
        BlockPos(x = 714, y = 92, z = 28367),
        BlockPos(x = 715, y = 92, z = 28366),
        BlockPos(x = 716, y = 92, z = 28367),
        BlockPos(x = 717, y = 70, z = 28367),
        BlockPos(x = 717, y = 78, z = 28367),
        BlockPos(x = 717, y = 86, z = 28363),
        BlockPos(x = 662, y = 64, z = 28348),
        BlockPos(x = 666, y = 81, z = 28351),
        BlockPos(x = 667, y = 71, z = 28344),
        BlockPos(x = 667, y = 81, z = 28350),
        BlockPos(x = 668, y = 75, z = 28349),
        BlockPos(x = 668, y = 81, z = 28351),
        BlockPos(x = 669, y = 90, z = 28347),
        BlockPos(x = 670, y = 75, z = 28347),
        BlockPos(x = 664, y = 75, z = 28362),
        BlockPos(x = 664, y = 75, z = 28363),
        BlockPos(x = 664, y = 86, z = 28355),
        BlockPos(x = 664, y = 86, z = 28356),
        BlockPos(x = 665, y = 67, z = 28360),
        BlockPos(x = 665, y = 67, z = 28366),
        BlockPos(x = 667, y = 71, z = 28358),
        BlockPos(x = 667, y = 81, z = 28352),
        BlockPos(x = 667, y = 91, z = 28366),
        BlockPos(x = 670, y = 70, z = 28355),
        BlockPos(x = 670, y = 75, z = 28355),
        BlockPos(x = 679, y = 74, z = 28373),
        BlockPos(x = 680, y = 71, z = 28375),
        BlockPos(x = 681, y = 92, z = 28368),
        BlockPos(x = 681, y = 92, z = 28374),
        BlockPos(x = 682, y = 74, z = 28379),
        BlockPos(x = 687, y = 71, z = 28368),
        BlockPos(x = 689, y = 74, z = 28377),
        BlockPos(x = 689, y = 85, z = 28369),
        BlockPos(x = 689, y = 85, z = 28373),
        BlockPos(x = 694, y = 71, z = 28375),
        BlockPos(x = 695, y = 89, z = 28369),
        BlockPos(x = 695, y = 89, z = 28373),
        BlockPos(x = 696, y = 78, z = 28371),
        BlockPos(x = 696, y = 79, z = 28375),
        BlockPos(x = 699, y = 71, z = 28368),
        BlockPos(x = 700, y = 75, z = 28378),
        BlockPos(x = 702, y = 81, z = 28375),
        BlockPos(x = 702, y = 92, z = 28379),
        BlockPos(x = 703, y = 79, z = 28368),
        BlockPos(x = 703, y = 79, z = 28382),
        BlockPos(x = 703, y = 81, z = 28374),
        BlockPos(x = 703, y = 81, z = 28376),
        BlockPos(x = 703, y = 86, z = 28379),
        BlockPos(x = 703, y = 89, z = 28369),
        BlockPos(x = 703, y = 89, z = 28373),
        BlockPos(x = 703, y = 92, z = 28378),
        BlockPos(x = 703, y = 92, z = 28380),
        BlockPos(x = 704, y = 75, z = 28372),
        BlockPos(x = 704, y = 81, z = 28375),
        BlockPos(x = 704, y = 86, z = 28379),
        BlockPos(x = 705, y = 86, z = 28379),
        BlockPos(x = 706, y = 75, z = 28376),
        BlockPos(x = 710, y = 79, z = 28375),
        BlockPos(x = 712, y = 77, z = 28376),
        BlockPos(x = 712, y = 77, z = 28378),
        BlockPos(x = 712, y = 77, z = 28380),
        BlockPos(x = 712, y = 77, z = 28382),
        BlockPos(x = 712, y = 86, z = 28374),
        BlockPos(x = 715, y = 69, z = 28382),
        BlockPos(x = 715, y = 92, z = 28368),
        BlockPos(x = 717, y = 86, z = 28371),
        BlockPos(x = 718, y = 81, z = 28379),
        BlockPos(x = 719, y = 70, z = 28369),
        BlockPos(x = 719, y = 70, z = 28373),
        BlockPos(x = 719, y = 78, z = 28369),
        BlockPos(x = 719, y = 78, z = 28373),
        BlockPos(x = 719, y = 81, z = 28378),
        BlockPos(x = 719, y = 81, z = 28380),
        BlockPos(x = 727, y = 70, z = 28329),
        BlockPos(x = 727, y = 70, z = 28333),
        BlockPos(x = 727, y = 78, z = 28329),
        BlockPos(x = 727, y = 78, z = 28333),
        BlockPos(x = 729, y = 70, z = 28335),
        BlockPos(x = 729, y = 78, z = 28335),
        BlockPos(x = 733, y = 70, z = 28335),
        BlockPos(x = 733, y = 78, z = 28335),
        BlockPos(x = 720, y = 67, z = 28344),
        BlockPos(x = 720, y = 75, z = 28337),
        BlockPos(x = 721, y = 67, z = 28341),
        BlockPos(x = 722, y = 58, z = 28348),
        BlockPos(x = 722, y = 58, z = 28351),
        BlockPos(x = 722, y = 67, z = 28341),
        BlockPos(x = 722, y = 80, z = 28339),
        BlockPos(x = 723, y = 67, z = 28341),
        BlockPos(x = 723, y = 70, z = 28339),
        BlockPos(x = 723, y = 80, z = 28338),
        BlockPos(x = 723, y = 80, z = 28340),
        BlockPos(x = 724, y = 67, z = 28341),
        BlockPos(x = 724, y = 76, z = 28341),
        BlockPos(x = 724, y = 80, z = 28339),
        BlockPos(x = 725, y = 67, z = 28341),
        BlockPos(x = 726, y = 67, z = 28351),
        BlockPos(x = 726, y = 76, z = 28338),
        BlockPos(x = 729, y = 70, z = 28343),
        BlockPos(x = 729, y = 78, z = 28343),
        BlockPos(x = 733, y = 70, z = 28343),
        BlockPos(x = 733, y = 78, z = 28343),
        BlockPos(x = 720, y = 67, z = 28358),
        BlockPos(x = 720, y = 86, z = 28363),
        BlockPos(x = 722, y = 58, z = 28354),
        BlockPos(x = 722, y = 80, z = 28363),
        BlockPos(x = 722, y = 92, z = 28367),
        BlockPos(x = 723, y = 67, z = 28360),
        BlockPos(x = 723, y = 71, z = 28360),
        BlockPos(x = 723, y = 80, z = 28362),
        BlockPos(x = 723, y = 80, z = 28364),
        BlockPos(x = 723, y = 92, z = 28366),
        BlockPos(x = 724, y = 80, z = 28363),
        BlockPos(x = 724, y = 92, z = 28367),
        BlockPos(x = 726, y = 75, z = 28365),
        BlockPos(x = 726, y = 86, z = 28352),
        BlockPos(x = 726, y = 86, z = 28358),
        BlockPos(x = 726, y = 86, z = 28360),
        BlockPos(x = 729, y = 70, z = 28359),
        BlockPos(x = 729, y = 70, z = 28367),
        BlockPos(x = 729, y = 78, z = 28359),
        BlockPos(x = 729, y = 78, z = 28367),
        BlockPos(x = 733, y = 70, z = 28359),
        BlockPos(x = 733, y = 70, z = 28367),
        BlockPos(x = 733, y = 78, z = 28359),
        BlockPos(x = 733, y = 78, z = 28367),
        BlockPos(x = 720, y = 81, z = 28379),
        BlockPos(x = 720, y = 86, z = 28371),
        BlockPos(x = 722, y = 86, z = 28369),
        BlockPos(x = 723, y = 92, z = 28368),
        BlockPos(x = 726, y = 75, z = 28382),
        BlockPos(x = 726, y = 86, z = 28374),
        BlockPos(x = 727, y = 70, z = 28369),
        BlockPos(x = 727, y = 70, z = 28373),
        BlockPos(x = 727, y = 78, z = 28369),
        BlockPos(x = 727, y = 78, z = 28373),
        BlockPos(x = 728, y = 78, z = 28376),
        BlockPos(x = 729, y = 81, z = 28379),
        BlockPos(x = 730, y = 71, z = 28382),
        BlockPos(x = 731, y = 67, z = 28382),
        BlockPos(x = 731, y = 78, z = 28382),
        BlockPos(x = 733, y = 81, z = 28379),
        BlockPos(x = 734, y = 78, z = 28376)
    )
    val bannerStates = listOf(
        Pair(
            BannerState(
                DyeColor.WHITE, listOf(
                    Pattern(DyeColor.BLACK, PatternType.STRIPE_MIDDLE),
                    Pattern(DyeColor.WHITE, PatternType.STRIPE_CENTER)
                )
            )
            , BannerState(
                DyeColor.WHITE, listOf(
                    Pattern(DyeColor.RED, PatternType.STRIPE_SMALL),
                    Pattern(DyeColor.WHITE, PatternType.GRADIENT_UP),
                    Pattern(DyeColor.WHITE, PatternType.HALF_HORIZONTAL),
                    Pattern(DyeColor.BLACK, PatternType.STRIPE_MIDDLE),
                    Pattern(DyeColor.WHITE, PatternType.STRIPE_CENTER)
                )
            )
        ),
        Pair(
            BannerState(
                DyeColor.WHITE, listOf(
                    Pattern(DyeColor.BLACK, PatternType.HALF_HORIZONTAL),
                    Pattern(DyeColor.BLACK, PatternType.CREEPER),
                    Pattern(DyeColor.WHITE, PatternType.CREEPER),
                    Pattern(DyeColor.BLACK, PatternType.GRADIENT)
                )
            )
            , BannerState(
                DyeColor.WHITE, listOf(
                    Pattern(DyeColor.RED, PatternType.MOJANG),
                    Pattern(DyeColor.BLACK, PatternType.HALF_HORIZONTAL),
                    Pattern(DyeColor.BLACK, PatternType.TRIANGLE_BOTTOM),
                    Pattern(DyeColor.WHITE, PatternType.CREEPER),
                    Pattern(DyeColor.WHITE, PatternType.TRIANGLES_BOTTOM),
                    Pattern(DyeColor.BLACK, PatternType.GRADIENT)
                )
            )
        )
    )
    val knifeDistance = 2.0
    val knifeDamage = 5.0
    val interactDistance = 2.0
    val gunDamage = 10.0
    val playerRespawnHealth = 6.0
    val playerVotingDuration = 5
    val playerDyingDuration = 5
    val votingTextHeight = 3.0
    val transformedDamage = 10.0
    val torchDuration = 20.0
    val playerPickupDistance = 1.0
    val outlineInnocentDelay = 5.0
    val outlineInnocentDuration = 1.0
    val levels = listOf(
        Level(
            72,
            65,
            10,
            10,
            10,
            5,
            10,
            listOf(
                EntityPos(x = 714.5, y = 67.5, z = 28361.03125),
                EntityPos(x = 733.5, y = 67.5, z = 28329.03125),
                EntityPos(x = 708.5, y = 67.5, z = 28341.96875),
                EntityPos(x = 722.03125, y = 67.5, z = 28338.5),
                EntityPos(x = 697.03125, y = 67.5, z = 28369.5),
                EntityPos(x = 721.03125, y = 67.5, z = 28347.5),
                EntityPos(x = 707.5, y = 67.5, z = 28345.03125),
                EntityPos(x = 693.5, y = 67.5, z = 28329.03125),
                EntityPos(x = 729.03125, y = 67.5, z = 28363.5),
                EntityPos(x = 731.5, y = 67.5, z = 28379.03125),
                EntityPos(x = 681.5, y = 67.5, z = 28365.96875),
                EntityPos(x = 693.96875, y = 67.5, z = 28373.5),
                EntityPos(x = 693.96875, y = 67.5, z = 28355.5),
                EntityPos(x = 676.5, y = 67.5, z = 28345.03125),
                EntityPos(x = 685.96875, y = 67.5, z = 28353.5),
                EntityPos(x = 701.96875, y = 67.5, z = 28355.5),
                EntityPos(x = 688.5, y = 67.5, z = 28338.03125)
            ), // bloodBagPoses
            1,
            5,
            listOf(
                BlockPos(x = 682, y = 67, z = 28330),
                BlockPos(x = 689, y = 67, z = 28353),
                BlockPos(x = 706, y = 56, z = 28346),
                BlockPos(x = 717, y = 67, z = 28345),
                BlockPos(x = 668, y = 67, z = 28345),
                BlockPos(x = 666, y = 67, z = 28365),
                BlockPos(x = 681, y = 67, z = 28369),
                BlockPos(x = 704, y = 68, z = 28375),
                BlockPos(x = 714, y = 67, z = 28380),
                BlockPos(x = 724, y = 67, z = 28351),
                BlockPos(x = 725, y = 69, z = 28365),
                BlockPos(x = 727, y = 71, z = 28381)
            ), // fusePositions
            5,
            listOf(
                BlockPos(x = 671, y = 66, z = 28344),
                BlockPos(x = 682, y = 66, z = 28342),
                BlockPos(x = 682, y = 66, z = 28347),
                BlockPos(x = 694, y = 66, z = 28358),
                BlockPos(x = 694, y = 66, z = 28381),
                BlockPos(x = 682, y = 66, z = 28334),
                BlockPos(x = 710, y = 66, z = 28336),
                BlockPos(x = 704, y = 66, z = 28358),
                BlockPos(x = 704, y = 66, z = 28368),
                BlockPos(x = 721, y = 66, z = 28361),
                BlockPos(x = 734, y = 66, z = 28360),
                BlockPos(x = 720, y = 66, z = 28378)
            ), // fuseSocketPositions
            listOf(
                BlockPos(x = 724, y = 71, z = 28350),
                BlockPos(x = 724, y = 71, z = 28351),
                BlockPos(x = 724, y = 72, z = 28350),
                BlockPos(x = 724, y = 72, z = 28351),
                BlockPos(x = 725, y = 70, z = 28350),
                BlockPos(x = 725, y = 70, z = 28351),
                BlockPos(x = 725, y = 71, z = 28349),
                BlockPos(x = 725, y = 71, z = 28350),
                BlockPos(x = 725, y = 71, z = 28351),
                BlockPos(x = 725, y = 72, z = 28349),
                BlockPos(x = 725, y = 72, z = 28350),
                BlockPos(x = 725, y = 72, z = 28351),
                BlockPos(x = 726, y = 69, z = 28350),
                BlockPos(x = 726, y = 69, z = 28351),
                BlockPos(x = 726, y = 70, z = 28349),
                BlockPos(x = 726, y = 70, z = 28350),
                BlockPos(x = 726, y = 70, z = 28351),
                BlockPos(x = 726, y = 71, z = 28349),
                BlockPos(x = 726, y = 71, z = 28350),
                BlockPos(x = 726, y = 71, z = 28351),
                BlockPos(x = 726, y = 72, z = 28349),
                BlockPos(x = 726, y = 72, z = 28350),
                BlockPos(x = 726, y = 72, z = 28351),
                BlockPos(x = 727, y = 68, z = 28350),
                BlockPos(x = 727, y = 68, z = 28351),
                BlockPos(x = 727, y = 69, z = 28349),
                BlockPos(x = 727, y = 69, z = 28350),
                BlockPos(x = 727, y = 69, z = 28351),
                BlockPos(x = 727, y = 70, z = 28349),
                BlockPos(x = 727, y = 70, z = 28350),
                BlockPos(x = 727, y = 70, z = 28351),
                BlockPos(x = 727, y = 71, z = 28349),
                BlockPos(x = 727, y = 71, z = 28350),
                BlockPos(x = 727, y = 71, z = 28351),
                BlockPos(x = 727, y = 72, z = 28349),
                BlockPos(x = 727, y = 72, z = 28350),
                BlockPos(x = 727, y = 72, z = 28351),
                BlockPos(x = 728, y = 67, z = 28350),
                BlockPos(x = 728, y = 67, z = 28351),
                BlockPos(x = 728, y = 68, z = 28349),
                BlockPos(x = 728, y = 68, z = 28350),
                BlockPos(x = 728, y = 68, z = 28351),
                BlockPos(x = 728, y = 69, z = 28349),
                BlockPos(x = 728, y = 69, z = 28350),
                BlockPos(x = 728, y = 69, z = 28351),
                BlockPos(x = 728, y = 70, z = 28349),
                BlockPos(x = 728, y = 70, z = 28350),
                BlockPos(x = 728, y = 70, z = 28351),
                BlockPos(x = 728, y = 71, z = 28349),
                BlockPos(x = 728, y = 71, z = 28350),
                BlockPos(x = 728, y = 71, z = 28351),
                BlockPos(x = 728, y = 72, z = 28349),
                BlockPos(x = 728, y = 72, z = 28350),
                BlockPos(x = 728, y = 72, z = 28351),
                BlockPos(x = 724, y = 71, z = 28352),
                BlockPos(x = 724, y = 72, z = 28352),
                BlockPos(x = 725, y = 70, z = 28352),
                BlockPos(x = 725, y = 71, z = 28352),
                BlockPos(x = 725, y = 71, z = 28353),
                BlockPos(x = 725, y = 72, z = 28352),
                BlockPos(x = 725, y = 72, z = 28353),
                BlockPos(x = 726, y = 69, z = 28352),
                BlockPos(x = 726, y = 70, z = 28352),
                BlockPos(x = 726, y = 70, z = 28353),
                BlockPos(x = 726, y = 71, z = 28352),
                BlockPos(x = 726, y = 71, z = 28353),
                BlockPos(x = 726, y = 72, z = 28352),
                BlockPos(x = 726, y = 72, z = 28353),
                BlockPos(x = 727, y = 68, z = 28352),
                BlockPos(x = 727, y = 69, z = 28352),
                BlockPos(x = 727, y = 69, z = 28353),
                BlockPos(x = 727, y = 70, z = 28352),
                BlockPos(x = 727, y = 70, z = 28353),
                BlockPos(x = 727, y = 71, z = 28352),
                BlockPos(x = 727, y = 71, z = 28353),
                BlockPos(x = 727, y = 72, z = 28352),
                BlockPos(x = 727, y = 72, z = 28353),
                BlockPos(x = 728, y = 67, z = 28352),
                BlockPos(x = 728, y = 68, z = 28352),
                BlockPos(x = 728, y = 68, z = 28353),
                BlockPos(x = 728, y = 69, z = 28352),
                BlockPos(x = 728, y = 69, z = 28353),
                BlockPos(x = 728, y = 70, z = 28352),
                BlockPos(x = 728, y = 70, z = 28353),
                BlockPos(x = 728, y = 71, z = 28352),
                BlockPos(x = 728, y = 71, z = 28353),
                BlockPos(x = 728, y = 72, z = 28352),
                BlockPos(x = 728, y = 72, z = 28353)
            ), // doorPositions
            listOf(
                BlockPos(703, 65, 28335) to BlockPos(700, 67, 28328),
                BlockPos(724, 65, 28347) to BlockPos(728, 67, 28344),
                BlockPos(690, 65, 28371) to BlockPos(688, 67, 28366)
            ), // objAs
            listOf(
                ThreePair(
                    BlockPos(668, 66, 28362),
                    BlockPos(676, 67, 28360),
                    BlockPos(669, 66, 28364)
                ),
                ThreePair(
                    BlockPos(702, 66, 28370),
                    BlockPos(705, 67, 28366),
                    BlockPos(700, 66, 28371)
                ),
                ThreePair(
                    BlockPos(715, 66, 28379),
                    BlockPos(712, 67, 28373),
                    BlockPos(714, 66, 28377)
                )
            ), // objBs
            listOf(
                ThreePair(
                    BlockPos(717, 66, 28355),
                    BlockPos(710, 67, 28358),
                    BlockPos(714, 66, 28356)
                ),
                ThreePair(
                    BlockPos(724, 66, 28381),
                    BlockPos(725, 67, 28376),
                    BlockPos(723, 66, 28378)
                ),
                ThreePair(
                    BlockPos(723, 66, 28339),
                    BlockPos(727, 67, 28333),
                    BlockPos(724, 66, 28336)
                )
            ), // objCs
            listOf(
                EntityPos(x = 722.5464125619044, y = 66.0, z = 28344.540049349045),
                EntityPos(x = 721.5739706303248, y = 66.0, z = 28358.397684600222),
                EntityPos(x = 734.4390964483626, y = 66.0, z = 28358.41372343969),
                EntityPos(x = 734.4396918170718, y = 66.0, z = 28332.37436281586),
                EntityPos(x = 720.603564405921, y = 66.0, z = 28334.494861808325),
                EntityPos(x = 712.7108944442418, y = 66.0625, z = 28334.31771068681),
                EntityPos(x = 718.6214485228933, y = 66.0, z = 28344.439337679607),
                EntityPos(x = 694.0486080737801, y = 66.0, z = 28334.296239084673),
                EntityPos(x = 706.4888427880364, y = 66.0, z = 28344.616759126984),
                EntityPos(x = 681.9961641749903, y = 66.0, z = 28329.041807429232),
                EntityPos(x = 718.5790089744046, y = 66.0, z = 28358.547252957153),
                EntityPos(x = 686.045420225446, y = 66.0, z = 28341.87055601281),
                EntityPos(x = 704.6446986242826, y = 66.0, z = 28366.30859847229),
                EntityPos(x = 718.2492596328054, y = 66.0625, z = 28368.704278148547),
                EntityPos(x = 717.5570711437284, y = 66.0, z = 28381.61290044562),
                EntityPos(x = 696.7105300031169, y = 66.0625, z = 28355.518940038422),
                EntityPos(x = 703.9613234999167, y = 66.5, z = 28374.32777564101),
                EntityPos(x = 694.4511365647663, y = 66.0, z = 28366.54470934274),
                EntityPos(x = 689.6370635035607, y = 67.0, z = 28355.440220526798),
                EntityPos(x = 682.2110878914609, y = 66.0, z = 28371.335276709728),
                EntityPos(x = 693.3602377008773, y = 66.0, z = 28379.448209817983),
                EntityPos(x = 678.2533358519345, y = 66.0, z = 28361.97509347506),
                EntityPos(x = 686.4546688318042, y = 66.0, z = 28349.66210269308),
                EntityPos(x = 674.5382818023561, y = 66.0, z = 28347.729913490017),
                EntityPos(x = 665.7380926771201, y = 67.0, z = 28346.28763266083),
                EntityPos(x = 734.081631894263, y = 66.0, z = 28374.33095064806),
                EntityPos(x = 733.1101809570317, y = 70.0, z = 28378.647286774823),
                EntityPos(x = 726.0223364188118, y = 66.0, z = 28382.372483333278),
                EntityPos(x = 692.4317822031619, y = 66.0, z = 28336.675784039264),
                EntityPos(x = 704.481129991777, y = 66.0625, z = 28342.04177987144),
                EntityPos(x = 686.53319593519, y = 66.0, z = 28360.549576106747),
                EntityPos(x = 726.3463054173654, y = 68.0, z = 28363.482905523506)
            ), // itemSpawnPlaces
            10,
            listOf(
                EntityPos(x = 726.275986729928, y = 66.0, z = 28356.82808153551),
                EntityPos(x = 730.0247261704653, y = 66.0625, z = 28367.76422589824),
                EntityPos(x = 718.8806961274023, y = 66.0625, z = 28372.94898510323),
                EntityPos(x = 708.4939485542445, y = 66.0625, z = 28365.77843926608),
                EntityPos(x = 708.3624457044829, y = 66.0, z = 28347.611138898836),
                EntityPos(x = 715.4866250752975, y = 66.0625, z = 28337.951758320163),
                EntityPos(x = 697.855648710733, y = 66.0625, z = 28337.13827777011),
                EntityPos(x = 727.8995971718634, y = 66.0625, z = 28332.407693182387),
                EntityPos(x = 726.9743580969849, y = 66.0, z = 28344.877536611726),
                EntityPos(x = 727.5406136730527, y = 70.0, z = 28379.793981559567),
                EntityPos(x = 695.5702794023017, y = 66.0625, z = 28362.16156147366),
                EntityPos(x = 670.5594419961533, y = 66.0, z = 28357.596850182348),
                EntityPos(x = 684.5451794106941, y = 66.0625, z = 28348.60538045905),
                EntityPos(x = 683.5994375146175, y = 66.0, z = 28331.46897023845)
            ) // spawnPoses
        ),
        Level(
            73,
            83,
            10,
            10,
            10,
            5,
            10,
            listOf(
                EntityPos(x = 676.5, y = 75.5, z = 28345.03125),
                EntityPos(x = 675.5, y = 75.5, z = 28365.96875),
                EntityPos(x = 693.96875, y = 75.5, z = 28334.5),
                EntityPos(x = 689.5, y = 75.5, z = 28365.96875),
                EntityPos(x = 707.5, y = 75.5, z = 28341.96875),
                EntityPos(x = 717.96875, y = 75.5, z = 28357.5),
                EntityPos(x = 697.03125, y = 75.5, z = 28375.5),
                EntityPos(x = 718.5, y = 75.5, z = 28377.03125),
                EntityPos(x = 722.5, y = 75.5, z = 28329.03125),
                EntityPos(x = 733.96875, y = 75.5, z = 28350.5),
                EntityPos(x = 721.5, y = 75.5, z = 28341.96875),
                EntityPos(x = 721.03125, y = 75.5, z = 28364.5),
                EntityPos(x = 733.96875, y = 75.5, z = 28377.5)
            ), // bloodBagPoses
            1,
            5,
            listOf(
                BlockPos(x = 689, y = 75, z = 28345),
                BlockPos(x = 693, y = 75, z = 28329),
                BlockPos(x = 717, y = 75, z = 28351),
                BlockPos(x = 665, y = 75, z = 28355),
                BlockPos(x = 670, y = 75, z = 28362),
                BlockPos(x = 697, y = 75, z = 28381),
                BlockPos(x = 717, y = 75, z = 28381),
                BlockPos(x = 721, y = 75, z = 28337),
                BlockPos(x = 723, y = 74, z = 28356),
                BlockPos(x = 729, y = 75, z = 28380)
            ), // fusePositions
            5,
            listOf(
                BlockPos(x = 680, y = 74, z = 28328),
                BlockPos(x = 678, y = 74, z = 28358),
                BlockPos(x = 688, y = 74, z = 28358),
                BlockPos(x = 712, y = 74, z = 28342),
                BlockPos(x = 718, y = 74, z = 28344),
                BlockPos(x = 664, y = 74, z = 28366),
                BlockPos(x = 696, y = 74, z = 28368),
                BlockPos(x = 712, y = 74, z = 28379),
                BlockPos(x = 726, y = 74, z = 28360),
                BlockPos(x = 734, y = 74, z = 28382)
            ), // fuseSocketPositions
            listOf(
                BlockPos(x = 682, y = 74, z = 28367),
                BlockPos(x = 682, y = 75, z = 28367),
                BlockPos(x = 682, y = 76, z = 28367),
                BlockPos(x = 683, y = 74, z = 28367),
                BlockPos(x = 683, y = 75, z = 28367),
                BlockPos(x = 683, y = 76, z = 28367),
                BlockPos(x = 683, y = 77, z = 28367),
                BlockPos(x = 684, y = 74, z = 28367),
                BlockPos(x = 684, y = 75, z = 28367),
                BlockPos(x = 684, y = 76, z = 28367)
            ), // doorPositions
            listOf(
                BlockPos(711, 73, 28335) to BlockPos(705, 75, 28335),
                BlockPos(724, 73, 28363) to BlockPos(729, 75, 28367),
                BlockPos(673, 73, 28351) to BlockPos(680, 75, 28350)
            ), // objAs
            listOf(
                ThreePair(
                    BlockPos(713, 74, 28354),
                    BlockPos(712, 75, 28360),
                    BlockPos(715, 74, 28355)
                ),
                ThreePair(
                    BlockPos(724, 74, 28381),
                    BlockPos(720, 75, 28374),
                    BlockPos(722, 74, 28380)
                )
            ), // objBs
            listOf(
                ThreePair(
                    BlockPos(687, 74, 28335),
                    BlockPos(694, 75, 28336),
                    BlockPos(690, 74, 28335)
                ),
                ThreePair(
                    BlockPos(708, 74, 28381),
                    BlockPos(703, 75, 28382),
                    BlockPos(707, 74, 28378)
                )
            ), // objCs
            listOf(
                EntityPos(x = 728.660894688046, y = 74.0, z = 28360.56214982678),
                EntityPos(x = 730.6318919605487, y = 74.0625, z = 28349.214771154424),
                EntityPos(x = 728.5911157867291, y = 74.0, z = 28336.66724950561),
                EntityPos(x = 734.3418059599659, y = 74.0, z = 28328.900573565283),
                EntityPos(x = 720.51726309406, y = 75.0625, z = 28336.560377867037),
                EntityPos(x = 702.4133202392369, y = 74.0, z = 28336.61337337401),
                EntityPos(x = 684.6398898281303, y = 75.0625, z = 28340.6683185167),
                EntityPos(x = 685.2254204250128, y = 74.0, z = 28329.406446034132),
                EntityPos(x = 674.5309840040732, y = 75.0625, z = 28346.44604134583),
                EntityPos(x = 674.5309840040732, y = 75.0625, z = 28346.44604134583),
                EntityPos(x = 669.9903849576392, y = 74.0, z = 28357.510096808288),
                EntityPos(x = 666.9385152574529, y = 74.0, z = 28360.673337694927),
                EntityPos(x = 686.410516308501, y = 74.0625, z = 28360.88438328056),
                EntityPos(x = 702.0255500303284, y = 74.0625, z = 28366.091836732685),
                EntityPos(x = 699.2422758507659, y = 74.0, z = 28379.849449974554),
                EntityPos(x = 718.0740325926832, y = 74.0625, z = 28361.069832250545),
                EntityPos(x = 713.4758401268222, y = 74.0, z = 28349.901745709878),
                EntityPos(x = 704.4511198325337, y = 74.0, z = 28358.14788900471),
                EntityPos(x = 696.9625093408267, y = 74.0625, z = 28350.082225956416),
                EntityPos(x = 715.3945816640902, y = 74.5, z = 28376.547420259918),
                EntityPos(x = 734.3304814074685, y = 74.0, z = 28374.30645223889)
            ), // itemSpawnPlaces
            10,
            listOf(
                EntityPos(x = 716.5087398168839, y = 74.0625, z = 28331.279865251643),
                EntityPos(x = 730.5418573856325, y = 74.0625, z = 28332.387152531177),
                EntityPos(x = 728.9160927932132, y = 74.0625, z = 28356.222011634058),
                EntityPos(x = 722.6974256922915, y = 74.0, z = 28363.948238623903),
                EntityPos(x = 731.0400272085712, y = 74.0625, z = 28372.85026449092),
                EntityPos(x = 716.6074562827497, y = 74.0625, z = 28368.102926057603),
                EntityPos(x = 700.4824882148071, y = 74.0625, z = 28359.331486357954),
                EntityPos(x = 691.9917240871306, y = 74.0, z = 28347.743572942305),
                EntityPos(x = 683.812715450076, y = 74.0625, z = 28353.826854504183),
                EntityPos(x = 668.0526534313598, y = 75.0625, z = 28346.646288619027),
                EntityPos(x = 702.9104481711756, y = 74.0625, z = 28339.363094161494),
                EntityPos(x = 698.7390713082569, y = 74.0, z = 28375.709679323227)
            )// spawnPoses
        )
    )
}

// spiders
// Location{world=CraftWorld{name=world},x=678.5163110416033,y=85.0,z=28344.467745576207,pitch=-8.3459425,yaw=-320.58594}
// Location{world=CraftWorld{name=world},x=694.5602909658236,y=93.20000004768372,z=28350.555475464374,pitch=59.050117,yaw=-221.58008}
