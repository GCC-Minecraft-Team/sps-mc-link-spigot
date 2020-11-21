package com.github.gcc_minecraft_team.sps_mc_link_spigot.claims;

import be.seeseemelk.mockbukkit.WorldMock;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import org.bukkit.Material;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorldGroupTest {

    WorldGroup wg;

    /**
     * Tests to make sure a {@link WorldGroup} can be created using the name constructor
     */
    @BeforeEach
    @Test
    void testWorldGroupFromName() {
        this.wg = new WorldGroup("TestGroup");
        assertNotNull(this.wg);
    }

    /**
     * Tests to make sure a {@link WorldGroup} can be created from a {@link WorldGroupSerializable}
     */
    @BeforeEach
    @Test
    void testWorldGroupFromSerial() {
        WorldGroupSerializable wgs = new WorldGroupSerializable(new WorldGroup("TestGroup"));
        wg = new WorldGroup(wgs);
        assertNotNull(wg);
    }

    /**
     * Tests to make sure the created {@link WorldGroup} contains the correct name
     */
    @Test
    void testGetName() {
        assertEquals("TestGroup", wg.getName());
    }
}