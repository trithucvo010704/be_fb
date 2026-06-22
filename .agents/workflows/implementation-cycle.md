# Workflow: Implementation Cycle

## Stage 1: Preparation (Chuẩn bị)
- Hiểu yêu cầu của USER.
- Tạo folder task tại `task-be-level/task-[ID]`.
- Khởi tạo file `task-todo.md` và `acceptance_criteria.md`.

## Stage 2: Development (Phát triển)
- Thực hiện coding theo từng bước trong `task-todo.md`.
- Viết Unit Test song song.
- Tự rà soát (Self-review) dựa trên bộ nguyên tắc Clean Code.

## Stage 3: Verification (Xác minh)
- Submit Pull Request (giả định) cho Bob.
- Trả lời các chất vấn kỹ thuật từ Bob.
- Sửa đổi nếu Bob yêu cầu.

## Stage 4: Maintenance (Bảo trì & Fix Bug)
- Nếu có Bug report từ Bob:
    - Phân tích nguyên nhân gốc rễ (Root cause).
    - Cập nhật `task-todo.md` để bổ sung bước fix.
    - Thực hiện fix và cập nhật Unit Test để tránh tái phát lỗi (Regression test).
