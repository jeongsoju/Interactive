# Pleos 문서 요약 모음

Pleos 공식 문서(https://document.pleos.ai/docs/pleos-only/)를 개발 참고용으로 정리한 노트입니다.
수집일: 2026-10-25

> 원문을 그대로 복사한 것이 아니라 **구조와 핵심을 정리한 요약**입니다.
> 클래스·함수·권한 이름, 버전, 링크는 원문 그대로 옮겼습니다.
> 예제 코드 전체나 세부 문구가 필요하면 각 파일 맨 위의 원문 링크를 확인하세요.

| 파일 | 내용 |
|---|---|
| [getting-started.md](getting-started.md) | 프로젝트 생성, 에뮬레이터, Vehicle SDK 가져오기, 첫 API 호출 |
| [vehicle-sdk-intro.md](vehicle-sdk-intro.md) | Vehicle SDK 구조: 진입점, 도메인 19개, API 4종류 |
| [vehicle-permission.md](vehicle-permission.md) | 도메인별 권한(`pleos.car.permission.*`) |
| [driving-restriction-development-guide.md](driving-restriction-development-guide.md) | 주행 규제: `CarUxRestrictionsManager` 연동 |
| [app-structure-screen-model.md](app-structure-screen-model.md) | 화면 구조: portrait/landscape, 창 1개/2개, 포그라운드·백그라운드 |
| [vehicle-sdk-sample-app.md](vehicle-sdk-sample-app.md) | Sample App 사용법, Door Position 예제 흐름, 문제 해결 |
| **사전 조사 (2026-10-25, spike 브랜치)** | |
| [ui-constraints.md](ui-constraints.md) | ⭐ UI 설계할 때 알아야 할 제약 사항 총정리 |
| [virtual-console.md](virtual-console.md) | Virtual Console 여는 법(클릭 단위), 기어·좌석 바꾸기, 위치 넣기 |
| [screen-size.md](screen-size.md) | 앱 창 크기 실측(1682×1210dp), FormFactorConsole 사용법 |
| [ui-design-guide.md](ui-design-guide.md) | 글자 크기 표, 버튼 크기, 색 토큰, 아이콘, 터치 기준 |
| [vehicle-data-api.md](vehicle-data-api.md) | 기어·좌석·연비·주행거리·위치 읽는 법과 에뮬레이터 결과 |
| [map-options.md](map-options.md) | GMS 없이 쓸 지도 후보와 확인 방법 |
| [template-vs-activity.md](template-vs-activity.md) | 템플릿 → 일반 화면 전환 장단점과 방법 |

## 우리 프로젝트와 관련해 눈여겨볼 점
- Vehicle SDK 버전 표기가 문서마다 다르다: 시작 가이드 의존성 예시는 `2.3.1`, SDK 소개·Sample App 기준은 `2.3.2`. **Maven에는 `2.0.3`, `2.3.2`만 있다 → `2.3.2` 사용** (2026-10-25 확인)
- 우리가 쓸 권한 후보: `CAR_DRIVE`(Gear·Odometer), `CAR_ENERGY`(EvBattery·Fuel), `CAR_SEATS`(Seat). 기어는 SDK의 `Gear` 도메인으로 읽히지만 공식 문서에는 없다 → [vehicle-data-api.md](vehicle-data-api.md)
- `release()`는 콜백이 끝난 뒤, 앱 종료 시점(`onDestroy()` 등)에 호출한다.
- 화면 방향: `screenOrientation`을 안 정하면 portrait(창 1개), `landscape`로 정하면 창 2개 영역을 쓴다.

## 아직 저장하지 않은, 다음에 필요할 문서
- 도메인 객체 획득: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/obtain-domain-object
- Capability API: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/check-capability
- Get API: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/get-property
- Callback: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/callback
- 에러 코드: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-error-code
- API Reference: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/entry
- Virtual Console: https://document.pleos.ai/docs/pleos-only/app-test-guide/virtual-console-guide
