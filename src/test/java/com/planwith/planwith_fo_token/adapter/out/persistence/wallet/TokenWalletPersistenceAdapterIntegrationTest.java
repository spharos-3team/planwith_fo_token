package com.planwith.planwith_fo_token.adapter.out.persistence.wallet;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.port.out.TokenWalletPort;
import com.planwith.planwith_fo_token.domain.model.TokenWallet;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TokenWalletPersistenceAdapterIntegrationTest {

	@Autowired
	private TokenWalletPort tokenWalletPort;

	@Test
	void saveAndFindWallet() {
		MemberUuid memberUuid = MemberUuid.from("22222222-2222-2222-2222-222222222222");
		TokenWallet wallet = TokenWallet.create(memberUuid);
		wallet.credit(500L, com.planwith.planwith_fo_token.domain.model.TokenLedgerEntryType.CHARGE);

		tokenWalletPort.save(wallet);

		TokenWallet found = tokenWalletPort.findByMemberUuid(memberUuid).orElseThrow();
		assertThat(found.balance()).isEqualTo(500L);
		assertThat(found.walletId()).isNotNull();
	}
}
