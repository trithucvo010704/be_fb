# Quyết định phase 1 và phase 2

## Phase 1 - Chốt scope và dọn khung

- Mục tiêu trước mắt là phục vụ Meta App Review, chưa làm các phase Ads, Marketing API, Business Management, insight nâng cao, auto/scheduled post, AI post hoặc multi-service callback.
- Backend sẽ tự lưu Messenger conversation/message trong database của chính service, không dùng chat generic cũ, không dùng Kafka chat bridge.
- Xóa mô hình chat generic cũ gồm controller/service/entity/repository/listener chat.
- Giữ base Redis/Kafka trong repo để không mất nền tảng kỹ thuật, nhưng các consumer/listener/scheduler không thuộc phase 1 phải tắt mặc định bằng feature flag.
- Phase 1 có thể hỗ trợ đăng bài text và ảnh bằng URL/paste link ảnh. Không mở Story/Reels/video/schedule trong flow duyệt.
- Ads và comment/reaction moderation giữ dormant, không expose trong API/UI App Review và không được tự chạy trong flow connect/login.

## Phase 2 - Chuẩn hóa schema và entity

- `docs/db.dbml` là nguồn tham chiếu chính cho entity phase 1.
- Entity phase 1 dùng package `entity.facebook`:
  - `FbUser`
  - `FbPage`
  - `FbPageClient`
  - `FbConversation`
  - `FbMessage`
  - `FbPagePost`
  - `FbPagePostMedia`
  - `FbPageDisconnectLog`
  - `FbDataDeletionRequest`
- Token phải được biểu diễn bằng field `accessTokenEncrypted` và `pageAccessTokenEncrypted`.
- Trạng thái page/token phải đủ để chặn thao tác sau disconnect: `CONNECTED`, `DISCONNECTED`, `ACTIVE`, `REVOKED`, `INVALID`, `MISSING_PERMISSION`.
- Các entity/service Ads, sync, scheduled post, comment/reaction được giữ dormant cho phase sau, nhưng không tham gia phase duyệt.

## Phase 3 - API bảo vệ, demo login và policy

- Tạo security chain riêng cho `/api/**` để không ảnh hưởng các chain cũ `/base/**`, `/biz/**`, `/_private/**`.
- Demo reviewer dùng endpoint `/api/auth/demo-login` để nhận token nội bộ.
- Các API nghiệp vụ phase 1 dùng bearer token demo qua `ApiTokenFilter`.
- Privacy Policy và Data Deletion là public endpoint để phục vụ Meta App Review.
- Không tạo business rule mới ngoài scope đã chốt trong docs và trao đổi với user.

## Phase 4 - OAuth scaffold và Page connect

- Backend tạo OAuth URL và nhận callback, nhưng chưa tự exchange `code` sang token trong task này.
- Endpoint connect Page tạm nhận user access token để tận dụng code hiện có và phục vụ luồng duyệt sớm.
- Token user/page phải lưu encrypted trong entity/database.
- Khi gọi Graph API nội bộ, service giải mã token qua `TokenCryptoService`.
- Subscribe webhook trong flow connect được thử tự động; nếu fail thì không làm fail toàn bộ connect, để page vẫn hiện được cho reviewer và phase sau xử lý permission/troubleshooting rõ hơn.
- Inbox, send message, publish post, disconnect và data deletion callback hoàn chỉnh chuyển sang phase tiếp theo.

## Phase 5 - API thao tác phục vụ reviewer

- Inbox đọc từ database nội bộ `fb_conversations` và `fb_messages`; backend không dùng lại chat generic cũ.
- Outbound Messenger message phải gọi Graph API rồi lưu vào `fb_messages` để reviewer thấy lịch sử gửi/nhận trong cùng service.
- Publish phase 5 đi trực tiếp Graph API, không bật scheduled worker cũ.
- Publish text dùng Page feed; publish ảnh URL dùng Page photos và lưu media URL vào `fb_page_post_media`.
- Disconnect Page không xóa row Page; chỉ chuyển trạng thái sang `DISCONNECTED`, token status `DISCONNECTED`, ghi log và evict token cache để chặn thao tác sau đó.
- Data deletion phase 5 lưu yêu cầu nhận được để có confirmation code; verify `signed_request` bằng app secret để lại cho bước hardening nếu Meta yêu cầu callback thật.
- DTO request/response mới dùng Java `record` vì đây là data carrier bất biến, không phải entity JPA.

## Phase 6 - OAuth exchange

- OAuth callback không trả raw user access token ra response.
- Nếu frontend gọi `/api/facebook/oauth/url` kèm bearer token demo, backend lưu state với owner để callback tự connect Page sau khi exchange code.
- Nếu callback không resolve được owner từ state, backend chỉ báo đã exchange thành công và `connected=false`; frontend có thể xử lý lại bằng flow connect cũ nếu cần.
- State hiện lưu in-memory trong service cho phase duyệt local/simple deployment; nếu scale nhiều instance, cần chuyển state store sang Redis.

## Phase 7 - Data deletion signed_request

- `signed_request` phải verify bằng HMAC SHA256 với `facebook.app-secret` trước khi tin payload.
- Nếu chữ ký sai, không lưu data deletion request.
- Nếu payload có `user_id`, dùng làm `fbUserId` khi request không truyền rõ.
- Không lưu nguyên `signed_request` vào note để giảm rủi ro log/token payload; chỉ lưu trạng thái verify và algorithm.

## API response DTO rule

- API controller không trả trực tiếp entity JPA ra ngoài.
- Page/User token field không được serialize trong response, kể cả token đã encrypted.
- Các response public/reviewer phải đi qua DTO hoặc response object rõ ràng.

## Messenger profile enrichment

- Webhook message vẫn là nguồn tạo/tìm conversation và lưu message.
- Tên/avatar người nhắn không có sẵn trong webhook nên phải enrich bằng Graph API theo PSID.
- Enrichment là best-effort: lỗi lấy profile không được làm fail việc lưu message.
- Chỉ gọi profile khi conversation chưa có tên/avatar để tránh gọi Graph API thừa.
