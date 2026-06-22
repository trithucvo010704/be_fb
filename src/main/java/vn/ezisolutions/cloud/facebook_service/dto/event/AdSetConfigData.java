package vn.ezisolutions.cloud.facebook_service.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdSetConfigData {

    @JsonProperty("optimization_goal")
    private String optimizationGoal;

    @JsonProperty("billing_event")
    private String billingEvent;

    @JsonProperty("bid_strategy")
    private String bidStrategy;

    @JsonProperty("bid_amount")
    private Long bidAmount;
}
