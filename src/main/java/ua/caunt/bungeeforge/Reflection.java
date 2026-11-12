package ua.caunt.bungeeforge;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Finds Minecraft internals by type instead of by name, so the same code works whatever the names are in a
 * given version. Every lookup insists on exactly one match and throws otherwise, so a version that adds a
 * second field of the same type fails loudly instead of picking the wrong one.
 */
final class Reflection {
    static final String GAME_PROFILE = "com.mojang.authlib.GameProfile";

    private Reflection() {
    }

    /** The only instance field of {@code owner} (or its superclasses) declared with exactly this type; null if none. */
    static Field field(Class<?> owner, String typeName) {
        Field found = null;
        for (Class<?> type = owner; type != null && !isLibrary(type); type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || !field.getType().getName().equals(typeName))
                    continue;
                if (found != null)
                    throw new IllegalStateException(owner.getName() + " has more than one " + typeName + " field");
                found = field;
            }
        }
        if (found != null)
            found.setAccessible(true);
        return found;
    }

    static Field field(Class<?> owner, Class<?> type) {
        return field(owner, type.getName());
    }

    static Field requireField(Class<?> owner, String typeName) {
        Field field = field(owner, typeName);
        if (field == null)
            throw new IllegalStateException(owner.getName() + " has no " + typeName + " field");
        return field;
    }

    static Field requireField(Class<?> owner, Class<?> type) {
        return requireField(owner, type.getName());
    }

    /** Values of all non-null, non-primitive instance fields declared by Minecraft (not JDK or library) classes. */
    static List<Object> values(Object owner) throws IllegalAccessException {
        List<Object> values = new ArrayList<Object>();
        for (Class<?> type = owner.getClass(); type != null && !isLibrary(type); type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || field.getType().isPrimitive())
                    continue;
                Object value;
                try {
                    field.setAccessible(true);
                    value = field.get(owner);
                } catch (RuntimeException closedModule) { // e.g. a mod jar with a module-info that opens nothing
                    continue;
                }
                if (value != null)
                    values.add(value);
            }
        }
        return values;
    }

    /** JDK and bundled-library classes: never Minecraft's own state, and not open to reflection on Java 9+. */
    static boolean isLibrary(Class<?> type) {
        String name = type.getName();
        return name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("jdk.") || name.startsWith("sun.")
                || name.startsWith("io.netty.") || name.startsWith("com.google.") || name.startsWith("org.apache.")
                || name.startsWith("com.mojang.");
    }

    /** Calls the first public no-argument method that exists, e.g. {@code getName} (authlib < 7) or {@code name}. */
    static Object call(Object target, String... names) throws ReflectiveOperationException {
        for (String name : names) {
            try {
                return target.getClass().getMethod(name).invoke(target);
            } catch (NoSuchMethodException ignored) {
            }
        }
        throw new NoSuchMethodException(target.getClass().getName() + " has none of the expected methods");
    }

    static UUID profileId(Object profile) throws ReflectiveOperationException {
        return (UUID) call(profile, "getId", "id");
    }

    /**
     * A new GameProfile with the forwarded id and properties and the name from {@code template}. Works with every
     * authlib Minecraft has shipped: a mutable class up to authlib 6, a record with an immutable PropertyMap since 7.
     */
    static Object newProfile(Object template, Forwarding forwarding) throws ReflectiveOperationException {
        return newProfile(template.getClass(), (String) call(template, "getName", "name"), forwarding);
    }

    static Object newProfile(Class<?> profileClass, String name, Forwarding forwarding) throws ReflectiveOperationException {
        ClassLoader loader = profileClass.getClassLoader();

        Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property", true, loader);
        Constructor<?> propertyConstructor = propertyClass.getConstructor(String.class, String.class, String.class);
        List<Object[]> properties = new ArrayList<Object[]>();
        for (Forwarding.Property property : forwarding.properties)
            properties.add(new Object[]{property.name, propertyConstructor.newInstance(property.name, property.value, property.signature)});

        Class<?> mapClass = Class.forName("com.mojang.authlib.properties.PropertyMap", true, loader);
        // Through the public interface: Guava's implementations inherit put() from package-private classes.
        Method put = Class.forName("com.google.common.collect.Multimap", true, loader).getMethod("put", Object.class, Object.class);
        Constructor<?> recordConstructor = null;
        try {
            recordConstructor = profileClass.getConstructor(UUID.class, String.class, mapClass);
        } catch (NoSuchMethodException ignored) {
        }

        if (recordConstructor == null) {
            Object profile = profileClass.getConstructor(UUID.class, String.class).newInstance(forwarding.id, name);
            Object map = call(profile, "getProperties");
            for (Object[] property : properties)
                put.invoke(map, property[0], property[1]);
            return profile;
        }

        Object multimap = Class.forName("com.google.common.collect.LinkedHashMultimap", true, loader).getMethod("create").invoke(null);
        for (Object[] property : properties)
            put.invoke(multimap, property[0], property[1]);
        for (Constructor<?> constructor : mapClass.getConstructors()) {
            Class<?>[] parameters = constructor.getParameterTypes();
            if (parameters.length == 1 && parameters[0].isInstance(multimap))
                return recordConstructor.newInstance(forwarding.id, name, constructor.newInstance(multimap));
        }
        throw new NoSuchMethodException(mapClass.getName() + " has no Multimap constructor");
    }
}
