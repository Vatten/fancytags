package dev.vatten.baserad;

import dev.vatten.baserad.commands.TagsCommand;
import dev.vatten.baserad.configs.PluginConfig;
import dev.vatten.baserad.configs.TagsConfig;
import dev.vatten.baserad.events.PlayerLoadInEvent;
import dev.vatten.baserad.interfaces.Interfaces;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;

import java.nio.file.Path;
import java.util.function.Consumer;

public class Plugin extends VattenPlugin {
    public static MiniMessage MINIMESSAGE = MiniMessage.miniMessage();
    private ConfigInstance<PluginConfig> PLUGIN_CONFIG;
    private ConfigInstance<TagsConfig> TAGS_CONFIG;
    @Getter
    private TagStore tagStore;
    @Getter
    private TagStore internalTagStore;
    @Getter
    private TextFormatter pluginTextFormatter;
    @Getter
    private TagGenerator tagGenerator;
    @Getter
    private Interfaces interfaces;
    private ScheduledTask<?> timer;

    protected Plugin(VattenPlatform<?, ?> pluginInterface, Type type, Path path) {
        super(pluginInterface, type, path);
    }

    @Override
    protected void onEnable() {
        super.onEnable();

        this.api = new API();

        PLUGIN_CONFIG = new ConfigInstance<>(this, "config", PluginConfig.class);
        TAGS_CONFIG = new ConfigInstance<>(this, "tags", TagsConfig.class);

        registerCommands(
                new TagsCommand(this, getPath().resolve("import"))
        );

        getEventHandler().registerEventHandler(PlayerLoadInEvent.class, this::onPlayerLoadIn);
    }

    @Override
    protected void reload() {
        super.reload();

        if(!path.toFile().isDirectory()) {
            path.toFile().mkdirs();
        }
        PLUGIN_CONFIG.load();
        TAGS_CONFIG.load();
        internalTagStore = new InternalTagStore(this);
        pluginTextFormatter = new TextFormatter(internalTagStore.getTag("fancytags_logo").asComponent().appendSpace(), Style.empty());
        tagStore = new TagStore(this, getTagsConfig().getTags());
        tagGenerator = new TagGenerator(this);
        interfaces = new Interfaces(this);

        if(timer != null) timer.cancel();
        if(getPluginConfig().getCacheSettings().isEnabled() && getPluginConfig().getCacheSettings().isOnTimer()) timer = pluginInterface.scheduleRepeatingTask(() -> broadcast(this::refreshTags), 0, PLUGIN_CONFIG.getData().getCacheSettings().getIntervalDuration()*1000);
    }

    private void onPlayerLoadIn(PlayerLoadInEvent event) {
        if(getPluginConfig().getCacheSettings().isEnabled() && getPluginConfig().getCacheSettings().isOnJoin()) refreshTags(event.getPlayer());
    }

    private void refreshTags(VattenPlayer tagsPlayer) {
        tagsPlayer.showTitle(Title.title(Component.text("  ".repeat((int) Math.ceil((getTagStore().getTagAtlasLength() + getInternalTagStore().getTagAtlasLength()) * getPluginConfig().getCacheSettings().getOffScreenRatio()))).append(getInternalTagStore().getTagAtlas().append(getTagStore().getTagAtlas())), Component.empty(), 0, 40, 0));
    }

    public void saveTags() {
        TAGS_CONFIG.getData().setTags(getTagStore().getTags());
        TAGS_CONFIG.save();
    }

    private void broadcast(Consumer<VattenPlayer> consumer) {
        for(VattenPlayer player : players.values()) {
            consumer.accept(player);
        }
    }

    PluginConfig getPluginConfig() {
        return PLUGIN_CONFIG.getData();
    }

    TagsConfig getTagsConfig() {
        return TAGS_CONFIG.getData();
    }

    public class API extends VattenPlugin.API {

    }
}