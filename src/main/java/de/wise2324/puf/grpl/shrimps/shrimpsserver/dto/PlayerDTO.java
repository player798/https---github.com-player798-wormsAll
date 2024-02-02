package de.wise2324.puf.grpl.shrimps.shrimpsserver.dto;

import de.wise2324.puf.grpl.shrimps.shrimpsserver.entity.OnlineStatus;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.Player;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PlayerDTO {

    private Long id;
    private String username;
    private String email;
    private OnlineStatus status;
    private String password;

    private List<Long> playerStatisticIdList;

    public PlayerDTO() {
        this.status = OnlineStatus.Offline;
        this.playerStatisticIdList = new ArrayList<>();
    }

    public void addPlayerStatisticId(Long id) {
        this.playerStatisticIdList.add(id);
    }
}
