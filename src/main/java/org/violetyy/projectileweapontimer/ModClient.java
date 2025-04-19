package org.violetyy.projectileweapontimer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;


public class ModClient implements ClientModInitializer {

	@Nullable
	public static ModConfig config;

	private boolean finishedCharging = false;

	@Override
	public void onInitializeClient() {

		AutoConfig.register(ModConfig.class, Toml4jConfigSerializer::new);
		config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

		HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.player == null) return;
			assert config != null;
			if (!config.showTimer) return;

			if (client.player instanceof ProjectileWeaponTimerUtils access) {
				int ticks = access.templateProject$getHeldTicks();

				if (ticks != 0) {
					Text text = Text.literal(new DecimalFormat("#0.00").format(ticks * 0.05) + "s");

					int centerX = client.getWindow().getScaledWidth() / 2;
					int centerY = client.getWindow().getScaledHeight() / 2;

					int textHalfWidth = client.textRenderer.getWidth(text) / 2;

					int textColor;
					if (ticks >= 25 - (5 * access.templateProject$getqcLevel())) {
						if (!finishedCharging && config.playDing && client.world != null) {
							final Vec3d pos = client.player.getPos();
							client.world.playSound(pos.x, pos.y, pos.z, SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.MASTER, config.dingVolume, config.dingPitch, false);
						}
						finishedCharging = true;
						textColor = (config.redAfter << 16) + (config.greenAfter << 8) + config.blueAfter;
					} else {
						finishedCharging = false;
						textColor = (config.redBefore << 16) + (config.greenBefore << 8) + config.blueBefore;
					}

					drawContext.drawText(
							client.textRenderer,
							text,
							centerX - textHalfWidth, centerY + 18,
							textColor,
							true
					);
				}
			}
		});
	}

}