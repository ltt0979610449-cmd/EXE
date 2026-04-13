package swd.coiviet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountVouchersResponse {
    /** Voucher user đã nhận (từ quiz, admin gửi) - chưa dùng */
    private List<UserVoucherClaimedResponse> userVouchers;
    /** Voucher hệ thống (mã công khai) - user chưa dùng */
    private List<VoucherResponse> systemVouchers;
}
