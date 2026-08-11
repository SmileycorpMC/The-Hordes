package net.smileycorp.hordes.hordeevent.data;

import com.google.common.collect.Maps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.HordeSpawnData;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;

import java.util.Map;

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

    public Level getWorld() {
        return event.getEntityWorld();
    }

    public HordeEvent getHorde() {
        return event.getHorde();
    }

    public int getDay() {
        return event.getDay();
    }

    public RandomSource getRandom() {
        return event.getRandom();
    }

    public ServerPlayer getPlayer() {
        return event.getPlayer();
    }

    public LivingEntity getEntity() {
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

    public <U extends Comparable<U>> void setValue(String key, U value) {
        variables.put(key, value);
    }

    public <U extends Comparable<U>> U getValue(String key) {
        return (U) variables.get(key);
    }

    public <U extends Comparable<U>> void setGlobal(String key, U value) {
        event.getSpawnData().setGlobal(key, value);
    }

    public <U extends Comparable<U>> U getGlobal(String key, DataType<U> type) {
        return event.getSpawnData().getGlobal(key, type);
    }

    private enum State {
        ACTIVE,
        BROKEN,
        RETURNED;
    }

}
