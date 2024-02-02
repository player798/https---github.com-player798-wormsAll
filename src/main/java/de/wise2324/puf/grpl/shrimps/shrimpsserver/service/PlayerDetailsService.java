package de.wise2324.puf.grpl.shrimps.shrimpsserver.service;

import de.wise2324.puf.grpl.shrimps.shrimpsserver.entity.OnlineStatus;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.Player;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.repository.PlayerRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlayerDetailsService implements UserDetailsService {

    @Autowired
    private PlayerRepository repo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Player player;
        player = this.repo.findByEmail(username);
        if (player == null) {
            throw new UsernameNotFoundException("Kein Nutzer mit der E-Mail: " + username + " gefunden.");
        }

        return new PlayerDetails(player);
    }

    public UserDetails loadById(Long findId) throws UsernameNotFoundException {
        Player player;
        Optional<Player> foundPlayer = repo.findById(findId);

        if (foundPlayer.isPresent()) { player = foundPlayer.get(); } else {
            throw new UsernameNotFoundException("Id: " + findId + " not found.");
        }

        return new PlayerDetails(player);
    }

    public void loginUser(String username) throws UsernameNotFoundException {
        Player player;
        player = this.repo.findByEmail(username);

        if (player != null) {
            player.setStatus(OnlineStatus.Online);
            this.repo.save(player);
        } else {
            throw new UsernameNotFoundException("No User with E-Mail: " + username + " found.");
        }
    }

    public void loginUser(Player player) {
        player.setStatus(OnlineStatus.Online);
        this.repo.save(player);
    }

    public void logoutUser(String username) throws UsernameNotFoundException {
        Player player;
        player = this.repo.findByEmail(username);

        if (player != null) {
            player.setStatus(OnlineStatus.Offline);
            this.repo.save(player);
        } else {
            throw new UsernameNotFoundException("No User with E-Mail: " + username + " found.");
        }
    }

    public List<Player> getOnlineUsers() {
        return repo.findAllOnline(OnlineStatus.Online);
    }

    public void savePlayerChanges(Player player) {
        this.repo.save(player);
    }
}
