package com.planwith.planwith_fo_token.adapter.out.persistence.wallet;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.port.out.LoadTokenLedgerPort;
import com.planwith.planwith_fo_token.application.port.out.SaveTokenLedgerPort;
import com.planwith.planwith_fo_token.application.port.out.SaveTokenWalletPort;
import com.planwith.planwith_fo_token.application.port.out.TokenWalletPort;
import com.planwith.planwith_fo_token.domain.model.TokenLedger;
import com.planwith.planwith_fo_token.domain.model.TokenWallet;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.service.TokenWalletReconstructor;

@Component
public class TokenWalletPersistenceAdapter implements TokenWalletPort, SaveTokenWalletPort {

	private final LoadTokenLedgerPort loadTokenLedgerPort;
	private final SaveTokenLedgerPort saveTokenLedgerPort;

	public TokenWalletPersistenceAdapter(
			LoadTokenLedgerPort loadTokenLedgerPort,
			SaveTokenLedgerPort saveTokenLedgerPort
	) {
		this.loadTokenLedgerPort = loadTokenLedgerPort;
		this.saveTokenLedgerPort = saveTokenLedgerPort;
	}

	@Override
	@Transactional(readOnly = true)
	public TokenWallet load(MemberUuid memberUuid) {
		return TokenWalletReconstructor.reconstruct(
				memberUuid,
				loadTokenLedgerPort.findByMemberUuidChronological(memberUuid)
		);
	}

	@Override
	@Transactional
	public TokenLedger saveMutation(TokenLedger ledger) {
		return saveTokenLedgerPort.save(ledger);
	}
}
