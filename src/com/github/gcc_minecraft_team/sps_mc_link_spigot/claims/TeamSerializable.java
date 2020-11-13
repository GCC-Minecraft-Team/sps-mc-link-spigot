package com.github.gcc_minecraft_team.sps_mc_link_spigot.claims;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.Set;
import java.util.UUID;

public class TeamSerializable {

    @BsonProperty(value = "name")
    public String name;
    @BsonProperty(value = "members")
    public Set<UUID> members;
    @BsonProperty(value = "leader")
    public UUID leader;

    @BsonProperty(value = "WGID")
    public UUID WGID;

    // MongoDB POJO constructor
    @BsonCreator
    public TeamSerializable(
            @BsonProperty("name") final String name,
            @BsonProperty("members") final Set<UUID> members,
            @BsonProperty("leader") final UUID leader,
            @BsonProperty(value = "WGID") final UUID WGID
    ) {
        this.name = name;
        this.members = members;
        this.leader = leader;
        this.WGID = WGID;
    }

    public TeamSerializable(Team team) {
        this.name = team.name;
        this.members = team.members;
        this.leader = team.leader;
        this.WGID = team.getWorldGroup().getID();
    }

}
