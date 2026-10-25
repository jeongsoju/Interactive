# Pleos UI 디자인 가이드 기준 (버튼·글자·색·터치)

원문 (Pleos Connect UI Design Guide)
- 소개: https://document.pleos.ai/docs/connect/guide/docs-design/Introduction/intro
- Typography: https://document.pleos.ai/docs/connect/guide/docs-design/foundations/typography
- Colors: https://document.pleos.ai/docs/connect/guide/docs-design/foundations/colors
- Basic Button: https://document.pleos.ai/docs/connect/guide/docs-design/components/actions/basic-button
- Iconography: https://document.pleos.ai/docs/connect/guide/docs-design/foundations/contents-creation/iconography
- 주행 안전 설계: https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/driver-distraction-guideline

수집일: 2026-10-25
> 숫자 대부분이 문서 본문이 아니라 **그림 안에만** 있다. 그림을 직접 열어 읽은 값이다.
> "측정값"이라고 표시한 것은 그림의 도형 크기를 잰 값이라 공식 수치가 아닐 수 있다.

## 핵심 원칙 (소개 페이지)
- **Easy & Safe**: 주행 중 시각·인지 부담 최소화. 영상·애니메이션·자동 스크롤 글자 피하기. 한 손으로 쉽게 조작
- **Consistency**: Pleos 기본 UI와 같은 아이콘·용어·조작 방식
- **Glanceability**: 1~2초 안에 이해할 핵심 정보만. 입력 반응은 **0.25초 이내**, 더 걸리면 "처리 중" 표시
- **대비**: 글자·아이콘·이미지 대비 **최소 4.5:1**
- **글자**: 짧게. 핵심 정보 글자 높이 **5mm 이상**, 보조 글자 **3mm 이상**. 한 줄 **80자 이내**, 문단 **3줄 이내**
- **아이콘**: 국제적으로 통용되는 아이콘 + 글자 라벨. 터치에 햅틱/소리 피드백
- **알림**: 운전자에게 관련 있을 때만. 광고 금지
- **유연성**: 주간/야간 모드, Gleo AI 음성, 다국어, 다양한 화면 크기·비율 지원

## 글자 (Typography)
- 시스템 글꼴: **Asta Sans** (한국어·영어, Google Fonts에서 받을 수 있음)
- 최소 글자 크기: **12sp 이상 권장** (160dpi에서 12sp = 12dp = 12px)
- 최소 글자 높이: **18px**(권장 18~20px), 줄 간격 약 **1.5배 이상**

| 단계 | 크기 | 줄 높이 | 굵기 |
|---|---|---|---|
| Headline Large | 60 | 80 | Extra Bold, Bold |
| Headline Medium | 56 | 72 | Extra Bold, Bold |
| Headline Small | 48 | 64 | Extra Bold, Bold |
| Title Large | 40 | 52 | Extra Bold, Bold, Regular |
| Title Medium | 36 | 44 | Extra Bold, Bold, Regular |
| Title Small | 32 | 40 | Extra Bold, Bold, Regular |
| Body Large | 30 | 38 | Strong, Normal |
| Body Medium | 28 | 36 | Strong, Normal |
| Body Small | 26 | 34 | Strong, Normal |
| Label Medium | 24 | 28 | Normal |
| Label Small | 20 | 24 | Normal |

- 단위는 그림에 표기되어 있지 않다. 에뮬레이터가 160dpi(1dp=1px)이므로 **sp/dp 그대로 쓰면 된다**고 보고 있다(추정).

## 버튼 (Basic Button)
- 구성: Root(컨테이너), Label, Icon(Prefix), Icon(Suffix)
- 상태: **Pressed**(누르는 중), **Disabled**(조작 불가), **Loading**(로더 표시 + 조작 불가)
- 크기 3종 Large / Medium / Small. **측정값**:

| 크기 | 높이 | 모서리 반경 |
|---|---|---|
| Large | 100 | 32 |
| Medium | 80 | 24 |
| Small | 64 | 16 |

- 같은 계열 버튼: Toggle button, Text button, Icon button, Keypad
- **최소 터치 영역과 버튼 간격 수치는 문서에 없다.** 주행 안전 문서도 "디자인 가이드를 따르라"고만 한다.
  - 임시 기준: 가장 작은 버튼(Small)이 64 높이이고 아이콘 기본 격자가 48×48dp다. 그래서 **터치 영역은 최소 64dp, 간격은 최소 16dp**로 잡고, 공식 수치를 Pleos에 문의하는 것을 권장한다(우리 판단).

## 아이콘
- 기본 격자 **48×48dp**
- Filled / Outlined 중 상황에 맞게. 마스터 아이콘 하나를 만들어 크기별로 조정

## 색 (Colors)
- **HEX 값은 공개되어 있지 않다.** 토큰 이름과 라이트/다크 대응만 나와 있다.
- 기본 색 묶음: Grayscale, White Alpha, Gray Alpha, Static Color(Light/Dark, 테마와 상관없이 고정)
- 시스템 색 묶음: Background, Button, Controller, Climate, Dropdown, Dim, Driving, Fields, Gleo AI, Icon, Informative, Keyboard, Media, Overlay, Phone, Regulation, Surface, Switch, Text

| 토큰 | 라이트 | 다크 |
|---|---|---|
| background_primary | basic_50 | basic_50 |
| background_secondary | basic_00 | basic_00 |
| background_popup | basic_00 | basic_100 |
| surface_basic | basic_00 | basic_50 |
| surface_low | basic_50 | basic_00 |
| surface_high | basic_100 | basic_100 |
| surface_accent | basic_200 | basic_200 |
| button_basic_enabled / pressed / disabled | alpha_dark_50 / alpha_dark_100 / alpha_light_200 | 같음 |
| button_filled_enabled / pressed / disabled | basic_600 / basic_700 / alpha_dark_50 | basic_600 / basic_500 / alpha_dark_50 |
| button_switch_enabled / pressed / disabled | basic_00 / alpha_light_500 / alpha_light_300 | alpha_dark_200 / alpha_dark_200 / alpha_dark_300 |
| text_dark_primary~quaternary | static_dark_100~400 | static_light_100~400 |

- 그림으로 보면 전체적으로 **흰색·회색 계열 무채색** 위주다. 라이트 모드는 흰 배경에 짙은 회색 버튼, 다크 모드는 검정 배경이다.
- 실제 HEX가 필요하면 Pleos에 디자인 토큰 파일을 요청하거나, 에뮬레이터 화면 캡처에서 색을 뽑아 근사값으로 쓴다.
