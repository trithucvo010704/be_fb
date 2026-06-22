package vn.ezisolutions.cloud.facebook_service.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FacebookMessengerProfileResponse(
        String id,

        @JsonProperty("first_name")
        String firstName,

        @JsonProperty("last_name")
        String lastName,

        String name,

        @JsonProperty("profile_pic")
        String profilePic
) {
    public String displayName() {
        if (name != null && !name.isBlank()) {
            return name;
        }
        String first = firstName == null ? "" : firstName.trim();
        String last = lastName == null ? "" : lastName.trim();
        String fullName = (first + " " + last).trim();
        return fullName.isBlank() ? null : fullName;
    }
}

