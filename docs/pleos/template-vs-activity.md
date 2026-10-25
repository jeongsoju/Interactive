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

## 전환 계획 (2026-10-25 작성, 승인 전에는 실행하지 않음)

### 어디서 할까 → **main에서 새 브랜치 `feature/activity-ui`를 만들어서** (추천)
- main에서 바로 하지 않는 이유: 앱 뼈대를 통째로 바꾸는 큰 작업이다. 중간에 빌드가 깨져도 main은 항상 "설치되는 APK" 상태로 남아야 한다. 11/14 제출 전까지 안전장치가 된다.
- spike에서 하지 않는 이유: spike에는 실험 화면·실험 권한·MapLibre가 섞여 있다. 본 코드에 실험 흔적이 들어가면 나중에 정리가 어렵다. spike는 참고용으로만 두고, 필요한 코드(기어 읽기, 지도 설정)는 **옮겨 적는다**.
- 끝나면 확인 → 사용자 승인 → main에 합치기(merge)
- 참고: 로컬 main에는 GitHub에 아직 올리지 않은 커밋 1개(`269eb1f` 문서)가 있다. 브랜치를 만들기 전에 main부터 push할지 정한다.

### 바꿀 파일과 내용
| 단계 | 파일 | 내용 |
|---|---|---|
| 1 | `gradle/libs.versions.toml`, `automotive/build.gradle.kts` | Compose 켜기(이미 버전 항목 있음: compose BOM, material3, activity-compose). `kotlin-compose` 플러그인 적용. `androidx.car.app:app-automotive` 제거 |
| 2 | `automotive/.../MainActivity.kt` (새 파일) | `ComponentActivity` + Compose 화면. 우선 "Interactive" 제목과 기어 상태 글자만 있는 빈 화면 |
| 3 | `automotive/src/main/AndroidManifest.xml` | `CarAppActivity` 삭제 → `MainActivity`를 런처로. `android.software.car.templates_host` uses-feature 삭제. `appCategory="audio"` 삭제(템플릿 기본값, 우리 앱과 무관). `android.hardware.type.automotive`는 유지. **DO(`distractionOptimized`)는 선언하지 않음**(A안: 주행 중 시스템이 화면을 가림) |
| 4 | `shared/` | `MyCarAppService`·`MyCarAppSession`·`MyCarAppScreen`, `automotive_app_desc.xml`(template 사용 선언), 매니페스트의 CarAppService 등록 삭제. `androidx.car.app:app` 의존성 제거. 빈 공용 모듈로 두고 나중에 `Trip`/`Stop`/`Territory` 모델과 `VehicleDataSource`를 넣을 자리로 쓴다 |
| 5 | `mobile/build.gradle` | `androidx.car.app:app-projected`(안드로이드 오토용) 제거. mobile은 일반 폰 앱(여행 영상)이 될 예정이라 필요 없다 |
| 6 | `automotive/src/main/res/values/themes.xml` | 라이트/다크 둘 다 되는 기본 테마 |

### 이번 전환에 넣지 않는 것 (다음 작업으로)
- 지도, 기어 읽기, P 카드, Vehicle SDK 연결 → 뼈대 전환이 확인된 뒤 하나씩 추가 (CLAUDE.md "한 번에 하나의 작은 기능")

### 확인 방법
1. 빌드 → 에뮬레이터 설치 → 런처에서 앱 실행 → 캡처: Compose 화면이 1682×1210 창에 뜨는지
2. Virtual Console에서 기어 D → 캡처: "주행 중에는 사용할 수 없습니다."로 가려지는지
3. 다시 P → 앱 화면이 돌아오는지
4. 실행 시간(10초 이내)과 메모리 확인
5. FormFactorConsole 12.9″ 세로형(628×855)에서 깨지지 않는지

### 되돌리기
- 문제가 생기면 브랜치를 버리면 된다. main은 영향받지 않는다.
