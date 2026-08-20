package com.planwith.planwith_fo_token.adapter.out.persistence.wallet;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.port.out.TokenLedgerPort;
import com.planwith.planwith_fo_token.application.port.out.TokenWalletPort;
import com.planwith.planwith_fo_token.domain.model.TokenWallet;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.service.TokenWalletReconstructor;

@Component
public class TokenWalletPersistenceAdapter implements TokenWalletPort {

	private final TokenLedgerPort tokenLedgerPort;

	public TokenWalletPersistenceAdapter(TokenLedgerPort tokenLedgerPort) {
		this.tokenLedgerPort = tokenLedgerPort;
	}

	@Override
	@Transactional(readOnly = true)
	public TokenWallet getByMemberUuid(MemberUuid memberUuid) {
		return TokenWalletReconstructor.reconstruct(
				memberUuid,
				tokenLedgerPort.findByMemberUuidChronological(memberUuid)
		);
	}
}
