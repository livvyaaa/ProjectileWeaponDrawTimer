package org.violetyy.projectileweapontimer;

import java.text.DecimalFormat;
import java.util.OptionalDouble;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;


public class ModClient implements ClientModInitializer {

	@Nullable
	public static ModConfig config;

	private boolean finishedCharging = false;

	@Override
	public void onInitializeClient() {
		AutoConfig.register(ModConfig.class, Toml4jConfigSerializer::new);
		config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

		HudRenderCallback.EVENT.register((context, tickDelta) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			ClientPlayerEntity player = client.player;
			if (player == null) return;
			if (config == null) {
				throw new IllegalStateException("Config not initialized!");
			}

			OptionalDouble potentialProgress = getProgress(player);

			potentialProgress.ifPresent(progress -> {
				int centerX = client.getWindow().getScaledWidth() / 2;
				int centerY = client.getWindow().getScaledHeight() / 2;
				if (config.showTimer) {
					drawTimer(context, progress, client, player, centerX, centerY);
				}

				if (config.showCrosshair) {
					drawCrosshair(context, progress, centerX, centerY);
				}
			});
		});
	}

	private void drawTimer(DrawContext context, double progress, MinecraftClient client, ClientPlayerEntity player, int centerX, int centerY) {
		if (config == null) {
			throw new IllegalStateException("Config not initialized!");
		}

		Text text = Text.literal(new DecimalFormat("#0.00").format(getUseSeconds(player)) + "s");

		int textHalfWidth = client.textRenderer.getWidth(text) / 2;

		int textColor;
		if (progress >= 0.99) {
			if (!finishedCharging && config.playDing && client.world != null) {
				final Vec3d pos = player.getPos();
				client.world.playSound(pos.x, pos.y, pos.z, SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.MASTER, config.dingVolume, config.dingPitch, false);
			}
			finishedCharging = true;
			textColor = (config.redAfter << 16) + (config.greenAfter << 8) + config.blueAfter;
		} else {
			finishedCharging = false;
			textColor = (config.redBefore << 16) + (config.greenBefore << 8) + config.blueBefore;
		}

		context.drawText(
			client.textRenderer,
			text,
			centerX - textHalfWidth, centerY + 18,
			textColor,
			true
		);
	}

	private static void drawCrosshair(DrawContext context, double progress, int centerX, int centerY) {
		if (config == null) {
			throw new IllegalStateException("Config not initialized!");
		}

		int armLength = config.crosshairArmLength;
		int color;

		int startPos = config.crosshairDistance;

		float t = (float) progress;
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

		if (progress >= 0.99) {
			color = 0xFF000000 + (config.redAfter << 16) + (config.greenAfter << 8) + config.blueAfter;
		} else {
			color = 0xFF000000 + (config.redBefore << 16) + (config.greenBefore << 8) + config.blueBefore;
		}

		if (!config.inverted) {
			context.drawVerticalLine(tlX, tlY - armLength - 1, tlY, color);
			context.drawHorizontalLine(tlX - armLength, tlX, tlY, color);

			context.drawVerticalLine(trX, trY - armLength - 1, trY, color);
			context.drawHorizontalLine(trX, trX + armLength, trY, color);

			context.drawVerticalLine(blX, blY, blY + armLength + 1, color);
			context.drawHorizontalLine(blX - armLength, blX, blY, color);

			context.drawVerticalLine(brX, brY, brY + armLength + 1, color);
			context.drawHorizontalLine(brX, brX + armLength, brY, color);
		} else if (!config.square) {
			context.drawHorizontalLine(tlX, trX, tlY - armLength - 1, color);
			context.drawVerticalLine(trX + armLength + 1, trY - 1, brY + 1, color);
			context.drawHorizontalLine(blX, brX, blY + armLength + 1, color);
			context.drawVerticalLine(blX - armLength - 1, tlY - 1, blY + 1, color);
		} else {
			context.drawHorizontalLine(tlX, trX, tlY - armLength - 1 + (armLength + 1), color);
			context.drawVerticalLine(trX + armLength + 1 - (armLength + 1), trY - 1, brY + 1, color);
			context.drawHorizontalLine(blX, brX, blY + armLength + 1 - (armLength + 1), color);
			context.drawVerticalLine(blX - armLength - 1 + (armLength + 1), tlY - 1, blY + 1, color);
		}
	}

	private static OptionalDouble getProgress(PlayerEntity player) {
		if (player.isUsingItem()) {
			ItemStack stack = player.getActiveItem();
			Item item = stack.getItem();

			if (item == Items.BOW) {
				double progress = player.getItemUseTime() / 20.0;
				return OptionalDouble.of(Math.min(progress, 1.0));
			}
			if (item == Items.CROSSBOW) {
				double progress = (double) player.getItemUseTime() / CrossbowItem.getPullTime(stack);
				return OptionalDouble.of(Math.min(progress, 1.0));
			}
			if (item == Items.TRIDENT) {
				double progress = player.getItemUseTime() / 10.0;
				return OptionalDouble.of(Math.min(progress, 1.0));
			}
		}
		return OptionalDouble.empty();
	}

	private static double getUseSeconds(PlayerEntity player) {
		ItemStack stack = player.getActiveItem();
		Item item = stack.getItem();

		if (item == Items.BOW) {
			return Math.min(player.getItemUseTime(), 20) / 20.0;
		}
		if (item == Items.CROSSBOW) {
			return Math.min(player.getItemUseTime(), CrossbowItem.getPullTime(stack)) / 20.0;
		}
		if (item == Items.TRIDENT) {
			return Math.min(player.getItemUseTime(), 10) / 20.0;
		}
		return 0;
	}

}