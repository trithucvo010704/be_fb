package vn.ezisolutions.cloud.facebook_service.core.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class KafkaSystemEvent<T> {
    protected String name;
    protected T payload;

    public KafkaSystemEvent() {
    }
}
