# Báo cáo chi tiết thay đổi backend `facebook_service`

Ngày tổng hợp: 2026-06-22

## 1. Mục tiêu đã chốt

Backend `facebook_service` được đưa về vai trò **Central Facebook Service** phục vụ Meta App Review trước.

Scope phase duyệt hiện tại:

- Demo login cho reviewer.
- Facebook OAuth login/connect Page.
- Lưu Page đã connect.
- Webhook Messenger inbound.
- Inbox conversation/message.
- Gửi message text/image URL.
- Đăng post text/image URL.
- Disconnect Page.
- Privacy Policy.
- Data Deletion.
- Token lưu DB dạng encrypted.

Ngoài scope phase duyệt:

- Ads/Marketing API.
- Business Management nâng cao.
- Scheduled post worker.
- AI auto post.
- Story/Reels/video upload.
- Insight/report nâng cao.
- Kafka chat bridge.
- Chat generic cũ.

## 2. Những logic đã bỏ khỏi flow phase duyệt

### 2.1. Bỏ chat generic cũ

Đã loại bỏ mô hình chat generic cũ khỏi phase duyệt vì backend hiện tại phải tự lưu conversation/message trong chính service này.

Các nhóm đã xóa khỏi source hoặc không còn dùng:

- Controller chat generic:
  - `AgentConversationController`
  - `ChatConversationController`
  - `ChatMessageController`
  - `WidgetChatConversationController`
- DTO chat generic.
- Entity chat generic.
- Repository chat generic.
- Service chat generic.
- Listener chat generic.
- Cấu hình Redis listener riêng cho chat generic.
- `ChatRedisChannels`.
- `SourceType` của chat cũ.
- Các service bridge chat/Facebook cũ trong package chat.

Kết luận: Messenger hiện không đi qua chat service cũ nữa.

### 2.2. Bỏ Kafka chat bridge khỏi flow hiện tại

Kafka base vẫn giữ trong repo, nhưng flow Messenger phase duyệt không dùng Kafka.

Flow mới:

```text
Facebook webhook
-> FacebookNotificationService
-> FbMessagingWebhookHandler
-> FacebookMessagePersistenceService
-> fb_conversations / fb_messages
```

Không còn flow:

```text
Facebook webhook
-> Kafka chat event
-> chat service cũ
```

### 2.3. Bỏ Ads sync khỏi connect Page phase duyệt

`FacebookPermissionService` đã được chỉnh để connect/sync Page trong scope phase duyệt, không còn gọi Ads sync và không còn tạo ChatSource/FbUserPage cũ trong flow phase 1.

Ads entity/service vẫn giữ dormant cho phase sau.

## 3. Những service/worker đã tắt mặc định

Trong `application.properties`, đã thêm feature flags mặc định `false`:

```properties
facebook.features.ads.enabled=false
facebook.features.comments.enabled=false
facebook.features.insights.enabled=false
facebook.features.scheduled-posts.enabled=false
facebook.features.sync-history.enabled=false
```

Các component bị tắt mặc định bằng `@ConditionalOnProperty`:

- `FacebookCommentConsumer`
  - flag: `facebook.features.comments.enabled=true`
- `FacebookPostConsumer`
  - flag: `facebook.features.scheduled-posts.enabled=true`
- `FacebookSyncConsumer`
  - flag: `facebook.features.sync-history.enabled=true`
- `FbWorkerScheduler`
  - flag: `facebook.features.scheduled-posts.enabled=true`
- `FbAdsVideoScheduler`
  - flag: `facebook.features.ads.enabled=true`
- `FbCommentWebhookHandler`
  - flag: `facebook.features.comments.enabled=true`
- `FbReactionWebhookHandler`
  - flag: `facebook.features.comments.enabled=true`

Ngoài ra đã bỏ `@EnableScheduling` khỏi application chính để scheduler không tự chạy trong phase duyệt.

## 4. Những phần base vẫn giữ lại

Không xóa nền tảng Redis/Kafka vì sau này vẫn có thể cần.

Giữ lại:

- `KafkaConfig`
- `KafkaProducer`
- `RedisConfig`
- `RedisClient`
- `RedisPublisher`
- `FacebookEventProducer`
- Ads services/entities dormant
- Sync services dormant
- Scheduled post worker dormant
- Comment/reaction moderation dormant

