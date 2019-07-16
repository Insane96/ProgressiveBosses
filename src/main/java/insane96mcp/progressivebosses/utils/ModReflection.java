package insane96mcp.progressivebosses.utils;

public class ModReflection {

    public static Class<? super Object> getClass(ClassLoader loader, String className) {
        try {
            return (Class<? super Object>) Class.forName(className, false, loader);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
