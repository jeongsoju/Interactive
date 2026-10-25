package com.example.interactive.shared

// 명세서(docs/spec.md)의 숫자를 모아 둔 곳. 값을 바꿀 때는 여기만 고친다.
object AppConfig {
    /** 이만큼 P로 머물면 '체류'로 확정하고 영토 점수를 갱신한다 */
    const val STAY_MIN_MINUTES = 10

    /** 앱이 다시 켜질 때 마지막 P가 이보다 오래됐으면 여행을 끝낸다 */
    const val TRIP_END_IDLE_HOURS = 3

    /** 주행 중 경로 점을 저장하는 간격 */
    const val LOCATION_INTERVAL_SECONDS = 60

    /** 영토 점수를 계산하는 기간 */
    const val SCORE_WINDOW_DAYS = 30

    /** 영토 점수: 방문한 날 하루당 더하는 점수 */
    const val VISIT_DAY_BONUS = 30

    /** 영토 탭: 가장 센 경쟁자 점수가 내 점수의 이 비율 이상이면 '빼앗길 수 있는 땅'으로 본다 (명세서에 숫자가 없어 정한 값) */
    const val AT_RISK_SCORE_RATIO = 0.8

    /** 영토 점수: 체류 1회에서 인정하는 최대 시간 */
    const val MAX_STAY_MINUTES_PER_VISIT = 240

    /** 기어가 P가 된 뒤 이 시간 안에 P 카드를 띄운다 */
    const val CARD_SHOW_WITHIN_MILLIS = 1_000L

    /** 기본 유가(휘발유, 원/L). 설정에서 바꿀 수 있다 */
    const val DEFAULT_FUEL_PRICE_WON_PER_LITER = 1_850

    /**
     * 공인연비 기본값(km/L). 설정에서 차종을 고르기 전(13단계)까지 연료비 추정에 쓴다.
     * 값은 임시이며, 13단계에서 차종별 공인연비로 대체한다.
     */
    const val DEFAULT_FUEL_EFFICIENCY_KM_PER_LITER = 12.0
}
