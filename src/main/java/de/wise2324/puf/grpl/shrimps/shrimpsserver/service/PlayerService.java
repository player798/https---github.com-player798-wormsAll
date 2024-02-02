package de.wise2324.puf.grpl.shrimps.shrimpsserver.service;

import de.wise2324.puf.grpl.shrimps.shrimpsserver.dto.HistoryDTO;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.dto.PlayerDTO;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.Player;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.PlayerStatistic;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.repository.PlayerRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import java.util.Optional;

@Service
@Transactional
@ApplicationScope
public class PlayerService {

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    PlayerStatisticService playerStatisticService;

    public PlayerDTO getDTOFromObject(Player player) {
        PlayerDTO result    = new PlayerDTO();

        result.setId(player.getId());
        result.setUsername(player.getUsername());
        result.setEmail(player.getEmail());
        result.setStatus(player.getStatus());
        result.setPassword(player.getPassword());

        return result;
    }
    public void saveFromDTO(PlayerDTO playerDTO) {

        Player player = new Player();

        player.setEmail(playerDTO.getEmail());
        player.setUsername(playerDTO.getUsername());
        player.setPassword(playerDTO.getPassword());
        player.setStatus(playerDTO.getStatus());

        this.playerRepository.save(player);
        playerDTO.setId(player.getId());
    }

    public Player getPlayerByEmail(String email) {

        return this.playerRepository.findByEmail(email);
    }

    public Player save(Player player) {
        return this.playerRepository.save(player);
    }

    public int contains(PlayerDTO playerDTO) {
        Player testEmail = this.playerRepository.findByEmail(playerDTO.getEmail());
        Player testUser  = this.playerRepository.findByUsername(playerDTO.getUsername());

        int result = 0;

        if (testEmail != null) { result = 1; }
        if (testUser != null) { result = 2; }

        return result;
    }
}
