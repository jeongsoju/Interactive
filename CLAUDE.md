# CLAUDE.md — Interactive (Pleos 차량용 앱)

## 1. 이 프로젝트
현대차그룹 차량용 앱 개발 공모전(2026) 출품작. Pleos Connect(현대차 인포테인먼트, Android Automotive 기반)에서 돌아가는 앱.

컨셉: **"나를 기억하고 말 거는 차"** — 차가 여행을 기록하고, 그 기록으로 게임을 하고, 먼저 말을 걸고, 추억으로 남겨준다.

핵심 기능 4가지
1. **자동 여행 기록**: 경로, 정차 지점, 체류 시간, 탑승 인원, 연료비를 자동 기록
2. **땅따먹기(영토 게임)**: 행정동 단위. 주차해서 일정 시간 머물면 내 땅. 최근 30일 더 오래·자주 머문 사람이 주인. 거리·속도는 경쟁 요소에서 제외(과속 유도 금지)
3. **말 거는 차(P 카드)**: 기어가 P가 되는 순간 카드 표시 ("처음 와본 곳이에요. 영토 +1", "1년 전 오늘, 강릉에 갔어요", "이번 여행 연료비 4만 2천 원"). 주행 중에는 아무것도 띄우지 않음
4. **폰 연동 여행 영상**: 폰 사진의 위치·시간과 차 기록을 맞춰 슬라이드 영상 생성 (mobile 모듈, 후순위)

일정: 11/14 기획서 제출(차 앱 핵심 동작 + APK), 12/3 PT 시연(전체 기능 동작)

## 2. 사용자에 대해 (중요)
- 사용자는 **코딩을 처음 해본다.** 컴퓨터 기본 지식은 있다.
- 작업을 끝낼 때마다 **무엇을 했는지, 왜 했는지를 비전공자가 이해할 수 있게 3~5줄로** 설명한다. 전문 용어는 처음 쓸 때 괄호로 풀어쓴다.
- 사용자가 직접 확인해야 하는 것(에뮬레이터 화면, Virtual Console 조작 등)은 **클릭 단위로** 알려준다.
- 사용자에게 코드를 직접 고치라고 하지 않는다. 필요한 수정은 직접 한다.

## 3. 개발 환경
- Windows, Android Studio, Kotlin
- 프로젝트 루트: `C:\dev` (이 폴더 자체가 프로젝트. 다른 파일을 두지 않는다)
- 모듈: `automotive`(차 앱), `mobile`(폰 앱), `shared`(공용 코드)
- minSdk 34, 에뮬레이터: **Pleos Connect Emulator v2.1.0 (API 34)**, AVD 이름 `Pleos_Connect_v2.1.0`
- adb: `"$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"`
- 화면 구조: 일반 화면 방식(`MainActivity` + Jetpack Compose). Car App Library 템플릿 방식은 2026-10-25에 걷어냈다. 구조를 다시 바꿀 때는 **먼저 이유와 계획을 사용자에게 설명하고 승인받는다.**
- 주행 중 화면: `distractionOptimized`를 선언하지 않는다 → 기어 D(주행 규제)에서는 시스템이 "주행 중에는 사용할 수 없습니다."로 앱을 가린다

## 4. Pleos 개발 규칙 (반드시 지킨다)
- **Pleos API는 문서에 있는 것만 쓴다.** 함수·클래스 이름을 추측해서 만들지 않는다. 필요하면 `docs/pleos/`의 문서를 먼저 읽고, 없으면 https://document.pleos.ai/docs/pleos-only/ 에서 해당 페이지를 가져와 `docs/pleos/`에 저장한 뒤 쓴다.
- Vehicle SDK: Maven 저장소 `https://nexus-playground.pleos.ai/repository/maven-releases/`, 의존성 `ai.pleos.playground:Vehicle` (버전은 문서 기준)
- 지금 쓸 수 있는 SDK: **Vehicle State(조회), NaviHelper**. Vehicle Control(제어), STT, TTS, FusedLocation은 아직 신청 불가 → 쓰지 않는다
- 권한은 쓰는 영역만 매니페스트에 선언 (`pleos.car.permission.CAR_DRIVE`, `CAR_SEATS`, `CAR_ENERGY` 등). 필요 없는 권한 추가 금지
- Vehicle SDK는 **비동기**(onComplete/onFailed 콜백). 콜백이 끝나기 전에 `release()` 호출 금지
- 기능 사용 전 `checkXXXCapabilities`로 지원 여부 확인
- **구글 서비스(GMS) 없는 환경**: Google Maps SDK, Firebase 중 GMS 의존 기능, Play Services 쓰지 않는다. 외부 라이브러리는 에뮬레이터에서 동작 확인 후 채택
- 주행 규제: `CarUxRestrictionsManager`로 상태를 받아 **주행 중에는 화면 요소를 숨긴다.** 카드·게임 화면은 기어 P에서만 표시
- 화면: 세로형(창 1개)과 가로형(창 2개) 모두에서 깨지지 않게
- 성능: RAM 400MB 이하, 주행 중 CPU 5% 미만, 실행 10초 이내, 버튼 반응 2초 이내
- 보안: API 키·비밀값 하드코딩 금지, 통신은 HTTPS(TLS 1.2+), 배포 빌드는 디버깅 끔

