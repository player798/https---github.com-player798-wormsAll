package de.wise2324.puf.grpl.shrimps.shrimpsserver.repository;

import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.Message;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query(value = "SELECT * FROM message m WHERE m.receiver_player_id = ?1", nativeQuery = true)
    List<Message> findByReceiverPlayerId(Long receiverId);

    @Query(value = "SELECT * FROM message m WHERE m.receiver_player_id is null", nativeQuery = true)
    List<Message> findAllForLobby();

    @Query(value = "SELECT * FROM message m WHERE m.receiver_player_id is null ORDER BY m.timestamp DESC LIMIT 20", nativeQuery = true)
    List<Message> findLobbyChat20();
}
