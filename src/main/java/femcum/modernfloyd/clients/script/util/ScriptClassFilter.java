package femcum.modernfloyd.clients.script.util;

import org.openjdk.nashorn.api.scripting.ClassFilter;

public final class ScriptClassFilter implements ClassFilter {

    @Override
    public boolean exposeToScripts(final String className) {
        return className.startsWith("femcum.modernfloyd.clients.script.api");
    }
}