## 5. 설계 원칙
- 차량 데이터는 인터페이스 `VehicleDataSource`로 분리하고 구현체를 두 개 둔다
  - `RealVehicleDataSource`: Pleos SDK에서 실제로 읽음
  - `ReplayVehicleDataSource`: JSON 파일의 가짜 여행을 시간순으로 재생 (에뮬레이터는 Virtual Console로 속도·기어를, `adb emu geo fix`로 위치 한 점을 넣을 수 있지만 경로를 따라 움직이는 재생은 안 되므로 개발·시연 모두 이걸 쓴다)
- 데이터 구조: 여행(Trip) → 정차 지점(Stop: 위치, 시작/끝 시각, 체류 시간, 탑승 인원) → 영토(Territory: 행정동 코드, 주인, 점수)
- 정차 감지: 기어 P → 정차 시작, D → 정차 끝, 일정 시간 이상이면 체류로 기록
- 기어는 AAOS 표준 `CarPropertyManager`의 `GEAR_SELECTION`(권한 `android.car.permission.CAR_POWERTRAIN`)으로 읽는다. 주행 중 화면 숨기기는 `CarUxRestrictions`로 판단한다. Pleos `Gear` API는 문서에 없어 쓰지 않는다(설명회 질문 대상)
- 연료비(2026-10-25 결정): 기본은 **주행거리 ÷ 공인연비 × 유가** 추정
  - 주행거리: Odometer 누적 거리의 출발·도착 차이
  - 공인연비: 설정에서 차종 선택
  - 유가: 앱 기본값, 설정에서 변경 가능. 오피넷 실시간 유가는 나중 단계(가입·키 필요)
  - 화면에는 "(추정)"을 표시하고, 차가 연비 값을 주면 그 값을 우선 쓴다
- 기록은 먼저 기기 안에 저장하고(오프라인 우선), 서버 동기화는 나중 단계에서 붙인다

## 6. 작업 방식
1. 한 번에 하나의 작은 기능만 한다
2. 시작 전에 **계획을 짧게 보여주고** 진행한다
3. 끝나면 직접 확인한다: 빌드 → 에뮬레이터 설치 → `adb exec-out screencap -p`로 화면 캡처해서 결과 확인
4. 동작이 확인되면 사용자에게 알리고, 승인받은 뒤 커밋한다. 커밋 메시지는 한국어 한 줄
5. 에러가 나면 원인을 쉬운 말로 설명하고 고친다
6. 단계를 시작할 때 docs/progress.md를 먼저 읽고, 단계가 끝나 커밋할 때 progress.md의 상태·커밋 번호·"다음에 할 일"을 같이 고친다.
7. 대화에서만 정한 계획이나 결정은 없다. 정한 것은 반드시 spec.md나 progress.md에 적는다.

## 7. 코드와 답변을 짧게
- 새 코드를 쓰기 전에 이미 있는 코드, Kotlin·Android 표준 기능, 이미 쓰는 라이브러리로 되는지 먼저 확인한다.
- 요청받은 것만 만든다. "나중에 쓸지도 모르는" 클래스·옵션·설정은 만들지 않는다. (VehicleDataSource 분리는 예외)
- 새 라이브러리를 추가하기 전에 이유를 한 줄로 말하고 승인받는다.
- 같은 파일을 반복해서 읽지 않는다. 필요한 부분만 찾아 읽는다.
- 답변에 코드 전문을 붙이지 않는다. 바꾼 파일 이름과 바꾼 이유만 말한다.
- 단계 설명은 2장 규칙(3~5줄)을 지킨다.
