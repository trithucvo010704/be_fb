package vn.ezisolutions.cloud.facebook_service.core;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AuthorizedUser {

    protected String id;
    protected String name;
    protected String username;
    protected List<String> roles;

    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roles != null && !roles.isEmpty()) {
            return roles.stream().map(SimpleGrantedAuthority::new).toList();
        }
        return null;
    }

}
