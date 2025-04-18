package org.violetyy.projectileweapontimer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.text.Text;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.text.DecimalFormat;


public class ModClient implements ClientModInitializer {

	@Nullable
	public static ModConfig config;

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
						textColor = (config.redAfter << 16) + (config.greenAfter << 8) + config.blueAfter;
					} else {
						textColor = (config.redBefore << 16) + (config.greenBefore << 8) + config.blueBefore;
					}

					drawContext.drawText(
							client.textRenderer,
							text,
							centerX - textHalfWidth, (int) (centerY * 1.1),
							textColor,
							true
					);
				}
			}
		});
	}
}