Nguyên tắc: giữ code nền, nhưng không cho tham gia flow App Review khi feature flag đang false.

## 5. Schema/entity đã chuẩn hóa theo DBML

DBML nguồn chính: `docs/db.dbml`.

Đã bổ sung/chuẩn hóa các entity phase duyệt:

- `FbUser`
- `FbPage`
- `FbPageClient`
- `FbConversation`
- `FbMessage`
- `FbPagePost`
- `FbPagePostMedia`
- `FbPageDisconnectLog`
- `FbDataDeletionRequest`

Repository tương ứng:

- `FbPageClientRepository`
- `FbConversationRepository`
- `FbMessageRepository`
- `FbPagePostRepository`
- `FbPagePostMediaRepository`
- `FbPageDisconnectLogRepository`
- `FbDataDeletionRequestRepository`

### 5.1. Token user/page

Token được lưu encrypted:

- `FbUser.accessTokenEncrypted`
- `FbPage.pageAccessTokenEncrypted`

Đã thêm `@JsonIgnore` để API không serialize token ra response, kể cả encrypted token.

### 5.2. Trạng thái Page/token

`FbPage` có:

- `tokenStatus`
  - `ACTIVE`
  - `DISCONNECTED`
  - `EXPIRED`
  - `REVOKED`
  - `INVALID`
  - `MISSING_PERMISSION`
- `connectionStatus`
  - `CONNECTED`
  - `DISCONNECTED`
  - `MISSING_PERMISSION`

Guard thao tác Page dùng `FacebookPageAccessGuard`.

## 6. Security và demo auth

### 6.1. API security chain

Đã tạo security chain riêng cho:

```text
/api/**
/privacy-policy
/data-deletion
```

Các API nghiệp vụ cần bearer token demo, ngoại trừ các endpoint public được skip trong `ApiTokenFilter`.

### 6.2. Public endpoint

Các endpoint public:

- `POST /api/auth/demo-login`
- `GET /api/facebook/oauth/url`
- `GET /api/facebook/oauth/callback`
- `POST /api/data-deletion`
- `GET /privacy-policy`
- `GET /data-deletion`
- `GET/POST /base/facebook/webhooks`
- `/api/public/**`

### 6.3. Demo auth

Thêm `DemoAuthService`.

Config demo:

```properties
app.demo.email=${DEMO_EMAIL:reviewer@ezisolutions.tech}
app.demo.password=${DEMO_PASSWORD:reviewer-password}
app.demo.user-id=${DEMO_USER_ID:demo-reviewer}
app.demo.name=${DEMO_NAME:Meta Reviewer}
```

Token demo được lưu qua Redis bằng `AuthCacheService`.

## 7. OAuth và connect Page

### 7.1. OAuth URL

Endpoint:

```text
GET /api/facebook/oauth/url
```

Response DTO:

- `FacebookOAuthUrlResponse`

Scope hiện tại:

```text
public_profile
pages_show_list
pages_manage_metadata
pages_messaging
pages_manage_posts
pages_read_engagement
```

### 7.2. OAuth callback exchange code

Endpoint:

```text
GET /api/facebook/oauth/callback
```

Service:

- `FacebookOAuthService`
- `FacebookOAuthGateway`

Flow:

```text
Facebook redirect callback code/state
-> exchange /oauth/access_token
-> nếu state gắn được AuthorizedUser thì tự connect Page
-> nếu không gắn được user thì trả exchanged=true, connected=false
```

Không trả raw access token ra response.

### 7.3. Connect Page

Service:

- `FacebookPageConnectService`

Flow:

```text
user access token
-> GET /me
-> upsert FbUser
-> GET /me/accounts
-> upsert FbPage
-> subscribe webhook Page
-> upsert FbPageClient
```

Subscribe webhook fields:

```text
messages,messaging_postbacks,messaging_optins,feed
```

## 8. Messenger inbound flow

Webhook endpoint:

```text
GET  /base/facebook/webhooks
POST /base/facebook/webhooks
```

Flow:

