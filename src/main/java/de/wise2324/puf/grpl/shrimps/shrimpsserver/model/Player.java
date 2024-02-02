package de.wise2324.puf.grpl.shrimps.shrimpsserver.model;

import de.wise2324.puf.grpl.shrimps.shrimpsserver.entity.OnlineStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "player")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column()
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OnlineStatus status = OnlineStatus.Offline;

}
