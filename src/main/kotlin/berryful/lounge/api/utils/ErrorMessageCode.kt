package berryful.lounge.api.utils

enum class ErrorMessageCode(val code: Int, val message: String) {

    // common
    ENTITY_NULL(-1, "만족하는 객체가 없습니다."),
    OK(0, "ok"),
    REQUEST_SIGNUP(1, "가입이 필요한 회원입니다."),
    BAD_REQUEST(2, "요청 데이터가 잘못되었습니다."),
    NOT_FOUND_MEMBER(3, "회원 정보를 찾을 수 없습니다."),
    MEMBER_STATUS_LEAVE(4, "탈퇴한 회원입니다."),
    MEMBER_STATUS_SUSPENDED(5, "관리자에 의해 정지된 회원입니다."),
    SNS_TYPE_MISMATCH(6, "sns 타입 입력이 잘못 되었습니다."),
    REQUIRED_TO_AGREE(7, "필수 약관 동의를 체크해야 합니다."),
    INCLUDED_FORBIDDEN(8,"금지된 단어가 포함되어 있습니다."),
    INVALID_PHONENUMBER(9,"올바른 전화번호 형식이 아닙니다."),
    SNS_VERIFY_EXCEPTION(10,"SNS 인증 오류입니다."),

    // CRM 100
    NOT_FOUND_CERT(101, "발행된 인증번호가 없습니다."),
    NUMBER_INCORRECT(102, "인증번호가 틀렸습니다."),
    NUMBER_EXPIRED(103, "인증번호가 만료되었습니다."),
    EMAIL_CANNOT_BE_MODIFIED(104, "이미 인증된 메일주소 입니다."),
    DUPLICATE_NICKNAME(105, "이미 사용중인 닉네임 입니다."),
    TOKEN_EXPIRED(106, "토큰 기한이 만료 되었습니다."),
    NOT_FOUND_NOTIFICATION(107, "알림 정보를 찾을 수 없습니다."),
    TOKEN_INVALID(108, "토큰 정보가 유효하지 않습니다."),
    INVALID_NICKNAME(109, "닉네임은 2자 이상 12자 이하의 한글, 영문, 숫자로만 가능합니다."),
    INVALID_EMAIL(110, "올바른 이메일 형식이 아닙니다."),
    FORBIDDEN_NICKNAME(111, "금지된 닉네임 입니다."),
    ALREADY_BLOCKED_MEMBER(112, "이미 차단된 회원 입니다."),
    ALREADY_UNBLOCKED_MEMBER(113, "이미 해제된 회원 입니다."),
    FOLLOW_MEMBER_BLOCKED(114, "팔로우 하려는 회원이 내가 차단된 회원입니다."),
    DUPLICATE_PHONENUMBER(115, "이미 사용중인 전화번호 입니다."),

    //LOUNGE 200
    NOT_FOUND_ARTICLE(201, "게시물 정보를 찾을 수 없습니다."),
    NOT_FOUND_POST(202, "포스트 정보를 찾을 수 없습니다."),
    NOT_FOUND_CLIP(203, "클립 정보를 찾을 수 없습니다."),
    NOT_FOUND_COMMENT(204,"댓글 정보를 찾을 수 없습니다."),
    NOT_FOUND_REPLY(205,"대댓글 정보를 찾을 수 없습니다."),
    NOT_FOUND_REPORT(206,"신고 내역을 찾을 수 없습니다."),

    ARTICLE_TYPE_MISMATCH(207, "articleType이 다릅니다."),
    NOT_HAVE_PERMISSION(208, "본인이 작성한 게시물이 아닙니다."),
    CANNOT_DELETE_POST(209, "해당 포스트에 업로드 된 클립이 존재합니다."),

    ALREADY_THUMBS_UP(210, "이미 좋아요를 누른 컨텐츠 입니다."),
    CANNOT_CANCEL_THUMBS_UP(211, "좋아요를 취소할 수 없습니다."),
    BLOCKED_USER_CLIP_UPLOADED(212, "차단한 유저의 클립이 업로드 되어있습니다."),

    REQUIRED_TO_TITLE(213, "제목을 입력해 주세요."),
    REQUIRED_TO_CONTENT(214, "본문을 입력해 주세요."),
    REQUIRED_TO_CLIPURL(215, "클립의 동영상 주소를 입력해 주세요."),

    ALREADY_ADOPT_CLIP(216, "이미 채택된 클립이 있습니다."),
    CANNOT_DELETE_ADOPT_CLIP(217, "채택된 클립은 7일간 삭제할 수 없습니다."),
    CANNOT_ADOPT_CLIP(218, "본인이 업로드한 클립은 채택할 수 없습니다."),

    NOTIFICATION_TYPE_MISMATCH(219, "notificationType이 다릅니다."),
    REQUIRED_TO_CATEGORY(220, "카테고리를 선택해 주세요"),

    NOT_FOUND_CHALLENGE(221,"챌린지 정보를 찾을 수 없습니다."),
}

data class ApiResultCode(
    val resultCode: Int,
)

data class ApiResultBoolean(
    var result: Boolean
)