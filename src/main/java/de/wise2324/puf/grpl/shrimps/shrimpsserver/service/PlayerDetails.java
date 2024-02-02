package de.wise2324.puf.grpl.shrimps.shrimpsserver.service;

import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.Player;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
@Setter
public class PlayerDetails implements UserDetails {

    private final Player player;

    public PlayerDetails() {
        this(new Player());
    }

    public PlayerDetails(Player player) {
        this.player = player;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getPassword() {
        return this.player.getPassword();
    }

    @Override
    public String getUsername() {
        return this.player.getEmail();
    }

}