package ethereumjava.solidity.types;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ethereumjava.exception.EthereumJavaException;
import ethereumjava.solidity.SolidityUtils;

/**
 * Created by gunicolas on 08/08/16.
 */
public abstract class SType<T> {

    public static int ENCODED_SIZE = 64;
    T value;

    public SType(T value) {
        this.value = value;
    }

    public static int staticPartLength(String name) {
        return -1;
    }

    public static List<String> nestedTypes(String name) {
        ArrayList<String> matches = new ArrayList<>();
        Matcher m = Pattern.compile("(\\[[0-9]*\\])").matcher(name);
        while (m.find()) matches.add(m.group());
        return matches;
    }

    public static boolean isDynamicArray(String name) {
        List<String> nestedTypes = nestedTypes(name);
        if (nestedTypes == null || nestedTypes.size() == 0) return false;
        String last = nestedTypes.get(nestedTypes.size() - 1);
        return !Pattern.compile("\\[[0-9]+\\]").matcher(last).matches();
    }

    public static boolean isStaticArray(String name) {
        List<String> nestedTypes = nestedTypes(name);
        if (nestedTypes == null || nestedTypes.size() == 0) return false;
        return !isDynamicArray(name);
    }

    public static int staticArrayLength(String name) {
        List<String> nestedTypes = nestedTypes(name);
        if (nestedTypes == null || !isStaticArray(name)) return 1;
        String last = nestedTypes.get(nestedTypes.size() - 1);
        return Integer.parseInt(last.substring(1, last.length() - 1));
    }

    public static String nestedName(String name) {
        List<String> nestedTypes = nestedTypes(name);
        if (nestedTypes == null || nestedTypes.size() == 0) return name;
        String last = nestedTypes.get(nestedTypes.size() - 1);
        return name.substring(0, name.length() - last.length());
    }

    public static boolean isDynamic(Object object) {
        if (!isSType(object))
            throw new EthereumJavaException("Type Error. Expected SType, got " + object.getClass().getSimpleName());
        String typeName = SolidityUtils.extractSolidityTypeName(object);
        return SType.isDynamicArray(typeName) || ((SType) object).isDynamicType();
    }

    public static boolean isSType(Object o) {
        Class clazz = o.getClass();
        if (clazz.isArray()) {
            clazz = clazz.getComponentType();
        }
        return clazz.isAssignableFrom(SType.class);
    }

    public abstract boolean isDynamicType();

    public final T get() {
        return value;
    }

    public abstract String asString();

    public static Class<? extends SType> getClazz(){
        return SType.class;
    }

    @Override
    public boolean equals(Object o) {
        if( !(o instanceof SType) ) return false;
        SType toCompare = (SType) o;
        return get().equals(toCompare.get());
    }
}
