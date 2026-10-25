# 템플릿 방식 → 일반 화면 방식 전환 검토

작성일: 2026-10-25 · **아직 바꾸지 않았음 (검토만)**

## 지금 구조 (템플릿 방식 = Car App Library)
- `automotive/AndroidManifest.xml`: `androidx.car.app.activity.CarAppActivity`가 런처 화면. `distractionOptimized=true`. `android.software.car.templates_host` 필수(required)
- `shared` 모듈: `MyCarAppService` → `MyCarAppSession` → `MyCarAppScreen`(MessageTemplate "Hello world!"), 카테고리 `POI`
- 화면은 앱이 그리지 않는다. "목록·메시지·격자" 같은 **정해진 틀(템플릿)**에 글자·아이콘만 넘기면 차 시스템이 그린다.

## 일반 화면 방식 (Activity + Jetpack Compose 또는 View)
- 앱이 화면을 직접 그린다. 지도, 카드, 게임 판, 애니메이션 모두 자유롭다
- 실험에서 이미 확인: spike의 일반 `Activity`가 Pleos 앱 창(1682×1210)에 정상적으로 떴다

## 장단점
| | 템플릿 (지금) | 일반 화면 |
|---|---|---|
| 지도 | 내비·POI 전용 템플릿 + 특별 권한 필요. 앱 고유의 지도 표현은 불가능 | MapLibre 등을 자유롭게 사용 |
| 카드·게임 UI | 불가능 (정해진 틀만) | 자유 |
| 주행 규제 | 시스템이 자동 처리 | **직접 처리**: `CarUxRestrictionsManager` 구독, DO 선언 여부 결정 |
| 화면 크기 대응 | 자동 | 직접: 창 1개·2개, 여러 화면 형태 테스트 |
| 디자인 일관성 | 자동으로 시스템 모양 | Pleos 디자인 가이드(글자 크기·색 토큰)를 직접 맞춰야 함 |
| 개발 난이도 | 쉬움 (기능 적음) | 더 많지만 Compose로 충분히 가능 |
| 심사 리스크 | 낮음 | 규제 처리를 잘못하면 심사에서 걸릴 수 있음 |

**결론(권장): 일반 화면 방식으로 바꾸는 것이 맞다.** 핵심 기능인 지도·땅따먹기·P 카드가 템플릿으로는 만들 수 없다.

## 주행 규제 처리 방식 두 가지 (실험으로 확인한 사실 기반)
- **A. DO 선언 안 함** (`distractionOptimized` 메타데이터 없음)
  - 기어 D(주행 상태)가 되면 시스템이 앱 화면을 **"주행 중에는 사용할 수 없습니다."**로 자동으로 가린다 (실측)
  - P로 돌아오면 다시 보인다
  - 우리 컨셉("주행 중에는 아무것도 안 띄움")과 그대로 맞는다. 구현이 가장 단순하다
- **B. DO 선언함** (`distractionOptimized=true`)
  - 주행 중에도 앱이 보인다. 대신 `CarUxRestrictions`를 받아 직접 숨기거나 줄여야 한다
  - 주행 중 "기록 중" 같은 아주 단순한 표시를 보이고 싶을 때만 필요하다
- 실측 규제 값: parked `0x0`, idling·moving `0x50` (영상 금지 `NO_VIDEO` + 설정 금지 `NO_SETUP`). 기어 D에서 정차 중(idling)이어도 규제가 걸린다
- 여행 **기록**은 화면이 아니라 백그라운드(Foreground Service)에서 해야 한다. 화면이 가려져도 기록은 계속되어야 하기 때문이다

## 바꾸는 방법 (승인 후 진행할 계획)
1. `automotive` 모듈에 Compose 설정 추가 (`libs.versions.toml`에 이미 compose 항목이 있음)
2. `MainActivity`(ComponentActivity + Compose) 만들고 런처로 지정
3. 매니페스트 정리
   - `CarAppActivity`, `templates_host` uses-feature, `com.android.automotive` 메타데이터의 템플릿 설정 제거
   - `android.hardware.type.automotive`는 유지
   - DO는 A안이면 선언하지 않는다
4. `shared` 모듈의 `MyCarApp*` 3개 파일은 삭제한다. 공용 코드(데이터 모델, `VehicleDataSource`) 자리로 바꾼다
5. 빌드 → 설치 → P/D 전환하며 캡처 확인
6. 이 과정에서 `appCategory="audio"`(템플릿 기본값)도 맞는 값으로 바꿔야 한다
