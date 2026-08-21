package com.planwith.planwith_fo_token.adapter.in.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_token.application.port.out.TokenWalletConcurrencyPort;
import com.planwith.planwith_fo_token.config.TokenMockMemberProperties;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

@Component
@ConditionalOnProperty(name = "token.mock.enabled", havingValue = "true")
public class LocalMockMemberInitializer implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(LocalMockMemberInitializer.class);

	private final TokenMockMemberProperties properties;
	private final TokenWalletConcurrencyPort tokenWalletConcurrencyPort;

	public LocalMockMemberInitializer(
			TokenMockMemberProperties properties,
			TokenWalletConcurrencyPort tokenWalletConcurrencyPort
	) {
		this.properties = properties;
		this.tokenWalletConcurrencyPort = tokenWalletConcurrencyPort;
	}

	@Override
	public void run(ApplicationArguments args) {
		MemberUuid memberUuid = new MemberUuid(properties.resolvedMemberUuid());
		log.info("LocalMockMemberInitializer : run : 테스트용 memberUuid 시드 시작 - memberUuid={}", memberUuid);
		tokenWalletConcurrencyPort.executeWithMemberLock(memberUuid, () -> true);
		log.info("LocalMockMemberInitializer : run : 테스트용 memberUuid 시드 완료 - memberUuid={}", memberUuid);
	}
}
