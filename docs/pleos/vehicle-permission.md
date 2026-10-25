# Vehicle SDK 권한 (요약)

원문: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-permission
수집일: 2026-10-25 · 원문을 정리한 요약 (매니페스트 선언 예시 코드는 원문 참고)

- Vehicle SDK API는 앱 매니페스트에 필요한 Pleos 차량 권한을 선언해야 호출할 수 있다.
- 각 API의 `@RequiresPermission` 애너테이션과 상수 정의는 `PleosPermission` 레퍼런스에 있다.
- 상수 접두어
  - `CAR_*`: 도메인 데이터 읽기(get)
  - `CONTROL_CAR_*`: 도메인 기능 제어(set)
- 매니페스트 표기: `pleos.car.permission.{상수 이름}` (예: `pleos.car.permission.CAR_DRIVE`)

## 도메인별 대표 읽기 권한
| 도메인 | 권한 |
|---|---|
| Door | `CAR_DOORS` |
| Window, Wiper | `CAR_WINDOWS` |
| HVAC | `CAR_CLIMATE` |
| DrivingMode, Brake, Odometer | `CAR_DRIVE` |
| EvBattery, Fuel | `CAR_ENERGY` |
| CarInfo, Display | `CAR_INFO` |
| Light, TurnSignal | `CAR_LIGHTS` |
| SideMirror | `CAR_MIRRORS` |
| Safety, Tire | `CAR_SAFETY` |
| Seat | `CAR_SEATS` |
| Toll | `CAR_TOLL` |

## 주의
- API에 따라 다른 권한이 필요할 수 있다 → 호출 전 함수의 `@RequiresPermission` 확인
- 앱이 쓰는 도메인 권한만 선언한다
- 권한이 없으면 `onFailed`로 `Exception`이 온다. `VehicleException`이면 `ERROR_SECURITY_PERMISSION`에 해당할 수 있다 → [에러 코드 가이드](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-error-code)
- 이 권한들은 실행 중 사용자 승인 창이 뜨지 않는다(매니페스트 선언 방식) — Sample App 가이드 기준
