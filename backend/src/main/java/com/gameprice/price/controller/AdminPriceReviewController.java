package com.gameprice.price.controller;

import com.gameprice.common.PageResult;
import com.gameprice.common.Result;
import com.gameprice.price.service.PriceReviewService;
import com.gameprice.price.vo.PendingPriceVO;
import com.gameprice.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 待确认队列（AC-12.6）：可审、可驳回。
 * 与 AdminPriceController（POST /admin/prices 手动录入）路径不冲突。
 */
@RestController
@RequestMapping("/admin/prices")
@RequiredArgsConstructor
public class AdminPriceReviewController {

    private final PriceReviewService priceReviewService;

    /** GET /api/admin/prices/pending */
    @GetMapping("/pending")
    public Result<PageResult<PendingPriceVO>> pending(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size) {
        return Result.ok(priceReviewService.pending(page, size));
    }

    /** POST /api/admin/prices/{id}/approve */
    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        priceReviewService.approve(id, UserContext.require().getUsername());
        return Result.ok();
    }

    /** POST /api/admin/prices/{id}/reject，body {"reason":"..."} */
    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id,
                               @RequestBody(required = false) Map<String, String> body) {
        String reason = body == null ? null : body.get("reason");
        priceReviewService.reject(id, UserContext.require().getUsername(), reason);
        return Result.ok();
    }
}
