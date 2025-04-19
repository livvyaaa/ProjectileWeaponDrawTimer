package org.violetyy.projectileweapontimer.mixin;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.violetyy.projectileweapontimer.ProjectileWeaponTimerUtils;

import java.util.Map;


@Mixin(LivingEntity.class)
@Environment(EnvType.CLIENT)
public abstract class ProjectileWeaponTimerMixin extends Entity implements ProjectileWeaponTimerUtils {

    public ProjectileWeaponTimerMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Unique
    private int ticksHeld = 0;

    @Unique
    private int qcLevel = 0;

    @Unique
    private Item previousItem = null;

    @Unique
    private int previousSlot = -1;

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (!(self instanceof PlayerEntity player)) return;

        ItemStack heldItem = player.getMainHandStack();
        Item currentItem = heldItem.getItem();
        int currentSlot = player.getInventory().selectedSlot;

        if (player.isUsingItem()) {

            qcLevel = 0;
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(heldItem);
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                Enchantment enchantment = entry.getKey();
                int level = entry.getValue();

                if (enchantment.getTranslationKey().equals(Enchantments.QUICK_CHARGE.getTranslationKey())) {
                    qcLevel = level;
                    break;
                }
            }

            if (previousSlot != -1 && (previousSlot != currentSlot || previousItem != currentItem)) {
                ticksHeld = 0;
            }

            if (currentItem instanceof CrossbowItem) {
                if (ticksHeld < 25 - (5 * qcLevel)) {
                    ticksHeld++;
                }
            } else if (currentItem instanceof BowItem) {
                qcLevel = 1;
                if (ticksHeld < 20) {
                    ticksHeld++;
                }
            } else if (currentItem instanceof TridentItem) {
                qcLevel = 3;
                if (ticksHeld < 10) {
                    ticksHeld++;
                }
            }
        } else {
            ticksHeld = 0;
        }

        previousItem = currentItem;
        previousSlot = currentSlot;
    }
    @Unique
    public int templateProject$getHeldTicks() {
        return ticksHeld;
    }

    @Unique
    public int templateProject$getqcLevel() {
        return qcLevel;
    }
}