```text
Facebook sends messages webhook
-> WebhookController
-> FacebookNotificationService
-> validate X-Hub-Signature-256
-> check Page active + webhookSubscribed=true
-> FbMessagingWebhookHandler
-> FacebookMessagePersistenceService.saveInbound
```

Persistence:

```text
find Page by fbPageId
find/create conversation by page_id + sender_psid
update lastMessageAt / lastInboundAt / unreadCount
enrich sender profile if missing
save conversation
save message with conversation_id
```

Tables:

- `fb_conversations`
- `fb_messages`
- `fb_notifications`

## 9. Messenger profile enrichment

Mục tiêu: khi người dùng nhắn vào Page, conversation có tên/avatar.

Đã thêm:

- `FacebookMessengerProfileGateway`
- `FacebookMessengerProfileService`
- `FacebookMessengerProfileResponse`

Graph call:

```text
GET /{psid}?fields=first_name,last_name,name,profile_pic&access_token={PAGE_ACCESS_TOKEN}
```

Lưu vào:

- `fb_conversations.sender_name`
- `fb_conversations.sender_avatar_url`
- `fb_conversations.raw_profile`

Nguyên tắc:

- Chỉ gọi profile khi conversation chưa có tên/avatar.
- Nếu Graph API lỗi thì bỏ qua, không làm fail webhook.
- Message vẫn phải được lưu kể cả khi enrich profile thất bại.

## 10. Messenger outbound flow

Endpoint:

```text
POST /api/pages/{pageId}/messages
```

Request DTO:

- `FacebookSendMessageRequest`

Response DTO:

- `FacebookMessageResponse`

Hỗ trợ:

- `TEXT`
- `IMAGE` bằng URL public

Flow:

```text
API request
-> FacebookPageAccessGuard.requireConnectedPage
-> lấy page access token
-> find/create conversation by page_id + recipient_psid
-> gọi Graph API /me/messages
-> lưu outbound message vào fb_messages
```

Lưu ý sau test:

- Gửi ảnh URL có thể mất thời gian vì Facebook phải fetch URL ảnh.
- Đã tăng read timeout Graph API từ `10s` lên `30s`.
- URL ảnh mẫu trong Postman đổi thành `https://placehold.co/600x400.jpg`.

## 11. Publish post flow

Endpoint:

```text
POST /api/pages/{pageId}/posts
GET  /api/pages/{pageId}/posts
```

Request DTO:

- `FacebookPublishPostRequest`

Response DTO:

- `FacebookPagePostResponse`

Hỗ trợ:

- Post text lên Page feed.
- Post ảnh URL qua Page photos.

Flow:

```text
API request
-> guard Page connected/token active
-> create fb_page_posts status PUBLISHING
-> save fb_page_post_media if imageUrls exists
-> call Graph API /{pageId}/feed or /{pageId}/photos
-> update status PUBLISHED / FAILED
```

Không dùng scheduled worker cũ.

## 12. Disconnect Page flow

Endpoint:

```text
POST /api/pages/{pageId}/disconnect
```

Request DTO:

- `FacebookDisconnectPageRequest`

Response DTO:

- `FacebookDisconnectPageResponse`

Flow:

```text
guard Page connected
-> set connectionStatus=DISCONNECTED
-> set tokenStatus=DISCONNECTED
-> set disconnectedAt
-> insert fb_page_disconnect_logs
-> evict page token cache
```

Sau disconnect:

- Send message bị chặn.
- Publish post bị chặn.
- Page không còn xuất hiện trong danh sách connected.

## 13. Data deletion flow

Public endpoint:

```text
POST /api/data-deletion
GET  /data-deletion
```

Request DTO:

- `FacebookDataDeletionRequest`

Response DTO:

- `FacebookDataDeletionResponse`

Đã thêm:

- `FacebookDataDeletionService`
- `FacebookSignedRequestService`

Hỗ trợ:

- JSON body.
- Form urlencoded.
- Facebook `signed_request`.

Signed request:

```text
signature.payload
```

Verify:

```text
HMAC SHA256 bằng facebook.app-secret
```

Nếu hợp lệ:

- parse payload base64url JSON.
- lấy `user_id` làm `fbUserId` nếu request chưa truyền.
- lưu `fb_data_deletion_requests`.
- trả confirmation code.

