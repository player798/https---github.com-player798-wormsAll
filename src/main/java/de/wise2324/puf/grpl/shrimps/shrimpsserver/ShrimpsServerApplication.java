package de.wise2324.puf.grpl.shrimps.shrimpsserver;

import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.Player;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class ShrimpsServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShrimpsServerApplication.class, args);

	}

}
