package de.wise2324.puf.grpl.shrimps.shrimpsserver.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.entity.GameState;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.util.List;
import java.sql.Timestamp;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@Setter
@Table(name = "game")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private GameState status;

    @Column(nullable = false)
    private Timestamp timestamp;

    @Column(nullable = false)
    private int roundToSuddenDeath = -1;

    @Column(nullable = false)
    private int roundTimeLimit = 30;

    @Lob @Basic(fetch=LAZY)
    @Column(columnDefinition = "BLOB NOT NULL")
    private byte[] foregroundMask;

    @Column
    private int levelForeground = 1;

    @Column
    private int levelBackground = 1;

    @Column(columnDefinition = "integer default 0")
    private int roundNumber = 0;

    @Column
    private Long currentPlayerId = 0L;

    @Column(columnDefinition = "long default 0")
    private Long currentShrimpId = 0L;

    @Column(columnDefinition = "integer default -1")
    private int currentRound = 0;

    @Column(columnDefinition = "integer default -1")
    private int currentPlayerIdx = -1;

    @Column(columnDefinition = "integer default -1")
    private int currentShrimpIdx = -1;

    /*
    @OneToMany
    private List<Weapon> defaultLoadout;
    */

    @OneToMany(mappedBy = "game")
    private List<PlayerStatistic> playerStatisticsList;
}