Nếu sai chữ ký:

- reject.
- không lưu request.

Không lưu nguyên `signed_request` vào note.

## 14. DTO response rule

Đã chốt rule:

- Controller không trả entity JPA trực tiếp.
- Token không serialize ra response, kể cả encrypted token.
- Public/reviewer response phải qua DTO rõ ràng.

DTO response chính:

- `DemoLoginResponse`
- `AuthorizedUserResponse`
- `FacebookOAuthUrlResponse`
- `FacebookOAuthExchangeResponse`
- `FacebookConnectedPageResponse`
- `FacebookConversationResponse`
- `FacebookMessageResponse`
- `FacebookPagePostResponse`
- `FacebookDisconnectPageResponse`
- `FacebookDataDeletionResponse`
- `FacebookMessengerProfileResponse`

## 15. API hiện có và mục đích

### Auth

```text
POST /api/auth/demo-login
```

Đăng nhập demo reviewer, trả bearer token.

```text
GET /api/auth/me
```

Lấy thông tin user hiện tại bằng bearer token.

```text
POST /api/auth/logout
```

Endpoint logout placeholder, hiện trả success.

### OAuth/Page connect

```text
GET /api/facebook/oauth/url
```

Tạo Facebook OAuth URL. Nếu có bearer token thì state được gắn với demo user.

```text
GET /api/facebook/oauth/callback
```

Facebook redirect callback. Exchange code và auto connect Page nếu state hợp lệ.

```text
POST /api/facebook/pages/connect
```

Connect Page thủ công bằng user access token. Giữ để fallback/debug.

### Page

```text
GET /api/pages
```

Danh sách Page đang `CONNECTED`.

```text
GET /api/pages/{pageId}
```

Chi tiết Page đang connected.

### Webhook

```text
GET /base/facebook/webhooks
```

Facebook verify webhook callback.

```text
POST /base/facebook/webhooks
```

Nhận webhook event.

### Inbox/message

```text
GET /api/pages/{pageId}/conversations
```

Danh sách conversation theo Page.

```text
GET /api/pages/{pageId}/conversations/{conversationId}/messages
```

Danh sách message trong conversation.

```text
POST /api/pages/{pageId}/messages
```

Gửi message text/image URL.

### Post

```text
POST /api/pages/{pageId}/posts
```

Đăng post text/image URL.

```text
GET /api/pages/{pageId}/posts
```

Lịch sử post đã lưu.

### Disconnect

```text
POST /api/pages/{pageId}/disconnect
```

Ngắt kết nối Page, ghi log, chặn thao tác sau disconnect.

### Policy/Data deletion

```text
GET /privacy-policy
```

Policy public cho Meta App Review.

```text
GET /data-deletion
```

Hướng dẫn data deletion public.

```text
POST /api/data-deletion
```

Nhận yêu cầu data deletion.

## 16. Gateway Graph API đã thêm/sử dụng

- `FacebookUserGateway`
  - `GET /me`
- `FacebookOAuthGateway`
  - `GET /oauth/access_token`
- `FacebookPageGateway`
  - `GET /me/accounts`
  - `POST /{pageId}/subscribed_apps`
- `FacebookMessageGateway`
  - `POST /me/messages`
- `FacebookMessengerProfileGateway`
  - `GET /{psid}`
- `FacebookPublishGateway`
  - `POST /{pageId}/feed`
  - `POST /{pageId}/photos`

## 17. Cấu hình local/ngrok đã dùng khi test

Ngrok trong lúc test:

```text
https://<NGROK_URL>
```

Local redirect URI:

```properties
facebook.redirect-uri=https://<NGROK_URL>/api/facebook/oauth/callback
```

Webhook callback URL trên Meta Dashboard:

```text
https://<NGROK_URL>/base/facebook/webhooks
```

Verify token:

```text
PFIVShXYCLAwNYtV
```

Privacy Policy URL:

```text
https://<NGROK_URL>/privacy-policy
```

Data Deletion URL:

```text
https://<NGROK_URL>/data-deletion
```

## 18. Postman

Đã tạo:

```text
postman_collection.json
```

Collection có biến:

