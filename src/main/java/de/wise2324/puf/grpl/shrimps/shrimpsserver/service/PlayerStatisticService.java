package de.wise2324.puf.grpl.shrimps.shrimpsserver.service;

import de.wise2324.puf.grpl.shrimps.shrimpsserver.dto.PlayerStatisticDTO;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.PlayerStatistic;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.Shrimp;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.repository.PlayerStatisticRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

@Service
@Transactional
@ApplicationScope
public class PlayerStatisticService {

    @Autowired
    PlayerStatisticRepository playerStatisticRepository;

    @Autowired
    PlayerService playerService;

    @Autowired
    GameService gameService;

    @Autowired
    ShrimpService shrimpService;

    public PlayerStatisticDTO getDTOFromObject(PlayerStatistic playerStatistic) {
        PlayerStatisticDTO playerStatisticDTO = new PlayerStatisticDTO();

        playerStatisticDTO.setId(playerStatistic.getId());
        playerStatisticDTO.setStatus(playerStatistic.getStatus());

        playerStatisticDTO.setPlayerDTO(
                playerService.getDTOFromObject(
                        playerStatistic.getPlayer()
                )
        );

        for (Shrimp shrimp : playerStatistic.getShrimps()) {
            playerStatisticDTO.addShrimpDTOToList(
                    this.shrimpService.getDTOFromObject(shrimp)
            );
        }

        return playerStatisticDTO;
    }

    public PlayerStatistic save(PlayerStatistic playerStatistic) {
        return this.playerStatisticRepository.save(playerStatistic);
    }
}
