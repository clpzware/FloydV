package femcum.modernfloyd.clients.event.impl.inventory;

import femcum.modernfloyd.clients.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SyncCurrentItemEvent implements Event {
    private int slot;
}