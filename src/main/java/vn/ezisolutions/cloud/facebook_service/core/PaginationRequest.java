package vn.ezisolutions.cloud.facebook_service.core;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaginationRequest implements Serializable {

    @Positive(message = "limit phải là số lớn hơn 0")
    private Integer limit;
    @Positive(message = "page phải là số lớn hơn 0")
    private Integer page;
    private String searchText;
    private String orderBy;

    public Map<String, String> getOrders() {
        Map<String, String> fields = new HashMap<>();
        if (orderBy != null && !orderBy.isEmpty()) {
            String[] splits = orderBy.split(",");
            for (String s : splits) {
                String[] f = s.split(":");
                if (f.length == 1) {
                    fields.put(f[0], "ASC");
                } else {
                    if ("DESC".equals(f[1].toUpperCase())) {
                        fields.put(f[0], "DESC");
                    } else {
                        fields.put(f[0], "ASC");
                    }
                }
            }
        }
        return fields;
    }

}
