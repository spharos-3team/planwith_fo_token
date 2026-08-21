package com.planwith.planwith_fo_token.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Domain/Application이 Kafka 구현체를 직접 참조하지 않는지 검증한다.
 */
class KafkaHexagonalBoundaryTest {

	@Test
	void domainAndApplicationDoNotDependOnSpringKafka() throws IOException {
		Path root = Path.of("src/main/java/com/planwith/planwith_fo_token");
		try (Stream<Path> paths = Files.walk(root)) {
			paths.filter(path -> path.toString().endsWith(".java"))
					.filter(path -> {
						String absolute = path.toString().replace('\\', '/');
						return absolute.contains("/domain/") || absolute.contains("/application/");
					})
					.forEach(path -> {
						String source;
						try {
							source = Files.readString(path);
						} catch (IOException exception) {
							throw new IllegalStateException(exception);
						}
						assertThat(source)
								.as(path.toString())
								.doesNotContain("org.springframework.kafka")
								.doesNotContain("KafkaTemplate")
								.doesNotContain("KafkaListener");
					});
		}
	}
}
