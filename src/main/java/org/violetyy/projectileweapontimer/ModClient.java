package org.violetyy.projectileweapontimer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;


public class ModClient implements ClientModInitializer {

	@Nullable
	public static ModConfig config;

	private boolean finishedCharging = false;

	public static int actualTicksElapsed = 0;


	@Override
	public void onInitializeClient() {

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player instanceof ProjectileWeaponTimerUtils access) {
				if (access.templateProject$getHeldTicks() != 0) {
					actualTicksElapsed++;
				} else {
					actualTicksElapsed = 0;
				}
			}
		});


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

		// crosshair

		HudRenderCallback.EVENT.register((context, tickDelta) -> {

			int centerX = (context.getScaledWindowWidth() - 1) / 2;
			int centerY = (context.getScaledWindowHeight() - 1) / 2;

			MinecraftClient client = MinecraftClient.getInstance();
			if (client.player == null) return;
			assert config != null;
			if (!config.showCrosshair) return;

			if (client.player instanceof ProjectileWeaponTimerUtils access) {
				int ticks = access.templateProject$getHeldTicks();

				if (ticks != 0) {
					float t = MathHelper.clamp(actualTicksElapsed / (25f - (5 * access.templateProject$getqcLevel())), 0f, 1f); // From 0 to 1

					int armLength = 5;
					int color;

					int startPos = 5;

					int tlX = MathHelper.lerp(t, centerX - startPos, centerX);
					int tlY = MathHelper.lerp(t, centerY - startPos, centerY);

					int tlDistX = tlX - centerX;
					int tlDistY = tlY - centerY;

					int trX = centerX - tlDistX;
					int trY = tlY;

					int blX = tlX;
					int blY = centerY - tlDistY;

					int brX = centerX - tlDistX;
					int brY = centerY - tlDistY;

					if (ticks >= 25 - (5 * access.templateProject$getqcLevel())) {
						color = 0xFF000000 + (config.redAfter << 16) + (config.greenAfter << 8) + config.blueAfter;
					} else {
						color = 0xFF000000 + (config.redBefore << 16) + (config.greenBefore << 8) + config.blueBefore;
					}

					context.drawVerticalLine(tlX, tlY - armLength - 1, tlY, color);
					context.drawHorizontalLine(tlX - armLength, tlX, tlY, color);

					context.drawVerticalLine(trX, trY - armLength - 1, trY, color);
					context.drawHorizontalLine(trX, trX + armLength, trY, color);

					context.drawVerticalLine(blX, blY, blY + armLength + 1, color);
					context.drawHorizontalLine(blX - armLength, blX, blY, color);

					context.drawVerticalLine(brX, brY, brY + armLength + 1, color);
					context.drawHorizontalLine(brX, brX + armLength, brY, color);
				}
			}
		});

	}

}