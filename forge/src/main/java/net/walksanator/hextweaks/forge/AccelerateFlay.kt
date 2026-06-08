package net.walksanator.hextweaks.forge

import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import de.dafuqs.spectrum.blocks.titration_barrel.TitrationBarrelBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.monster.Witch
import net.walksanator.hextweaks.HexTweaks
import net.walksanator.hextweaks.casting.MindflayRegistry
import net.walksanator.hextweaks.casting.mindflay.MindflayInput
import net.walksanator.hextweaks.casting.mindflay.MindflayResult
import thedarkcolour.kotlinforforge.forge.vectorutil.v3d.toVec3i
import kotlin.math.absoluteValue
import kotlin.math.min
import kotlin.math.sign

object AccelerateFlay {
    fun skip12hours(input: MindflayInput): MindflayResult {
        if (input.target !is Vec3Iota) {
            HexTweaks.LOGGER.info("Ritual failed: iota is not a vector")
            return MindflayResult(false)
        }
        val pos = (input.target as Vec3Iota).vec3
        val env = input.env
        if (!env.isVecInRange(pos)) {
            HexTweaks.LOGGER.info("Ritual failed: vector not in range")
            return MindflayResult(false)
        }


        val blockpos = BlockPos(
            (if (pos.x()<0) pos.x()-1 else pos.x()).toInt(),
            (if (pos.y()<0) pos.y()-1 else pos.y()).toInt(),
            (if (pos.z()<0) pos.z()-1 else pos.z()).toInt()
        )

        val be = env.world.getBlockEntity(blockpos)?: run{
            HexTweaks.LOGGER.info("Ritual failed: no block entity at position {}", blockpos)
            return MindflayResult(
                false
            )
        } // no block entity at specified position
        if (be !is TitrationBarrelBlockEntity) {
            HexTweaks.LOGGER.info("Ritual failed: the block entity is not a titration barrel")
            return MindflayResult(false)
        } // the block entity is not a titration barrel
        val be_accessor = (be as net.walksanator.hextweaks.forge.mixin.SealTimeAccessor)
        val points = MindflayRegistry.calcuateVillagerPoints(input.inputs)
        val witches = input.inputs.filterIsInstance<Witch>().size * 8
        val skip_ammount = min(points,witches)/8

        var time = be_accessor.sealTime
        time -= skip_ammount.toLong() * 43200000L
        be_accessor.sealTime = time
        be.setChanged()

        MindflayRegistry.performBrainsweeps(input.inputs,env.caster)

        return MindflayResult(true)
    }
}