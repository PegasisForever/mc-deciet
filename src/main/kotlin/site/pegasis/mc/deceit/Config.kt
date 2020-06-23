package site.pegasis.mc.deceit

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Entity

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
    val lightTime: Int,
    val darkTime: Int,
    val rageTime: Int,
    val runTime: Int,
    val bloodPackPosesCount: Int,
    val bloodPackPoses: List<EntityPos>,
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
        BlockPos(x = 720, y = 67, z = 28344),
        BlockPos(x = 720, y = 75, z = 28337),
        BlockPos(x = 721, y = 67, z = 28341),
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
        BlockPos(x = 705, y = 78, z = 28335),
        BlockPos(x = 709, y = 70, z = 28335),
        BlockPos(x = 711, y = 70, z = 28329),
        BlockPos(x = 711, y = 70, z = 28333),
        BlockPos(x = 711, y = 78, z = 28329),
        BlockPos(x = 713, y = 70, z = 28335),
        BlockPos(x = 717, y = 70, z = 28335),
        BlockPos(x = 717, y = 78, z = 28335),
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
        BlockPos(x = 727, y = 70, z = 28329),
        BlockPos(x = 727, y = 70, z = 28333),
        BlockPos(x = 727, y = 78, z = 28329),
        BlockPos(x = 727, y = 78, z = 28333),
        BlockPos(x = 729, y = 70, z = 28335),
        BlockPos(x = 729, y = 78, z = 28335),
        BlockPos(x = 733, y = 70, z = 28335),
        BlockPos(x = 733, y = 78, z = 28335),
        BlockPos(x = 720, y = 67, z = 28358),
        BlockPos(x = 720, y = 86, z = 28363),
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
        BlockPos(x = 697, y = 70, z = 28343),
        BlockPos(x = 697, y = 70, z = 28351),
        BlockPos(x = 697, y = 78, z = 28343),
        BlockPos(x = 697, y = 78, z = 28351),
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
        BlockPos(x = 688, y = 86, z = 28363),
        BlockPos(x = 691, y = 79, z = 28352),
        BlockPos(x = 695, y = 70, z = 28361),
        BlockPos(x = 695, y = 70, z = 28365),
        BlockPos(x = 695, y = 78, z = 28361),
        BlockPos(x = 695, y = 78, z = 28365),
        BlockPos(x = 695, y = 89, z = 28353),
        BlockPos(x = 695, y = 89, z = 28357),
        BlockPos(x = 697, y = 70, z = 28359),
        BlockPos(x = 697, y = 78, z = 28359),
        BlockPos(x = 698, y = 87, z = 28363),
        BlockPos(x = 701, y = 70, z = 28359),
        BlockPos(x = 701, y = 78, z = 28359),
        BlockPos(x = 703, y = 70, z = 28361),
        BlockPos(x = 703, y = 70, z = 28365),
        BlockPos(x = 703, y = 78, z = 28361),
        BlockPos(x = 703, y = 78, z = 28365),
        BlockPos(x = 703, y = 89, z = 28353),
        BlockPos(x = 703, y = 89, z = 28357),
        BlockPos(x = 689, y = 74, z = 28377),
        BlockPos(x = 689, y = 85, z = 28369),
        BlockPos(x = 689, y = 85, z = 28373),
        BlockPos(x = 694, y = 71, z = 28375),
        BlockPos(x = 695, y = 89, z = 28369),
        BlockPos(x = 695, y = 89, z = 28373),
        BlockPos(x = 696, y = 79, z = 28375),
        BlockPos(x = 699, y = 71, z = 28368),
        BlockPos(x = 700, y = 75, z = 28378),
        BlockPos(x = 702, y = 81, z = 28375),
        BlockPos(x = 702, y = 86, z = 28379),
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
        BlockPos(x = 729, y = 81, z = 28379),
        BlockPos(x = 730, y = 71, z = 28382),
        BlockPos(x = 731, y = 67, z = 28382),
        BlockPos(x = 733, y = 81, z = 28379),
        BlockPos(x = 681, y = 72, z = 28329),
        BlockPos(x = 681, y = 72, z = 28333),
        BlockPos(x = 682, y = 81, z = 28335),
        BlockPos(x = 683, y = 81, z = 28334),
        BlockPos(x = 684, y = 81, z = 28335),
        BlockPos(x = 686, y = 75, z = 28331),
        BlockPos(x = 673, y = 75, z = 28347),
        BlockPos(x = 674, y = 81, z = 28351),
        BlockPos(x = 674, y = 86, z = 28344),
        BlockPos(x = 675, y = 71, z = 28344),
        BlockPos(x = 675, y = 81, z = 28350),
        BlockPos(x = 676, y = 81, z = 28351),
        BlockPos(x = 678, y = 70, z = 28346),
        BlockPos(x = 680, y = 68, z = 28339),
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
        BlockPos(x = 687, y = 70, z = 28361),
        BlockPos(x = 687, y = 70, z = 28365),
        BlockPos(x = 687, y = 78, z = 28361),
        BlockPos(x = 687, y = 78, z = 28365),
        BlockPos(x = 687, y = 86, z = 28363),
        BlockPos(x = 687, y = 89, z = 28353),
        BlockPos(x = 687, y = 89, z = 28357),
        BlockPos(x = 679, y = 74, z = 28373),
        BlockPos(x = 680, y = 71, z = 28375),
        BlockPos(x = 681, y = 92, z = 28368),
        BlockPos(x = 681, y = 92, z = 28374),
        BlockPos(x = 682, y = 74, z = 28379),
        BlockPos(x = 687, y = 71, z = 28368),
        BlockPos(x = 662, y = 64, z = 28348),
        BlockPos(x = 666, y = 81, z = 28351),
        BlockPos(x = 667, y = 71, z = 28344),
        BlockPos(x = 667, y = 81, z = 28350),
        BlockPos(x = 668, y = 75, z = 28349),
        BlockPos(x = 668, y = 81, z = 28351),
        BlockPos(x = 670, y = 75, z = 28347),
        BlockPos(x = 664, y = 75, z = 28362),
        BlockPos(x = 664, y = 75, z = 28363),
        BlockPos(x = 664, y = 86, z = 28356),
        BlockPos(x = 665, y = 67, z = 28360),
        BlockPos(x = 665, y = 67, z = 28366),
        BlockPos(x = 667, y = 71, z = 28358),
        BlockPos(x = 667, y = 81, z = 28352),
        BlockPos(x = 670, y = 70, z = 28355),
        BlockPos(x = 670, y = 75, z = 28355)
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
            15,
            10,
            10,
            5,
            10,
            listOf(
                EntityPos(x = 681.5, y = 67.5, z = 28365.96875),
                EntityPos(x = 693.96875, y = 67.5, z = 28373.5),
                EntityPos(x = 693.96875, y = 67.5, z = 28355.5),
                EntityPos(x = 676.5, y = 67.5, z = 28345.03125),
                EntityPos(x = 697.03125, y = 67.5, z = 28369.5),
                EntityPos(x = 707.5, y = 67.5, z = 28345.03125),
                EntityPos(x = 693.5, y = 67.5, z = 28329.03125),
                EntityPos(x = 733.5, y = 67.5, z = 28329.03125),
                EntityPos(x = 729.03125, y = 67.5, z = 28363.5),
                EntityPos(x = 731.5, y = 67.5, z = 28379.03125),
                EntityPos(x = 714.5, y = 67.5, z = 28361.03125),
                EntityPos(x = 689.03125, y = 67.5, z = 28337.5),
                EntityPos(x = 708.5, y = 67.5, z = 28341.96875),
                EntityPos(x = 722.03125, y = 67.5, z = 28338.5),
                EntityPos(x = 685.96875, y = 67.5, z = 28353.5),
                EntityPos(x = 701.96875, y = 67.5, z = 28355.5),
                EntityPos(x = 721.03125, y = 67.5, z = 28347.5)
            ),
            1,
            5,
            listOf(
                BlockPos(x = 668, y = 67, z = 28345),
                BlockPos(x = 666, y = 67, z = 28365),
                BlockPos(x = 681, y = 67, z = 28369),
                BlockPos(x = 689, y = 67, z = 28353),
                BlockPos(x = 682, y = 67, z = 28330),
                BlockPos(x = 717, y = 67, z = 28345),
                BlockPos(x = 704, y = 68, z = 28375),
                BlockPos(x = 714, y = 67, z = 28380),
                BlockPos(x = 724, y = 67, z = 28351),
                BlockPos(x = 725, y = 69, z = 28365),
                BlockPos(x = 727, y = 71, z = 28381)
            ),
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
            ),
            listOf(
                BlockPos(x = 682, y = 74, z = 28367),
                BlockPos(x = 683, y = 74, z = 28367),
                BlockPos(x = 684, y = 74, z = 28367),
                BlockPos(x = 724, y = 71, z = 28350),
                BlockPos(x = 724, y = 71, z = 28351),
                BlockPos(x = 724, y = 72, z = 28350),
                BlockPos(x = 724, y = 72, z = 28351),
                BlockPos(x = 724, y = 73, z = 28349),
                BlockPos(x = 724, y = 73, z = 28350),
                BlockPos(x = 724, y = 73, z = 28351),
                BlockPos(x = 725, y = 70, z = 28350),
                BlockPos(x = 725, y = 70, z = 28351),
                BlockPos(x = 725, y = 71, z = 28349),
                BlockPos(x = 725, y = 71, z = 28350),
                BlockPos(x = 725, y = 71, z = 28351),
                BlockPos(x = 725, y = 72, z = 28349),
                BlockPos(x = 725, y = 72, z = 28350),
                BlockPos(x = 725, y = 72, z = 28351),
                BlockPos(x = 725, y = 73, z = 28349),
                BlockPos(x = 725, y = 73, z = 28350),
                BlockPos(x = 725, y = 73, z = 28351),
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
                BlockPos(x = 726, y = 73, z = 28349),
                BlockPos(x = 726, y = 73, z = 28350),
                BlockPos(x = 726, y = 73, z = 28351),
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
                BlockPos(x = 727, y = 73, z = 28349),
                BlockPos(x = 727, y = 73, z = 28350),
                BlockPos(x = 727, y = 73, z = 28351),
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
                BlockPos(x = 728, y = 73, z = 28349),
                BlockPos(x = 728, y = 73, z = 28350),
                BlockPos(x = 728, y = 73, z = 28351),
                BlockPos(x = 724, y = 71, z = 28352),
                BlockPos(x = 724, y = 72, z = 28352),
                BlockPos(x = 724, y = 73, z = 28352),
                BlockPos(x = 724, y = 73, z = 28353),
                BlockPos(x = 725, y = 70, z = 28352),
                BlockPos(x = 725, y = 71, z = 28352),
                BlockPos(x = 725, y = 71, z = 28353),
                BlockPos(x = 725, y = 72, z = 28352),
                BlockPos(x = 725, y = 72, z = 28353),
                BlockPos(x = 725, y = 73, z = 28352),
                BlockPos(x = 725, y = 73, z = 28353),
                BlockPos(x = 726, y = 69, z = 28352),
                BlockPos(x = 726, y = 70, z = 28352),
                BlockPos(x = 726, y = 70, z = 28353),
                BlockPos(x = 726, y = 71, z = 28352),
                BlockPos(x = 726, y = 71, z = 28353),
                BlockPos(x = 726, y = 72, z = 28352),
                BlockPos(x = 726, y = 72, z = 28353),
                BlockPos(x = 726, y = 73, z = 28352),
                BlockPos(x = 726, y = 73, z = 28353),
                BlockPos(x = 727, y = 68, z = 28352),
                BlockPos(x = 727, y = 69, z = 28352),
                BlockPos(x = 727, y = 69, z = 28353),
                BlockPos(x = 727, y = 70, z = 28352),
                BlockPos(x = 727, y = 70, z = 28353),
                BlockPos(x = 727, y = 71, z = 28352),
                BlockPos(x = 727, y = 71, z = 28353),
                BlockPos(x = 727, y = 72, z = 28352),
                BlockPos(x = 727, y = 72, z = 28353),
                BlockPos(x = 727, y = 73, z = 28352),
                BlockPos(x = 727, y = 73, z = 28353),
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
                BlockPos(x = 728, y = 72, z = 28353),
                BlockPos(x = 728, y = 73, z = 28352),
                BlockPos(x = 728, y = 73, z = 28353),
                BlockPos(x = 722, y = 74, z = 28381)
            ),
            listOf(
                BlockPos(703, 65, 28335) to BlockPos(700, 67, 28328),
                BlockPos(724, 65, 28347) to BlockPos(728, 67, 28344),
                BlockPos(690, 65, 28371) to BlockPos(688, 67, 28366)
            ),
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
            ),
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
            ),
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
            ),
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
            )
        )
    )
}

// spiders
// Location{world=CraftWorld{name=world},x=678.5163110416033,y=85.0,z=28344.467745576207,pitch=-8.3459425,yaw=-320.58594}
// Location{world=CraftWorld{name=world},x=694.5602909658236,y=93.20000004768372,z=28350.555475464374,pitch=59.050117,yaw=-221.58008}
