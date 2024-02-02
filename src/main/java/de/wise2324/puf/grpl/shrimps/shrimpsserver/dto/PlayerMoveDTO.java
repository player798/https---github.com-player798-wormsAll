package de.wise2324.puf.grpl.shrimps.shrimpsserver.dto;

import de.wise2324.puf.grpl.shrimps.shrimpsserver.entity.MoveDirection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerMoveDTO {

    private ShrimpDTO shrimpDTO;
    private MoveDirection moveDirection;
}
