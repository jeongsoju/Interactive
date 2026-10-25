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

## 우리 프로젝트와 관련해 눈여겨볼 점
- Vehicle SDK 버전 표기가 문서마다 다르다: 시작 가이드 의존성 예시는 `2.3.1`, SDK 소개·Sample App 기준은 `2.3.2`. 실제로 붙일 때 Maven 저장소에서 사용 가능한 버전을 확인한다.
- 우리가 쓸 권한 후보: `CAR_DRIVE`(DrivingMode·Brake·Odometer), `CAR_ENERGY`(EvBattery·Fuel), `CAR_SEATS`(Seat). 기어(P/D) 상태를 어느 도메인·권한으로 읽는지는 API Reference(`/connect-sdk-pleos/Vehicle/entry`)를 추가로 확인해야 한다.
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
