package berryful.lounge.api.service.livecommerce

import berryful.lounge.api.data.AnalyzeContentReq
import berryful.lounge.api.data.AnalyzeContentRes
import berryful.lounge.api.data.ErrorRes
import berryful.lounge.api.externalApi.analyzeContentApi.AiReq
import berryful.lounge.api.externalApi.analyzeContentApi.AiRes
import berryful.lounge.api.externalApi.analyzeContentApi.AnalyzeContentApiClient
import berryful.lounge.api.utils.ApiResultCode
import berryful.lounge.api.utils.ErrorMessageCode
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class AnalyzeContentService(
    private val analyzeContentApiClient: AnalyzeContentApiClient,
) {
    fun analyzeContent(req: AnalyzeContentReq): Any {

        val content471 = mutableMapOf<Long, String>()
        content471[2700] = "데일리하면서 과하지않은 애굣살 메이크업❤"
        content471[386] = "8년째 쓰는 중! 남친이랑 같이 써요! 향 넘 조아서 바디워시랑 바디크림 롤러까지 사서 썼는데 바디워시랑 롤러는 단종됐더라구요ㅠㅠ단점은,, 향수 지속력이 약해요 ㅠㅠ 흑 롤러 다시 내주라쥬#키엘 #오리지널머스크"
        content471[415] = "겨울엔 #니베아 대용량 사고 시작!  틴케이스로 사서 만능으로 쓰긔라메* 크림이랑 성분 거의 유사하다는 썰이 있었죠,, 얼굴에도 씀니다 (피부타입=유전)"
        content471[11469] =
            "키핀터치 아이스 젤리 치크 블러셔_01 모브오로라\uD83D\uDC96오늘 가져온 블러셔는 수채화 발색의 딸기우유빛 컬러예요! 피부에 올리면 사랑스럽고 화사하게 발색돼서 봄맞이템으로 딱인 제품 같아요>_< 젤리블러셔라 꾹꾹 눌러서 모양변형도 가능하단 사실.. 특히 여쿨라에게 찰떡일만한 색상이라 강추드립니다♥︎#키핀터치 #모브오로라 #핑크 #블러셔"
        content471[8565] = "겨울엔 항상 몸이 건조하다보니 이거 사용해요! 오일감이 느껴지긴 하지만 효과가 좋아서 계속 사용중..✨✨"
        content471[8510] = "스킨케어 마지막에 여드름 부위에 필요한만큼 발라주는 시카크림으로 라벨영 티트리 시카크림 추천드려요! 화장 하지 마시고 꾸준히 이거 열심히 발랐을때 효과 엄청 크게 본 아이..\uD83D\uDE0A"
        content471[8471] = "핑크빛도는 글리터.,. ㅜㅜ 데일리에 은은하게 빛나도록 하기에 최고!"
        content471[8491] = "클리오 킬커버 에어리핏 컨실러 #진저쿠션은 너무 매트하고 고커버라 부담스러워서 컨실러로 구매했어요!홍조나 다클서클은 얇게깔면 완벽하게는 아니지만 이쁘게 톤 보정되서 매일 쓰고 있어요-!"
        content471[9766] = "홀리카 테일레스팅 샤프 펜 라이너 01 잉크블랙 추천드려요!!원래키스미만 쓰다가 이번에 저렴한걸로 바꿨는데 생각보다 굵기조절도 잘되고 발색도 짱짱한데다 지속력도 만족스럽더라구요"
        content471[10198] = "글리터 올리전이랑 삼각존 깨끗하게 하기좋아요!#삐아"

        val content526 = mutableMapOf<Long, String>()
        content526[7159] = "맥립스틱 데인저러스+페리페라 잉크무드드롭틴트 조합 존예보스예요ㅜㅜ 매트한 느낌으로 맥 발라주고 그 위에 촉촉하게 얹어주면 세상 조녜"
        content526[7575] = "저도 봄웜인데 제가 진짜 잘 쓰는 제품이에요!롬앤 어도러블 임미다"
        content526[8185] = "이걸 사용했었는데 꾸준히 사용했을 경우에는 조금씩 옅어지는 것을 볼 수 있었어요! 그런데 까먹고 넘어가는 날이 많아지면 다시 원래대로 되돌아오는 것 같긴 했어요.. 꾸준히 바를 자신이 있으시다면 추천드려요!"
        content526[8294] = "다크서클이 짙은 편인데 루나 컨실러가 진짜 잘 가려져요! 붉은끼나 잡티도 이거 하나만 발라도 커버가 돼서 넘 조은 제품\uD83E\uDD0D"
        content526[8297] = "피카소 FB17번 파데 브러쉬 사용합니다☺️ 정말 정말 유명한 브러쉬죠 파데 두껍게 얹는거 안 좋아해서 브러쉬로 바르는걸 선호하는 편인데 정말..피카소는 역시 다르더라구요!얇게 발리고 피부에 챡 달라붙는 사용감이 넘 좋아요"
        content526[8843] = "저는 어반디케이 올나이터 사용 중입니다!이거 냄새가 좀 술냄새긴 한데ㅠㅠ그래도 전 적응돼서 괜찮아요 ㅎㅎㅎ제가 써본 것 중에는 이게 가장 지속력 좋은 거 같아요!!#어반디케이 #픽서 #메이크업픽서 #자현티"
        content526[8510] = "스킨케어 마지막에 여드름 부위에 필요한만큼 발라주는 시카크림으로 라벨영 티트리 시카크림 추천드려요! 화장 하지 마시고 꾸준히 이거 열심히 발랐을때 효과 엄청 크게 본 아이..\uD83D\uDE0A"
        content526[8977] = "메디힐 티트리 에센셜 마스크"
        content526[9199] = "구찌 블룸 아쿠아 디 피오리 추천해요! 저도 작년에 선물받은 향수인데 시원하고 라이트한 향이라 데일리로 자주 뿌리고 있고, 더운 날이나 특히 여름에 부담스럽지 않아서 좋습니다 ☺️"
        content526[9541] = "속눈썹 폼 이후로 쓰고 있는 건데 괜찬항요. 그런데 제품안에 속눈썹이 들어가있는 걸 목격했어요! 위생적으로 관리하려면 팁에 속눈썹 붙어있는지 보고 넣어야겠다라구요!"


        val content536 = mutableMapOf<Long, String>()
        content536[1527] = "저는 메이크업 방식도 좋지만 가끔 샵에서 메이크업 받는것처럼 피부 베이스 표현부터 속눈썹까지 더더욱 꼼꼼하게 해보면서 기분 전환도 하는것같아요ㅎㅎㅎ 샵 촬영하듯 말이에요"
        content536[1770] = "라카 여쿨님들이 쓰시기에 뽀용뻐용할거같아요 ㅎㅎ 색감 너무 이쁘거든요 ㅠㅠ #라카 #블러셔"
        content536[1940] = "저도 갈뮽은 아니고 갈솦인데 뮤트하게 메이크업하는 거 되게 좋아하는 편이거든요!아이메이크업 뮤트하게 하고 블러셔는 이거 두 개 중에 발라주면 너무 회끼 돌지 않으면서도 짱 예뻐요 !! #맥  #맥미네랄라이즈블러쉬  #웜소울  #릴리바이레드  #릴리바이레드러브빔치크  #여신빔  #가을웜톤블러셔  #가을웜톤  #가을뮤트"
        content536[2171] = "클리오 프아팔 13호 좋아용!! 쿨톤 모브 색상이여서 너무 예쁘더라구요~!! 회끼가 있어서 확실한 쿨톤 팔레트입니당☺️ #쿨톤팔레트 #클리오"
        content536[2521] = "지금 올리브영 BOH 브랜드 수분크림 쓰는 중입니다 ! ☺️수분크림 중 저렴한 편이 속하는 크림이어서 큰 기대를 안 했는데,꾸덕한 촉촉함이 있는 그런 느낌이 드는 수분크림이에용 \uD83D\uDE3B#BOH #촉촉한수분크림  #수분크림추천"
        content536[2984] = "저는 다크 덜심할때는 아이레놀쓰고 심한날은 아이레놀s써요"
        content536[3459] = "시어버터 드라이 스킨 핸드 밤 추천드려요!시어버터가 고함량으로 들어있고 시간이 지날수록 경도가 더욱 형성되면서 꾸덕꾸덕꾸덕~~!!! 해지면서 촉촉함이 오랫동안 유지된답니당\uD83E\uDD2D\uD83E\uDD2D❤️ #핸드크림 #핸드밤 #시어버터"
        content536[3834] = "아토베리어 365크림 추천드려요! 장벽 크림인데 무겁지도 않고 가볍게 쓰기 딱 좋은 크림이랍니다\uD83D\uDE0A"
        content536[4181] = "삐아 진저블랑이예요 저거만 단독으로 발라도 살구살구하고 눈화장끝나용"
        content536[4835] = "달바 워터풀 에센스 선크림!이거 진짜 촉촉하고 좋아요!!비건 인증까지 받아서 너무 좋은 거 같고, 건성이 쓰기에 딱이었어요!#선크림 #건성선크림"

        var content = mutableMapOf<Long, String>()
        content = when (req.memberId) {
            471L -> content471
            526L -> content526
            536L -> content536
            else -> return ErrorRes(message = "${req.memberId} No learning model found")
        }

        var avg_calculation_time = 0L
        var avg_algorithm_error = 0.0
        val result: MutableList<AiRes> = mutableListOf()
        content.forEach {
            val az = analyzeContentApiClient.analyzeContent(AiReq(req.memberId, it.value)) as AiRes
            avg_calculation_time += (az.calculation_time_end - az.calculation_time_start)
            avg_algorithm_error += (1 - az.ai_feedback)
            result.add(
                AiRes(
                    member_id = req.memberId,
                    post_id = it.key,
                    calculation_time_start = az.calculation_time_start,
                    calculation_time_end = az.calculation_time_end,
                    ai_feedback = az.ai_feedback
                )
            )
        }

        return AnalyzeContentRes(
            AiRes = result,
            avg_calculation_time = avg_calculation_time/10.0,
            avg_algorithm_error = avg_algorithm_error/10.0
        )
    }

    fun learningModel(id: Long, trainData: MultipartFile, testData: MultipartFile): Any {
        return analyzeContentApiClient.learningModel(id, trainData, testData)
            ?: return ApiResultCode(ErrorMessageCode.ENTITY_NULL.code)
    }
}