package de.wise2324.puf.grpl.shrimps.shrimpsserver.service;

import de.wise2324.puf.grpl.shrimps.shrimpsserver.dto.ShrimpDTO;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.Shrimp;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.repository.ShrimpRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

@Service
@Transactional
@ApplicationScope
public class ShrimpService {

    @Autowired
    ShrimpRepository shrimpRepository;

    public ShrimpDTO getDTOFromObject(Shrimp shrimp) {
        ShrimpDTO shrimpDTO = new ShrimpDTO();

        shrimpDTO.setId(shrimp.getId());
        shrimpDTO.setHitpoints(shrimp.getHitpoints());
        shrimpDTO.setName(shrimp.getName());
        shrimpDTO.setX_position(shrimp.getX_position());
        shrimpDTO.setY_position(shrimp.getY_position());
        shrimpDTO.setParentPlayerStatisticId(shrimp.getPlayerStatistic().getId());
        shrimpDTO.setLookingRight(shrimp.isLookingRight());
        shrimpDTO.setLastActiveOnRound(shrimp.getLastActiveOnRound());

        return shrimpDTO;
    }
}