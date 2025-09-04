package net.myriantics.impenduits.registry.block;

import net.minecraft.state.property.BooleanProperty;
import net.myriantics.impenduits.ImpenduitsCommon;

public abstract class ImpenduitsBlockStateProperties {

    /**
     * Indicates whether an Impenduit Pylon has a power source or not.
     */
    public static final BooleanProperty POWER_SOURCE_PRESENT = BooleanProperty.of("power_source_present");
    /**
     * Indicates that an Impenduit Pylon is actively supporting an Impenduit Field.
     */
    public static final BooleanProperty POWERED = BooleanProperty.of("powered");
    /**
     * Indicates whether an Impenduit Field has finished forming. When true, enables Impenduit Fields self-destructing when unsupported.
     */
    public static final BooleanProperty FORMED = BooleanProperty.of("formed");

    public static void init() {
        ImpenduitsCommon.LOGGER.info("Initialized Impenduits' Block State Properties!");
    }
}
