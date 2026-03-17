package net.myriantics.impenduits.registry.block;

import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.myriantics.impenduits.ImpenduitsCommon;

public abstract class ImpenduitsBlockStateProperties {

    /**
     * Indicates whether an Impenduit Pylon has a power source or not.
     */
    public static final BooleanProperty POWER_SOURCE_PRESENT = BooleanProperty.create("power_source_present");
    /**
     * Indicates that an Impenduit Pylon is actively supporting an Impenduit Field.
     */
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");
    /**
     * Indicates whether an Impenduit Field has finished forming. When true, enables Impenduit Fields self-destructing when unsupported.
     */
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    public static void init() {
        ImpenduitsCommon.LOGGER.info("Initialized Impenduits' Block State Properties!");
    }
}
