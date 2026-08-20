package com.planwith.planwith_fo_token.adapter.out.persistence.wallet;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.port.out.TokenLedgerPort;
import com.planwith.planwith_fo_token.application.port.out.TokenWalletPort;
import com.planwith.planwith_fo_token.domain.model.ReferenceType;
import com.planwith.planwith_fo_token.domain.model.TokenWallet;
import com.planwith.planwith_fo_token.domain.model.TransactionType;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TokenWalletPersistenceAdapterIntegrationTest {

	@Autowired
	private TokenWalletPort tokenWalletPort;

	@Autowired
	private TokenLedgerPort tokenLedgerPort;

	@Test
	void reconstructsWalletFromTokenWalletLedgerTable() {
		MemberUuid memberUuid = MemberUuid.from("22222222-2222-2222-2222-222222222222");
		TokenWallet wallet = TokenWallet.empty(memberUuid);
		Instant now = Instant.parse("2026-08-20T01:00:00Z");

		tokenLedgerPort.save(wallet.grant(
				TransactionType.CHARGE,
				ReferenceType.PAYMENT,
				500L,
				"charge",
				now
		));
		tokenLedgerPort.save(wallet.grant(
				TransactionType.REWARD,
				ReferenceType.GRADE_REWARD,
				40L,
				"grade free",
				now.plusSeconds(1)
		));

		TokenWallet found = tokenWalletPort.getByMemberUuid(memberUuid);
		assertThat(found.getPaidBalance()).isEqualTo(500L);
		assertThat(found.getFreeBalance()).isEqualTo(40L);
		assertThat(found.getTotalBalance()).isEqualTo(540L);
	}
}
