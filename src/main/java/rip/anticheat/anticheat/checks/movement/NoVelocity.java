/*
 * Decompiled with CFR 0_122.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.entity.EntityDamageEvent
 *  org.bukkit.event.entity.EntityDamageEvent$DamageCause
 *  org.bukkit.event.player.PlayerMoveEvent
 *  org.bukkit.event.player.PlayerQuitEvent
 *  org.bukkit.event.player.PlayerVelocityEvent
 *  org.bukkit.util.Vector
 */
package rip.anticheat.anticheat.checks.movement;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;
import rip.anticheat.anticheat.AntiCheat;
import rip.anticheat.anticheat.PlayerStats;
import rip.anticheat.anticheat.Violation;
import rip.anticheat.anticheat.ViolationPriority;
import rip.anticheat.anticheat.checks.Check;
import rip.anticheat.anticheat.checks.CheckType;
import rip.anticheat.anticheat.util.misc.PlayerUtil;
import rip.anticheat.anticheat.util.misc.ServerUtil;

public class NoVelocity
extends Check {
    private Map<Player, Long> lastVelocity = new HashMap<Player, Long>();
    private Map<Player, Integer> awaitingVelocity = new HashMap<Player, Integer>();
    private Map<Player, Double> totalMoved = new HashMap<Player, Double>();

    public NoVelocity(AntiCheat antiCheat) {
        super(antiCheat, CheckType.MOVEMENT, "NoVelocity", "KnockBack [Modifier]", 100, 50, 2, 0);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent playerQuitEvent) {
        Player player = playerQuitEvent.getPlayer();
        if (this.lastVelocity.containsKey((Object)player)) {
            this.lastVelocity.remove((Object)player);
        }
        if (this.awaitingVelocity.containsKey((Object)player)) {
            this.awaitingVelocity.remove((Object)player);
        }
        if (this.totalMoved.containsKey((Object)player)) {
            this.totalMoved.remove((Object)player);
        }
    }

    @EventHandler
    public void Move(PlayerMoveEvent playerMoveEvent) {
        double d;
        Player player = playerMoveEvent.getPlayer();
        PlayerStats playerStats = this.getCore().getPlayerStats(player);
        if (PlayerUtil.isOnBlock(player, 0, new Material[]{Material.WEB}) || PlayerUtil.isOnBlock(player, 1, new Material[]{Material.WEB})) {
            return;
        }
        if (PlayerUtil.isHoveringOverWater(player, 1) || PlayerUtil.isHoveringOverWater(player, 0)) {
            return;
        }
        if (player.getAllowFlight()) {
            return;
        }
        if (ServerUtil.getPing(player) > 400) {
            return;
        }
        int n = 0;
        if (this.awaitingVelocity.containsKey((Object)player)) {
            n = this.awaitingVelocity.get((Object)player);
        }
        long l = 0;
        if (this.lastVelocity.containsKey((Object)player)) {
            l = this.lastVelocity.get((Object)player);
        }
        if (player.getLastDamageCause() == null || player.getLastDamageCause().getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK && player.getLastDamageCause().getCause() != EntityDamageEvent.DamageCause.PROJECTILE) {
            n = 0;
        }
        if (System.currentTimeMillis() - l > 2000 && n > 0) {
            --n;
        }
        double d2 = 0.0;
        if (this.totalMoved.containsKey((Object)player)) {
            d2 = this.totalMoved.get((Object)player);
        }
        if ((d = playerMoveEvent.getTo().getY() - playerMoveEvent.getFrom().getY()) > 0.0) {
            d2 += d;
        }
        int n2 = playerStats.getCheck(this, 0);
        int n3 = this.getThreshold();
        if (n > 0) {
            if (d2 < 0.3) {
                n2 += 9;
            } else {
                n2 = 0;
                d2 = 0.0;
                --n;
            }
            if (PlayerUtil.isOnGround(player, -1) || PlayerUtil.isOnGround(player, -2) || PlayerUtil.isOnGround(player, -3)) {
                n2 -= 9;
            }
        }
        if (n2 > n3) {
            if (d2 == 0.0) {
                if (ServerUtil.getPing(player) > 500) {
                    return;
                }
                this.getCore().addViolation(player, this, new Violation(this, ViolationPriority.LOW, "Received no velocity"));
            } else {
                if (ServerUtil.getPing(player) > 220) {
                    return;
                }
                this.getCore().addViolation(player, this, new Violation(this, ViolationPriority.LOW, "Received less velocity than expected"));
            }
            n2 = 0;
            d2 = 0.0;
            --n;
        }
        playerStats.setCheck(this, 0, n2);
        this.awaitingVelocity.put(player, n);
        this.totalMoved.put(player, d2);
    }

    @EventHandler
    public void Velocity(PlayerVelocityEvent playerVelocityEvent) {
        double d;
        long l;
        Player player = playerVelocityEvent.getPlayer();
        if (PlayerUtil.isOnBlock(player, 0, new Material[]{Material.WEB}) || PlayerUtil.isOnBlock(player, 1, new Material[]{Material.WEB})) {
            return;
        }
        if (PlayerUtil.isHoveringOverWater(player, 1) || PlayerUtil.isHoveringOverWater(player, 0)) {
            return;
        }
        if (PlayerUtil.isOnGround(player, -1) || PlayerUtil.isOnGround(player, -2) || PlayerUtil.isOnGround(player, -3)) {
            return;
        }
        if (player.getAllowFlight()) {
            return;
        }
        if (this.lastVelocity.containsKey((Object)player) && (l = System.currentTimeMillis() - this.lastVelocity.get((Object)player)) < 500) {
            return;
        }
        Vector vector = playerVelocityEvent.getVelocity();
        double d2 = Math.abs(vector.getY());
        if (d2 > 0.0 && (d = (double)((int)(Math.pow(d2 + 2.0, 2.0) * 5.0))) > 20.0) {
            int n = 0;
            if (this.awaitingVelocity.containsKey((Object)player)) {
                n = this.awaitingVelocity.get((Object)player);
            }
            this.awaitingVelocity.put(player, ++n);
            this.lastVelocity.put(player, System.currentTimeMillis());
        }
    }
}

