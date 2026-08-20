package com.planwith.planwith_fo_token.adapter.in.web;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_token.adapter.in.web.dto.TokenCommandRequest;
import com.planwith.planwith_fo_token.adapter.in.web.dto.TokenExpireRequest;
import com.planwith.planwith_fo_token.application.command.ChargeTokenCommand;
import com.planwith.planwith_fo_token.application.command.ExpireTokenCommand;
import com.planwith.planwith_fo_token.application.command.GrantTokenCommand;
import com.planwith.planwith_fo_token.application.command.RecoverTokenCommand;
import com.planwith.planwith_fo_token.application.command.UseTokenCommand;
import com.planwith.planwith_fo_token.application.port.in.command.ChargeTokenUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.ExpireTokenUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.GrantTokenUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.RecoverTokenUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.UseTokenUseCase;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;

@RestController
@RequestMapping("/api/planwith-fo-token/members/{memberUuid}/tokens")
public class TokenCommandController {

	private static final Logger log = LoggerFactory.getLogger(TokenCommandController.class);

	private final ChargeTokenUseCase chargeTokenUseCase;
	private final UseTokenUseCase useTokenUseCase;
	private final GrantTokenUseCase grantTokenUseCase;
	private final ExpireTokenUseCase expireTokenUseCase;
	private final RecoverTokenUseCase recoverTokenUseCase;

	public TokenCommandController(
			ChargeTokenUseCase chargeTokenUseCase,
			UseTokenUseCase useTokenUseCase,
			GrantTokenUseCase grantTokenUseCase,
			ExpireTokenUseCase expireTokenUseCase,
			RecoverTokenUseCase recoverTokenUseCase
	) {
		this.chargeTokenUseCase = chargeTokenUseCase;
		this.useTokenUseCase = useTokenUseCase;
		this.grantTokenUseCase = grantTokenUseCase;
		this.expireTokenUseCase = expireTokenUseCase;
		this.recoverTokenUseCase = recoverTokenUseCase;
	}

	// 토큰 충전
	@PostMapping("/charge")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void charge(@PathVariable UUID memberUuid, @RequestBody TokenCommandRequest request) {
		log.info("TokenCommandController : POST charge : 토큰 충전 요청 - memberUuid={}", memberUuid);
		chargeTokenUseCase.charge(toChargeCommand(memberUuid, request));
		log.info("TokenCommandController : POST charge : 토큰 충전 완료 - memberUuid={}", memberUuid);
	}

	// 토큰 사용
	@PostMapping("/use")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void use(@PathVariable UUID memberUuid, @RequestBody TokenCommandRequest request) {
		log.info("TokenCommandController : POST use : 토큰 사용 요청 - memberUuid={}", memberUuid);
		useTokenUseCase.use(toUseCommand(memberUuid, request));
		log.info("TokenCommandController : POST use : 토큰 사용 완료 - memberUuid={}", memberUuid);
	}

	// 토큰 지급
	@PostMapping("/grant")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void grant(@PathVariable UUID memberUuid, @RequestBody TokenCommandRequest request) {
		log.info("TokenCommandController : POST grant : 토큰 지급 요청 - memberUuid={}", memberUuid);
		grantTokenUseCase.grant(toGrantCommand(memberUuid, request));
		log.info("TokenCommandController : POST grant : 토큰 지급 완료 - memberUuid={}", memberUuid);
	}

	// 무료 토큰 만료
	@PostMapping("/expire")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void expire(@PathVariable UUID memberUuid, @RequestBody TokenExpireRequest request) {
		log.info("TokenCommandController : POST expire : 무료 토큰 만료 요청 - memberUuid={}", memberUuid);
		expireTokenUseCase.expire(new ExpireTokenCommand(
				new TransactionUuid(request.transactionUuid()),
				MemberUuid.from(memberUuid.toString())
		));
		log.info("TokenCommandController : POST expire : 무료 토큰 만료 완료 - memberUuid={}", memberUuid);
	}

	// 충전 실패 복구
	@PostMapping("/recover")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void recover(@PathVariable UUID memberUuid, @RequestBody TokenCommandRequest request) {
		log.info("TokenCommandController : POST recover : 충전 실패 복구 요청 - memberUuid={}", memberUuid);
		recoverTokenUseCase.recover(toRecoverCommand(memberUuid, request));
		log.info("TokenCommandController : POST recover : 충전 실패 복구 완료 - memberUuid={}", memberUuid);
	}

	private static UseTokenCommand toUseCommand(UUID memberUuid, TokenCommandRequest request) {
		return new UseTokenCommand(
				new TransactionUuid(request.transactionUuid()),
				MemberUuid.from(memberUuid.toString()),
				request.amount(),
				request.referenceType(),
				request.referenceUuid(),
				request.description()
		);
	}

	private static ChargeTokenCommand toChargeCommand(UUID memberUuid, TokenCommandRequest request) {
		return new ChargeTokenCommand(
				new TransactionUuid(request.transactionUuid()),
				MemberUuid.from(memberUuid.toString()),
				request.amount(),
				request.referenceType(),
				request.referenceUuid(),
				request.description()
		);
	}

	private static GrantTokenCommand toGrantCommand(UUID memberUuid, TokenCommandRequest request) {
		return new GrantTokenCommand(
				new TransactionUuid(request.transactionUuid()),
				MemberUuid.from(memberUuid.toString()),
				request.amount(),
				request.referenceType(),
				request.referenceUuid(),
				request.description()
		);
	}

	private static RecoverTokenCommand toRecoverCommand(UUID memberUuid, TokenCommandRequest request) {
		return new RecoverTokenCommand(
				new TransactionUuid(request.transactionUuid()),
				MemberUuid.from(memberUuid.toString()),
				request.amount(),
				request.referenceType(),
				request.referenceUuid(),
				request.description()
		);
	}
}
