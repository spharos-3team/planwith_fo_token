package com.planwith.planwith_fo_token.adapter.in.web.dto;

import java.util.UUID;

public record TokenExpireRequest(
		UUID transactionUuid
) {
}
