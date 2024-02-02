package de.wise2324.puf.grpl.shrimps.shrimpsserver.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "shrimp")
public class Shrimp {

    @Id
    @GeneratedValue
    private Long id;

    @Column
    private int hitpoints = 100;

    @Column
    private String name;

    @Column
    private int x_position;

    @Column
    private int y_position;

    /*
    @OneToMany
    private List<Weapon> loadout;
    */

    @Column
    private boolean isLookingRight = true;

    @Column
    private int lastActiveOnRound = 0;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    private PlayerStatistic playerStatistic;
}