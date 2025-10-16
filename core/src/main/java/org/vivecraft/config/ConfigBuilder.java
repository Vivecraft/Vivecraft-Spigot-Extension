package org.vivecraft.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vivecraft.ViveMain;
import org.vivecraft.VivePlayer;
import org.vivecraft.network.NetworkHandler;
import org.vivecraft.network.packet.s2c.VivecraftPayloadS2C;
import org.vivecraft.util.Utils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class ConfigBuilder {

    private FileConfiguration config;
    private final Deque<String> stack = new ArrayDeque<>();
    private final List<ConfigValue> configValues = new ArrayList<>();
    private final Map<String, ConfigValue> map = new HashMap<>();

    public ConfigBuilder(FileConfiguration config) {
        this.config = config;
    }

    public int setNewConfigFile(FileConfiguration newConfig, boolean withUpdate, @NotNull Consumer<String> notifier) {
        this.config = newConfig;
        int changes = 0;
        for (ConfigValue configValue : this.configValues) {
            Object oldValue = configValue.get();
            configValue.reload();
            Object newValue = configValue.get();
            if (checktSingle(configValue, notifier)) {
                changes++;
                configValue.reset(notifier);
                continue;
            }
            if (!Objects.equals(oldValue, newValue)) {
                changes++;
                notifier.accept(
                    ViveMain.translate("vivecraft command.reload.changed",
                        configValue.path, Utils.green(oldValue), Utils.green(newValue)));
                if (withUpdate) {
                    configValue.set(newValue, notifier);
                }
            }
        }
        return changes;
    }

    protected FileConfiguration getConfig() {
        return this.config;
    }

    @Nullable
    public ConfigValue getConfigValue(String key) {
        return this.map.get(key);
    }

    private void registerConfigValue(String key, ConfigValue configValue) {
        if (this.map.containsKey(key)) {
            throw new IllegalStateException("Duplicate config key: " + key);
        }
        this.map.put(key, configValue);
        this.configValues.add(configValue);
    }

    /**
     * pushes the given subPath to the path
     *
     * @param subPath new sub path
     * @return this builder, for chaining commands
     */
    public ConfigBuilder push(String subPath) {
        this.stack.add(subPath);
        return this;
    }

    /**
     * pops the last sub path
     *
     * @return this builder, for chaining commands
     */
    public ConfigBuilder pop() {
        this.stack.removeLast();
        return this;
    }

    /**
     * corrects the attached config, with the built spec
     *
     * @param listener listener to send correction to, boolean is if the correction should only be logged in debug mode
     */
    public void correct(@NotNull Consumer<String> listener) {
        for (ConfigValue<?> configValue : this.configValues) {
            if (checktSingle(configValue, listener)) {
                // we don't want to log unset values
                configValue.reset(configValue.getRaw() == null ? null : listener);
            }
        }
    }

    /**
     * checks if a config value needs to be reset
     *
     * @param configValue config value to check
     * @param listener    listener to forward errors to
     * @return if the config needs a reset
     */
    protected boolean checktSingle(ConfigValue configValue, @NotNull Consumer<String> listener) {
        if (configValue.getRaw() == null) {
            return true;
        } else if (configValue instanceof NumberValue) {
            if (!(configValue.getRaw() instanceof Number)) {
                listener.accept(ViveMain.translate("vivecraft.command.invalid", Utils.red(configValue.getRaw()),
                    configValue.getPath()));
                return true;
            }
            NumberValue<?> numberValue = (NumberValue<?>) configValue;
            Number val = numberValue.get();
            if (val.doubleValue() < numberValue.getMin().doubleValue() ||
                val.doubleValue() > numberValue.getMax().doubleValue())
            {
                listener.accept(ViveMain.translate("vivecraft.command.number.outOfRange", Utils.red(val),
                    Utils.green(numberValue.getMin()), Utils.green(numberValue.getMax()), numberValue.getPath()));
                return true;
            }
        } else if (configValue instanceof InListValue<?>) {
            InListValue<?> listValue = (InListValue<?>) configValue;
            if (!listValue.getValidValues().contains(listValue.get())) {
                listener.accept(
                    ViveMain.translate("vivecraft.command.invalid", Utils.red(listValue.get()), listValue.getPath()));
                return true;
            }
        }
        return false;
    }

    public List<ConfigValue> getConfigValues() {
        return this.configValues;
    }

    // general Settings

    /**
     * defines a setting with the current path, and pops the last path segment
     *
     * @param defaultValue default value this setting should have
     * @return ConfigValue that accesses the setting at the path when calling this method
     */
    public <T> ConfigValue<T> define(T defaultValue) {
        String path = String.join(".", this.stack);
        this.config.addDefault(path, defaultValue);
        this.stack.removeLast();

        ConfigValue<T> value = new ConfigValue<>(this, path, defaultValue);
        registerConfigValue(path, value);
        return value;
    }

    /**
     * defines a setting with the current path, and pops the last path segment
     *
     * @param defaultValue default value this setting should have
     * @param min          the minimum value, that  is valid for this setting
     * @param max          the maximum value, that  is valid for this setting
     * @return ConfigValue that accesses the setting at the path when calling this method
     */
    public <T extends Comparable<? super T>> ConfigValue<T> defineInRange(T defaultValue, T min, T max) {
        String path = String.join(".", this.stack);
        this.stack.removeLast();

        ConfigValue<T> value = new ConfigValue<>(this, path, defaultValue);
        registerConfigValue(path, value);
        return value;
    }

    /**
     * defines a setting with the current path, and pops the last path segment
     *
     * @param defaultValue default value this setting should have
     * @return ConfigValue that accesses the setting at the path when calling this method
     */
    public <T> ListValue<T> defineList(List<T> defaultValue) {
        String path = String.join(".", this.stack);
        this.stack.removeLast();

        ListValue<T> value = new ListValue<>(this, path, defaultValue);
        registerConfigValue(path, value);
        return value;
    }

    /**
     * defines a setting with the current path, and pops the last path segment
     *
     * @param defaultValue default value this setting should have
     * @return ConfigValue that accesses the setting at the path when calling this method
     */
    public StringListValue defineStringList(List<String> defaultValue) {
        String path = String.join(".", this.stack);
        this.stack.removeLast();

        StringListValue value = new StringListValue(this, path, defaultValue);
        registerConfigValue(path, value);
        return value;
    }

    /**
     * defines a setting with the current path, and pops the last path segment
     *
     * @param defaultValue default value this setting should have
     * @param validValues  Collection of values that are accepted
     * @return ConfigValue that accesses the setting at the path when calling this method
     */
    public <T> InListValue<T> defineInList(T defaultValue, Collection<? extends T> validValues) {
        String path = String.join(".", this.stack);
        this.stack.removeLast();

        InListValue<T> value = new InListValue<>(this, path, defaultValue, validValues);
        registerConfigValue(path, value);
        return value;
    }

    /**
     * defines a setting with the current path, and pops the last path segment
     *
     * @param defaultValue default value this setting should have
     * @return ConfigValue that accesses the setting at the path when calling this method
     */
    public <T extends Enum<T>> EnumValue<T> defineEnum(T defaultValue, Class<T> enumClass) {
        String path = String.join(".", this.stack);

        EnumValue<T> value = new EnumValue<>(this, path, defaultValue, enumClass);
        this.stack.removeLast();

        registerConfigValue(path, value);
        return value;
    }

    /**
     * same as {@link #define define(T defaultValue)} but returns a {@link BooleanValue}
     */
    public BooleanValue define(boolean defaultValue) {
        String path = String.join(".", this.stack);
        this.stack.removeLast();

        BooleanValue value = new BooleanValue(this, path, defaultValue);
        registerConfigValue(path, value);
        return value;
    }

    /**
     * same as {@link #define define(T defaultValue)} but returns a {@link StringValue}
     */
    public StringValue define(String defaultValue) {
        String path = String.join(".", this.stack);
        this.stack.removeLast();

        StringValue value = new StringValue(this, path, defaultValue);
        registerConfigValue(path, value);
        return value;
    }

    /**
     * same as {@link #defineInRange defineInRange(T defaultValue, T min, T max)} but returns a {@link DoubleValue}
     */
    public DoubleValue defineInRange(double defaultValue, double min, double max) {
        String path = String.join(".", this.stack);
        this.stack.removeLast();

        DoubleValue value = new DoubleValue(this, path, defaultValue, min, max);
        registerConfigValue(path, value);
        return value;
    }

    /**
     * same as {@link #defineInRange defineInRange(T defaultValue, T min, T max)} but returns a {@link DoubleValue}
     */
    public IntValue defineInRange(int defaultValue, int min, int max) {
        String path = String.join(".", this.stack);
        this.stack.removeLast();

        IntValue value = new IntValue(this, path, defaultValue, min, max);
        registerConfigValue(path, value);
        return value;
    }


    public static class ConfigValue<T> {

        // the config, this setting is part of
        protected final ConfigBuilder config;
        protected final String path;
        protected final T defaultValue;
        // cache the value to minimize config lookups
        protected T cachedValue = null;
        protected boolean needsReload;
        // the client has some different default values
        protected T clientDefaultValue;

        /**
         * Function that takes the setting value and a VivePlayer and creates a network packet for them
         */
        private BiFunction<T, VivePlayer, VivecraftPayloadS2C> packetFunction = null;
        /**
         * Consumer that takes the old and new value to send updates
         */
        private BiConsumer<T, T> updateConsumer = null;

        public ConfigValue(ConfigBuilder config, String path, T defaultValue) {
            this.config = config;
            this.path = path;
            this.defaultValue = defaultValue;
        }

        public T get() {
            if (this.cachedValue == null) {
                this.cachedValue = getFromConfig();
            }
            return this.cachedValue;
        }

        protected T getFromConfig() {
            return (T) this.config.getConfig().get(this.path);
        }

        protected T getRaw() {
            return (T) this.config.getConfig().get(this.path);
        }

        public void set(T newValue, @Nullable Consumer<String> notifier) {
            T oldValue = this.getRaw();
            this.cachedValue = newValue;
            this.config.getConfig().set(this.path, newValue);
            if (oldValue != null) {
                // we don't want to call update, if we first create the config
                this.onUpdate(oldValue, newValue, notifier);
            }
        }

        public T reset(@Nullable Consumer<String> notifier) {
            this.set(this.defaultValue, notifier);
            if (notifier != null) {
                notifier.accept(
                    ViveMain.translate("vivecraft.command.configReset", this.path, Utils.green(this.defaultValue)));
            }
            return this.defaultValue;
        }

        public void reload() {
            this.cachedValue = null;
            this.get();
        }

        protected T getDefaultValue() {
            return this.defaultValue;
        }

        public boolean isDefault() {
            return Objects.equals(get(), getDefaultValue());
        }

        public String getPath() {
            return this.path;
        }

        @SuppressWarnings("unchecked")
        public <V extends ConfigValue<T>> V setOnUpdate(BiConsumer<T, T> onUpdate) {
            this.updateConsumer = onUpdate;
            return (V) this;
        }

        public void onUpdate(T oldValue, T newValue, @Nullable Consumer<String> notifier) {
            if (this.updateConsumer != null) {
                this.updateConsumer.accept(oldValue, newValue);
            }
            NetworkHandler.sendUpdatePacketToAll(this, notifier);
        }

        @SuppressWarnings("unchecked")
        public <V extends ConfigValue<T>> V setPacketFunction(
            BiFunction<T, VivePlayer, VivecraftPayloadS2C> supplier)
        {
            this.packetFunction = supplier;
            return (V) this;
        }

        @Nullable
        public BiFunction<T, VivePlayer, VivecraftPayloadS2C> getPacketFunction() {
            return this.packetFunction;
        }

        @SuppressWarnings("unchecked")
        public <V extends ConfigValue<T>> V setNeedsReload(boolean needsReload) {
            this.needsReload = needsReload;
            return (V) this;
        }

        public boolean needsReload() {
            return this.needsReload;
        }

        @SuppressWarnings("unchecked")
        public <V extends ConfigValue<T>> V setClientDefault(T clientDefaultValue) {
            this.clientDefaultValue = clientDefaultValue;
            return (V) this;
        }

        public boolean isClientDefault() {
            return this.clientDefaultValue != null ? Objects.equals(get(), this.clientDefaultValue) : isDefault();
        }
    }

    public static class BooleanValue extends ConfigValue<Boolean> {
        public BooleanValue(ConfigBuilder config, String path, boolean defaultValue) {
            super(config, path, defaultValue);
        }

        @Override
        protected Boolean getFromConfig() {
            return this.config.getConfig().getBoolean(this.path);
        }
    }

    public static class StringValue extends ConfigValue<String> {
        public StringValue(ConfigBuilder config, String path, String defaultValue) {
            super(config, path, defaultValue);
        }

        @Override
        protected String getFromConfig() {
            return this.config.getConfig().getString(this.path);
        }
    }

    public static class ListValue<T> extends ConfigValue<List<T>> {
        public ListValue(ConfigBuilder config, String path, List<T> defaultValue) {
            super(config, path, defaultValue);
        }

        @Override
        protected List<T> getFromConfig() {
            return (List<T>) this.config.getConfig().getList(this.path);
        }
    }

    public static class StringListValue extends ListValue<String> {
        public StringListValue(ConfigBuilder config, String path, List<String> defaultValue) {
            super(config, path, defaultValue);
        }

        @Override
        protected List<String> getFromConfig() {
            return this.config.getConfig().getStringList(this.path);
        }
    }

    public static class InListValue<T> extends ConfigValue<T> {
        private final Collection<? extends T> validValues;

        public InListValue(ConfigBuilder config, String path, T defaultValue, Collection<? extends T> validValues)
        {
            super(config, path, defaultValue);
            this.validValues = validValues;
        }

        public Collection<? extends T> getValidValues() {
            return this.validValues;
        }
    }

    public static class EnumValue<T extends Enum<T>> extends ConfigValue<T> {
        private final Class<T> enumClass;

        public EnumValue(ConfigBuilder config, String path, T defaultValue, Class<T> enumClass) {
            super(config, path, defaultValue);
            this.enumClass = enumClass;
        }

        @Override
        public T get() {
            if (this.cachedValue == null) {
                this.cachedValue = this.getEnumValue(this.getFromConfig());
            }
            return this.cachedValue;
        }

        @Override
        public void set(T newValue, Consumer<String> notifier) {
            T oldValue = this.getRaw();
            this.cachedValue = newValue;
            this.config.getConfig().set(this.path, newValue.name());
            if (oldValue != null) {
                this.onUpdate(oldValue, newValue, notifier);
            }
        }

        // enums are stored as a string, so it can't be cast to enum directly
        @Override
        protected T getRaw() {
            return getEnumValue(this.config.getConfig().get(this.path));
        }

        public T getEnumValue(Object value) {
            if (value != null) {
                final Class<?> cls = value.getClass();
                if (this.enumClass.isAssignableFrom(cls)) {
                    return (T) value;
                } else if (cls == String.class) {
                    final String name = (String) value;
                    for (T item : this.enumClass.getEnumConstants()) {
                        if (item.name().equalsIgnoreCase(name)) {
                            return item;
                        }
                    }
                    ViveMain.LOGGER.severe("No enum constant " + this.enumClass.getCanonicalName() + "." + name);
                } else {
                    ViveMain.LOGGER.severe("Cannot convert a value of type " + cls.getCanonicalName() + " to an Enum " +
                        this.enumClass.getCanonicalName());
                }
            }
            return null;
        }

        public Collection<? extends T> getValidValues() {
            return EnumSet.allOf(this.enumClass);
        }
    }

    public static abstract class NumberValue<E extends Number> extends ConfigValue<E> {

        private final E min;
        private final E max;

        public NumberValue(ConfigBuilder config, String path, E defaultValue, E min, E max) {
            super(config, path, defaultValue);
            this.min = min;
            this.max = max;
        }

        public E getMin() {
            return this.min;
        }

        public E getMax() {
            return this.max;
        }
    }

    public static class IntValue extends NumberValue<Integer> {

        public IntValue(ConfigBuilder config, String path, int defaultValue, int min, int max) {
            super(config, path, defaultValue, min, max);
        }

        @Override
        protected Integer getFromConfig() {
            return this.config.getConfig().getInt(this.path);
        }
    }

    public static class DoubleValue extends NumberValue<Double> {

        public DoubleValue(ConfigBuilder config, String path, double defaultValue, double min, double max) {
            super(config, path, defaultValue, min, max);
        }

        @Override
        protected Double getFromConfig() {
            return this.config.getConfig().getDouble(this.path);
        }
    }
}
