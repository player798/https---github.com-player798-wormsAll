package de.wise2324.puf.grpl.shrimps.shrimpsserver;

import de.wise2324.puf.grpl.shrimps.shrimpsserver.controller.ShrimpServerAuthenticationController;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.controller.ShrimpServerLobbyController;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.dto.PlayerDTO;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.entity.OnlineStatus;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.Message;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.Player;

import de.wise2324.puf.grpl.shrimps.shrimpsserver.repository.PlayerRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.sql.Timestamp;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ShrimpsServerApplicationTests {

	@Autowired
	ShrimpServerAuthenticationController authController;

	@Autowired
	ShrimpServerLobbyController lobbyController;

	@Autowired
	PlayerRepository playerRepository;

	@Test
	@Order(1)
	void contextLoads() {
		assertThat(authController).isNotNull();
		assertThat(lobbyController).isNotNull();
	}

	@Test
	@Order(2)
	void createInitialUser() {
		String searchString = "register_success";

		PlayerDTO player = new PlayerDTO();
		player.setEmail("test@mail.de");
		player.setPassword("start123");
		player.setUsername("Tester");
		player.setStatus(OnlineStatus.Offline);

		Model model = new ConcurrentModel();
		String answer = authController.processRegister(player);
		assertThat(answer).contains(searchString);
		assertThat(player.getId()).isGreaterThan(0);
	}

	@Test
	@Order(3)
	void createLobbyMessages() {
		Message m = new Message();
		m.setText("Hallo, Sie haben 3 gelbe Bananen gewählt!");
		m.setSender(playerRepository.findByEmail("test@mail.de"));
		m.setTimestamp(Timestamp.from(ZonedDateTime.now().toInstant()));

		String result = lobbyController.sendMessage(m);

		assertThat(result).contains("lobby");
	}
}
