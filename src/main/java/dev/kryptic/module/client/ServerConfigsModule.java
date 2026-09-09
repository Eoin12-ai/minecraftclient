package dev.kryptic.module.client;

import dev.kryptic.KrypticClient;
import dev.kryptic.config.ConfigStore;
import dev.kryptic.config.ServerConfigs;
import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.SliderSetting;

/**
 * Ties config slots to servers.
 *
 * Bind the slot you are using to the server you are on, and every future join
 * to that address restores it. Useful when one address wants ESP and scanning
 * turned up and another wants the client quiet.
 *
 * The two Bind/Unbind switches behave as buttons: flipping one performs the
 * action and flips itself back off, so the ClickGUI needs no new widget type
 * for a one-shot action.
 */
public class ServerConfigsModule extends Module {

    public final BooleanSetting autoApply = this.addSetting(new BooleanSetting(
            "Auto Apply", "Switch to the bound config when joining a server", true));

    public final SliderSetting slot = this.addSetting(new SliderSetting(
            "Slot", "Which config slot Bind uses", 1.0, 1.0, ConfigStore.SLOT_COUNT, 1.0));

    public final BooleanSetting bind = this.addSetting(new BooleanSetting(
            "Bind Server", "Bind the server you are on to the slot above", false));

    public final BooleanSetting unbind = this.addSetting(new BooleanSetting(
            "Unbind Server", "Remove the binding for the server you are on", false));

    public final BooleanSetting announce = this.addSetting(new BooleanSetting(
            "Announce", "Notify when a config is switched on join", true));

    public ServerConfigsModule() {
        super("ServerConfigs", "Apply a saved config automatically per server", Category.CLIENT);
        this.setEnabled(true);
    }

    private ServerConfigs configs() {
        return KrypticClient.serverConfigs();
    }

    @Override
    public void onTick() {
        ServerConfigs configs = configs();
        if (configs == null) return;

        if (bind.get()) {
            bind.set(Boolean.FALSE);
            String key = ServerConfigs.currentKey();
            int index = (int) Math.round(slot.getFloat()) - 1;
            configs.bind(key, index);
            KrypticClient.LOGGER.info("Bound {} to config slot {}", key, index + 1);
            notify("Bound " + key + " to slot " + (index + 1));
        }

        if (unbind.get()) {
            unbind.set(Boolean.FALSE);
            String key = ServerConfigs.currentKey();
            configs.unbind(key);
            KrypticClient.LOGGER.info("Unbound {}", key);
            notify("Unbound " + key);
        }
    }

    /** Called from the join event once the server entry is available. */
    public void onJoinedServer() {
        ServerConfigs configs = configs();
        if (configs == null || !isEnabled() || !autoApply.get()) return;
        if (configs.applyForCurrentServer() && announce.get()) {
            ConfigStore store = KrypticClient.configStore();
            int active = store == null ? -1 : store.activeIndex();
            String name = (store != null && active >= 0 && store.slot(active) != null)
                    ? store.slot(active).name()
                    : "config";
            notify("Loaded " + name);
        }
    }

    private void notify(String message) {
        try {
            if (KrypticClient.notifications() != null) {
                KrypticClient.notifications().pushInfo(message);
            }
        } catch (Throwable ignored) {
            // a notification failing must never break a join
        }
    }
}
