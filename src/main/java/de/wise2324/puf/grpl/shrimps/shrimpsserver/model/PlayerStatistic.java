package de.wise2324.puf.grpl.shrimps.shrimpsserver.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.entity.PlayerGameStatus;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

import java.util.List;

//@IdClass(PlayerStatisticId.class)
@Entity
@Getter
@Setter
@Table(name = "player_statistic")
public class PlayerStatistic {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PlayerGameStatus status;

    @ManyToOne
    private Player player;

    @ManyToOne
    private Game game;

    @OneToMany(mappedBy = "playerStatistic")
    private List<Shrimp> shrimps;
}