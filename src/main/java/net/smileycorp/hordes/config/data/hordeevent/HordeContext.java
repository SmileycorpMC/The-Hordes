package net.smileycorp.hordes.config.data.hordeevent;

import com.google.common.collect.Maps;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.HordeSpawnData;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;

import java.util.Map;
import java.util.Random;

public class HordeContext<T extends HordePlayerEvent> {

    private final T event;
    private boolean called = false;
    private State state = State.ACTIVE;
    private final Map<String, Comparable<?>> variables = Maps.newHashMap();

    public HordeContext(T event) {
        this.event = event;
    }

    public T getEvent() {
        return event;
    }

    public World getWorld() {
        return event.getEntityWorld();
    }

    public HordeEvent getHorde() {
        return event.getHorde();
    }

    public int getDay() {
        return event.getDay();
    }

    public Random getRandom() {
        return event.getRandom();
    }

    public EntityPlayerMP getPlayer() {
        return event.getPlayer();
    }

    public EntityLivingBase getEntity() {
        return event.getEntity();
    }

    public HordeSpawnData getSpawnData() {
        return event.getSpawnData();
    }

    public Class<? extends HordePlayerEvent> getEventClass() {
        return event.getClass();
    }

    public void setCalled(boolean called) {
        this.called = called;
    }

    public boolean isCalled() {
        return called;
    }

    public void resetState() {
        if (state == State.BROKEN) state = State.ACTIVE;
    }

    public void breakScript() {
        HordesLogger.logInfo("break");
        if (state == State.ACTIVE) state = State.BROKEN;
    }

    public void returnScript() {
        state = State.RETURNED;
    }

    public boolean isBroken() {
        return state != State.ACTIVE;
    }

    public void cancelEvent() {
        if (event.isCancelable()) event.setCanceled(true);
        if (event.hasResult()) event.setResult(Event.Result.DENY);
    }

    public void setValue(String key, Comparable<?> value) {
        variables.put(key, value);
    }

    public Comparable<?> getValue(String key) {
        return variables.get(key);
    }

    private enum State {
        ACTIVE,
        BROKEN,
        RETURNED;
    }

}
