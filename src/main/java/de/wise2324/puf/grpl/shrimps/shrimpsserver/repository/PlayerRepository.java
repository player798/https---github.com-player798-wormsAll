package de.wise2324.puf.grpl.shrimps.shrimpsserver.repository;

import de.wise2324.puf.grpl.shrimps.shrimpsserver.entity.OnlineStatus;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    @Query(value = "SELECT * FROM player p WHERE p.email = ?1", nativeQuery = true)
    Player findByEmail(String email);

    //
    @Query(value = "SELECT * FROM player p WHERE p.status =:#{#status.name()}", nativeQuery = true)
    List<Player> findAllOnline(@Param("status") OnlineStatus status);

    @Query(value = "SELECT * FROM player p WHERE p.username = ?1", nativeQuery = true)
    Player findByUsername(String username);
}
