package org.violetyy.projectileweapontimer;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "projectileweapontimer")
public class ModConfig implements ConfigData {
    public boolean showCrosshair = true;
    public boolean inverted = false;
    public boolean square = false;
    public int crosshairArmLength = 5;
    public int crosshairDistance = 5;
    public boolean showTimer = true;
    public int redAfter = 0x32;
    public int greenAfter = 0xCD;
    public int blueAfter = 0x32;
    public int redBefore = 0xFF;
    public int greenBefore = 0xFF;
    public int blueBefore = 0xFF;

    public boolean playDing = true;
    public float dingVolume = 0.7f;
    public float dingPitch = 0.4f;
}
