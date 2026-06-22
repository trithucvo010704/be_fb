package vn.ezisolutions.cloud.facebook_service.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum FacebookErrorCode {
    INVALID_ACCESS_TOKEN (190, 0,        FacebookErrorCategory.AUTH,
            "Access token không hợp lệ hoặc đã hết hạn. Vui lòng đăng nhập lại.", 0L),
    INVALID_SESSION      (102, 0,        FacebookErrorCategory.AUTH,
            "Phiên đăng nhập hết hạn. Vui lòng xác thực lại.", 0L),
    USER_CHECKPOINTED    (459, 0,        FacebookErrorCategory.AUTH,
            "Tài khoản Facebook bị checkpoint. Người dùng cần xác minh trên Facebook.", 0L),
    PASSWORD_CHANGED     (460, 0,        FacebookErrorCategory.AUTH,
            "Mật khẩu Facebook đã thay đổi. Vui lòng đăng nhập lại.", 0L),
    EXPIRED_TOKEN        (463, 0,        FacebookErrorCategory.AUTH,
            "Token đã hết hạn. Vui lòng đăng nhập lại.", 0L),

    PERMISSION_DENIED    (200, 0,        FacebookErrorCategory.PERMISSION,
            "Không có quyền thực hiện thao tác này.", 0L),
    PERMISSION_DENIED_ALT(294, 0,        FacebookErrorCategory.PERMISSION,
            "Không đủ quyền quản lý quảng cáo trên tài khoản này.", 0L),
    MISSING_PERMISSION   (10,  0,        FacebookErrorCategory.PERMISSION,
            "Thiếu permission cần thiết. Kiểm tra lại scope OAuth.", 0L),

    ACCOUNT_RESTRICTED   (275, 0,        FacebookErrorCategory.ACCOUNT,
            "Tài khoản quảng cáo bị hạn chế. Liên hệ Facebook Support.", 0L),
    POLICY_VIOLATION     (368, 0,        FacebookErrorCategory.ACCOUNT,
            "Vi phạm chính sách quảng cáo Facebook. Kiểm tra nội dung quảng cáo.", 0L),
    ACCOUNT_UNSETTLED    (471, 0,        FacebookErrorCategory.ACCOUNT,
            "Tài khoản có vấn đề về thanh toán. Kiểm tra phương thức thanh toán.", 0L),
    DAILY_FUNDING_REACHED(514, 0,        FacebookErrorCategory.ACCOUNT,
            "Đã đạt giới hạn chi tiêu ngày. Kiểm tra cài đặt ngân sách.", 0L),

    INVALID_PARAMETER    (100, 0,        FacebookErrorCategory.VALIDATION,
            "Tham số không hợp lệ. Kiểm tra lại dữ liệu đầu vào.", 0L),
    PARAM_REQUIRED       (100, 1349125,  FacebookErrorCategory.VALIDATION,
            "Thiếu tham số bắt buộc.", 0L),
    INVALID_CREATIVE     (100, 1885008,  FacebookErrorCategory.VALIDATION,
            "Creative không hợp lệ. Kiểm tra ảnh/video và nội dung.", 0L),
    DUPLICATE_AD_NAME    (100, 1487297,  FacebookErrorCategory.VALIDATION,
            "Tên quảng cáo đã tồn tại trong AdSet này.", 0L),
    INVALID_BUDGET       (100, 1487399,  FacebookErrorCategory.VALIDATION,
            "Ngân sách không hợp lệ. Kiểm tra giá trị minimum budget.", 0L),
    INVALID_BID          (100, 1487065,  FacebookErrorCategory.VALIDATION,
            "Giá thầu không hợp lệ.", 0L),
    INVALID_TARGETING    (100, 1487569,  FacebookErrorCategory.VALIDATION,
            "Cấu hình targeting không hợp lệ.", 0L),
    SPEND_LIMIT_REACHED  (100, 1487390,  FacebookErrorCategory.ACCOUNT,
            "Đã đạt giới hạn chi tiêu tài khoản.", 0L),
    ACCOUNT_DISABLED     (100, 1487926,  FacebookErrorCategory.ACCOUNT,
            "Tài khoản quảng cáo đã bị vô hiệu hóa.", 0L),
    PAGE_NOT_FOUND       (100, 1487470,  FacebookErrorCategory.PAGE,
            "Không tìm thấy Facebook Page hoặc không có quyền truy cập.", 0L),
    PAGE_UNPUBLISHED     (100, 1487471,  FacebookErrorCategory.PAGE,
            "Facebook Page chưa được publish.", 0L),
    RATE_LIMIT_APP           (4,     0,  FacebookErrorCategory.RATE_LIMIT,
            "Vượt giới hạn API theo app. Thử lại sau 1 phút.",    60_000L),
    RATE_LIMIT_EXCEEDED      (17,    0,  FacebookErrorCategory.RATE_LIMIT,
            "Vượt giới hạn gọi API. Thử lại sau 5 phút.",         300_000L),
    RATE_LIMIT_USER          (613,   0,  FacebookErrorCategory.RATE_LIMIT,
            "Vượt giới hạn API theo user. Thử lại sau 10 phút.",  600_000L),
    RATE_LIMIT_ADS_MANAGEMENT(80004, 0,  FacebookErrorCategory.RATE_LIMIT,
            "Vượt giới hạn Ads Management API. Thử lại sau 5 phút.", 300_000L),
    AD_DISAPPROVED       (1580005, 0,    FacebookErrorCategory.AD_REVIEW,
            "Quảng cáo bị từ chối. Kiểm tra nội dung theo chính sách Facebook Ads.", 0L),
    AD_PENDING_REVIEW    (1580015, 0,    FacebookErrorCategory.AD_REVIEW,
            "Quảng cáo đang chờ duyệt.", 0L),
    IMAGE_NOT_FOUND      (1487061, 0,    FacebookErrorCategory.ASSET,
            "Không tìm thấy ảnh. Image hash có thể đã hết hạn (>24h).", 0L),
    VIDEO_NOT_FOUND      (1487200, 0,    FacebookErrorCategory.ASSET,
            "Không tìm thấy video trên tài khoản này.", 0L),
    VIDEO_STILL_PROCESSING(1487201, 0,   FacebookErrorCategory.ASSET,
            "Video đang được xử lý. Vui lòng thử lại sau 1-2 phút.", 120_000L),
    INVALID_IMAGE_FORMAT (1487212, 0,    FacebookErrorCategory.ASSET,
            "Định dạng ảnh không được hỗ trợ. Dùng JPG, PNG, GIF, BMP (tối đa 30MB).", 0L),
    INVALID_VIDEO_FORMAT (1487213, 0,    FacebookErrorCategory.ASSET,
            "Định dạng video không được hỗ trợ. Dùng MP4, MOV, AVI (tối đa 4GB, 240s).", 0L),

    MESSAGED_ALREADY     (10900, 0,  FacebookErrorCategory.VALIDATION,
            "Khách hàng này đã được Page nhắn tin trước đó rồi.", 0L),
    MESSAGED_ALREADY_ALT (10903, 0,  FacebookErrorCategory.VALIDATION,
            "Khách hàng này đã được Page nhắn tin trước đó rồi.", 0L),
    MESSAGE_NOT_SUPPORTED(100, 33,   FacebookErrorCategory.VALIDATION,
            "Không hỗ trợ nhắn tin (Do là Comment Reply hoặc Comment đã xóa).", 0L),
    BLOCKED_BY_USER      (551, 0,    FacebookErrorCategory.VALIDATION,
            "Khách hàng này đã chặn tin nhắn từ Page.", 0L),
    SPAM_OR_SELF_MESSAGE (1, 0,      FacebookErrorCategory.VALIDATION,
            "Lỗi Spam hoặc Admin tự nhắn cho chính mình.", 0L),

    UNKNOWN(-1, 0, FacebookErrorCategory.SYSTEM,
            "Lỗi hệ thống không xác định từ Facebook API.", 0L);

    private final int code;
    private final int subcode;
    private final FacebookErrorCategory category;
    private final String userMessage;
    private final long retryAfterMs;

    private static final Map<Long, FacebookErrorCode> BY_CODE_SUBCODE = new HashMap<>();

    static {
        for (FacebookErrorCode ec : values()) {
            if (ec != UNKNOWN) {
                BY_CODE_SUBCODE.put(key(ec.code, ec.subcode), ec);
            }
        }
    }

    public static FacebookErrorCode from(int code, int subcode) {
        FacebookErrorCode exact = BY_CODE_SUBCODE.get(key(code, subcode));
        if (exact != null) return exact;

        FacebookErrorCode fallback = BY_CODE_SUBCODE.get(key(code, 0));
        if (fallback != null) return fallback;

        return UNKNOWN;
    }

    private static long key(int code, int subcode) {
        return (long) code * 10_000_000L + subcode;
    }
}
