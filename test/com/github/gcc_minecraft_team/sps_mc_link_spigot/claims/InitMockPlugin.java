package com.github.gcc_minecraft_team.sps_mc_link_spigot.claims;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class InitMockPlugin {

    private static ServerMock server;
    private static MockPlugin plugin;

    @BeforeAll
    public static void setUp()
    {
        server = MockBukkit.mock();
        plugin = (MockPlugin) MockBukkit.load(MockPlugin.class);
    }

    @AfterAll
    public static void tearDown()
    {
        MockBukkit.unmock();
    }

    public static ServerMock getServer() {
        return server;
    }

    public static MockPlugin getPlugin() {
        return plugin;
    }

}
