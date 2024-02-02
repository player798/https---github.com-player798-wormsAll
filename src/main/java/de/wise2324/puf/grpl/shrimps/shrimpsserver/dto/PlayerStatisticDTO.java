package de.wise2324.puf.grpl.shrimps.shrimpsserver.dto;

import de.wise2324.puf.grpl.shrimps.shrimpsserver.entity.PlayerGameStatus;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.PlayerStatistic;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.Shrimp;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PlayerStatisticDTO {

    private Long id;
    private PlayerGameStatus status;

    private PlayerDTO playerDTO;

    private List<ShrimpDTO> shrimpDTOList;

    public PlayerStatisticDTO() {
        this.id = -1L;

        this.playerDTO = null;
        this.status = PlayerGameStatus.NOT_READY;
        this.shrimpDTOList = new ArrayList<>();
    }

    public void addShrimpDTOToList(ShrimpDTO shrimpDTO) {
        this.shrimpDTOList.add(shrimpDTO);
    }
}
