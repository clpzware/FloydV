package femcum.modernfloyd.clients.script.api.wrapper;

import femcum.modernfloyd.clients.script.api.API;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class ScriptWrapper<T> extends API {
    protected T wrapped;
}
