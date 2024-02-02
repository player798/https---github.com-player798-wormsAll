package de.wise2324.puf.grpl.shrimps.shrimpsserver.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Getter
@Setter
@Table(name = "message")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private Timestamp timestamp;

    @Column()
    private String text;

    @ManyToOne
    @JoinColumn(name = "sender_player_id")
    private Player sender;

    @ManyToOne
    @JoinColumn(name = "receiver_player_id")
    private Player receiver;

    @ManyToOne
    @JoinColumn(name = "invite_game_id")
    private Game gameInvite;
}