- `baseUrl`
- `email`
- `password`
- `token`
- `pageId`
- `conversationId`
- `psid`
- `verifyToken`

Các request chính:

- `01 Demo Login`
- `02 Get OAuth URL`
- `03 List Connected Pages`
- `04 Page Detail`
- `05 Webhook Verify`
- `06 List Conversations`
- `07 List Messages`
- `08 Send Text Message`
- `09 Send Image Message`
- `10 Publish Text Post`
- `11 Publish Image Post`
- `12 List Post History`
- `13 Data Deletion JSON`
- `14 Privacy Policy`
- `15 Data Deletion Page`
- `16 Disconnect Page`
- `17 Publish After Disconnect Should Fail`

## 19. Các comment/ghi chú trong code/config

Đã thêm/giữ các comment có ý nghĩa:

- `application.properties`
  - ghi chú feature flags phase 1 App Review.
  - ghi chú demo reviewer auth.
  - ghi chú token encryption secret phải override ở môi trường thật.
- `RestTemplateConfig`
  - ghi chú read timeout 30s vì Facebook có thể fetch attachment URL trước khi response.
- `FacebookMessengerProfileService`
  - ghi chú profile enrichment là best-effort, không được làm fail message persistence.
- `FbUser`
  - comment alias backward-compatible cho service dormant phase sau.
- `application-local.properties`
  - dòng redirect URI localhost cũ đang được comment:
    ```properties
    #facebook.redirect-uri=http://localhost:8080/facebook/login
    ```
  - redirect URI đang dùng ngrok mới.

## 20. Test đã chạy

Lệnh:

```bash
mvn test
```

Kết quả mới nhất:

```text
BUILD SUCCESS
Tests run: 19
Failures: 0
Errors: 0
Skipped: 0
```

Test hiện có:

- `FacebookOAuthServiceTest`
- `FacebookPageConnectServiceTest`
- `FacebookMessagePersistenceServiceTest`
- `FacebookMessengerInboxServiceTest`
- `FacebookMessengerProfileServiceTest`
- `FacebookMessengerServiceTest`
- `FacebookDataDeletionServiceTest`
- `FacebookPageAccessGuardTest`
- `FacebookPageDisconnectServiceTest`
- `FacebookPagePublishServiceTest`
- `TokenCryptoServiceTest`

## 21. Những điểm đã test thủ công bằng ngrok/Postman

Đã test:

- Demo login lấy token.
- OAuth URL.
- Facebook callback qua ngrok.
- Exchange code.
- Connect Page thành công.
- DB có `fb_users`, `fb_pages`, `fb_page_clients`.
- List connected pages.
- Page detail.
- Webhook verify.
- Inbound message tạo conversation/message.
- Conversation có `senderPsid`.
- Bổ sung logic lấy `senderName`/`senderAvatarUrl` cho message mới.
- Send text message.
- Send image message đã xử lý lỗi timeout bằng tăng read timeout và đổi URL ảnh mẫu.

## 22. Những việc còn lại đề xuất

### 22.1. Trước khi submit App Review

- Thay ngrok bằng domain ổn định hoặc reserved ngrok domain.
- Viết nội dung Privacy Policy/Data Deletion thật hơn thay vì string placeholder.
- Chuẩn bị video/instruction reviewer.
- Kiểm tra app mode/tester/Page role trong Meta Dashboard.
- Kiểm tra các permission cần review:
  - `pages_show_list`
  - `pages_manage_metadata`
  - `pages_messaging`
  - `pages_manage_posts`
  - `pages_read_engagement`

### 22.2. Hardening sau test

- Chuyển OAuth state store từ in-memory sang Redis nếu deploy nhiều instance.
- Không cache token plain trong Redis, hoặc mã hóa token cache.
- Dọn cảnh báo SLF4J multiple providers.
- Cấu hình Mockito agent nếu muốn sạch test log.
- Thêm migration SQL chính thức nếu không muốn dùng `spring.jpa.hibernate.ddl-auto=update`.

### 22.3. Phase sau App Review

- Scheduled post worker.
- AI auto post.
- Ads/Marketing API.
- Insight/report nâng cao.
- Comment/reaction moderation.
- Video/Reels/Story upload.
