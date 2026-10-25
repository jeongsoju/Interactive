# Vehicle SDK Sample App 사용 가이드 (요약)

원문: https://document.pleos.ai/docs/pleos-only/tutorial/vehicle-sdk-sample-app
수집일: 2026-10-25 · 원문을 정리한 요약 (전체 permission 목록·세부 설명은 원문 참고)

## Sample App 화면
- 진입 화면: 왼쪽 차량 기능 분류(Domain), 오른쪽 기능 카드 목록
- 상단 `SDK initialized`: 초기화 호출이 예외 없이 반환됐다는 뜻일 뿐, Service 연결 완료나 기능 사용 가능을 뜻하지 않음
- 기능 상세 화면
  - Vehicle support: 지원 상태, 영역별 값 범위 (`Check support`)
  - Current value: 현재 값 (`Refresh`), Area 선택
  - Change value: 값 입력 후 `Apply change`
  - Live updates: `Start updates` / `Stop updates`
  - API activity: `Expand` / `Collapse`

## 학습 목표
- 대상: Android 경험은 있고 차량용 앱은 처음인 개발자
- 목표: Pleos Connect 전용 AVD에서 Sample App 실행, Door Position 중심으로 Vehicle SDK 연동 익히기

## 기준 환경
| 항목 | 기준 |
|---|---|
| Sample App | `1.0.0-sdk35` |
| Vehicle SDK | `2.3.2` |
| Vehicle Service | `2.3.22` |
| AVD | `Pleos_Connect_v2.1`, Android 14 |
| JDK | 17 |
| Gradle wrapper | `8.10.2` |
| Android Gradle Plugin | `8.8.2` |
| Kotlin | `2.0.0` |
| Android SDK | compileSdk / targetSdk 35 |
| Sample App minSdk | 23 |

- 빌드·설치: `./gradlew assembleDebug`, `./gradlew installDebug` (APK: `app/build/outputs/apk/debug/`)
- 제품 앱(Pleos Playground 배포)은 Android API 28 이상 요구사항 적용

## Door Position으로 익히는 사용 흐름 (영역 `ROW_1_LEFT`)
| 작업 | Typed API |
|---|---|
| 지원 범위 확인 | `checkDoorPositionCapabilities` |
| 현재 상태 조회 | `getCurrentDoorPosition` |
| 변경 알림 구독 | `registerDoorPosition` |
| 상태 변경 요청 | `setDoorPosition` |
| 변경 알림 해제 | `unregisterDoorPosition` |

1. 지원 범위 확인: 실패하면 변경 요청을 하지 않는다
2. 현재 상태 조회: `null`/`unavailable`은 유효한 값을 못 받은 상태
3. API activity의 `Complete` 의미는 작업마다 다르다. Listener 등록·해제의 `Complete`는 Service 완료 응답이 아님
4. 구독 상태 표시와 실제 알림 수신은 따로 확인
5. 변경 요청: 완료 콜백의 값은 요청값이지 재조회 결과가 아님
6. 변경 결과는 재조회나 변경 알림으로 확인. 요청 완료만으로 성공을 단정하지 않음
7. 알림 해제는 등록한 영역으로. 화면 이동만으로는 해제되지 않음

## 내 앱에 적용하기
| 단계 | 문서 |
|---|---|
| SDK 의존성 추가 | [Vehicle SDK 연동 가이드](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/project-setup) |
| 권한 선언 | [권한 선언 가이드](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-permission) |
| 도메인 객체 획득 | [도메인 객체 획득](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/obtain-domain-object) |
| 알림 등록·해제 | [Callback 가이드](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/callback) |
| API 명세 | [Vehicle SDK API 상세 정보](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/entry) |

- 쓰는 typed API의 최소 권한만 선언
- Listener는 등록 때와 같은 객체·area로 해제
- 사용이 끝나면 `Vehicle.release()`

## 문제 해결
- 권한 선언 위치: `app/src/main/AndroidManifest.xml`
- Door Position 조회·구독: `CAR_DOORS`, 변경: `CONTROL_CAR_DOORS`
- 이 권한들은 실행 중 승인 창이 뜨지 않음

| 증상 | 확인 사항 |
|---|---|
| `Initialization failed` / Service 연결 오류 | 전용 AVD, Vehicle Service 설치 상태 |
| `Vehicle support`가 `Unsupported` | 실제 미지원이거나 확인 요청 실패 → `Check support` 재시도 |
| 현재값 `null` / `unavailable` | 지원 여부·선택 영역 확인 후 재조회 |
| 현재값 조회 `Failed` | 오류 메시지, 읽기 권한, 영역, Service 상태 |
| 변경 요청 거부·범위 오류 | 쓰기 권한, 쓰기 가능 여부, 값 범위 |
| 요청 완료 후 값 불일치 | 재조회·변경 알림 확인 |
| 구독했는데 알림 없음 | 등록 영역, 오류 메시지 |
| 해제 시 영역 오류 | 등록했던 영역 선택 후 `Stop updates` |

- 기능은 Supported인데 특정 영역 조회 실패(예: `area ID: 0x1 not supported`) → 기능 지원과 영역 사용 가능 여부는 따로 확인

## Sample App의 public permission (접두어 `pleos.car.permission.`)
- 읽기: `CAR_CLIMATE`, `CAR_CONNECT_VENDOR_CONTROL`, `CAR_DOORS`, `CAR_DRIVE`, `CAR_ENERGY`, `CAR_INFO`, `CAR_LIGHTS`, `CAR_MILEAGE`, `CAR_MIRRORS`, `CAR_POWERTRAIN`, `CAR_SAFETY`, `CAR_SEATS`, `CAR_SPEED`, `CAR_TOLL`, `CAR_TIRES`, `CAR_WINDOWS`, `READ_CAR_DISPLAY_UNITS`, `READ_CAR_STEERING`, `READ_ADAS_STATES`, `READ_WINDSHIELD_WIPERS`
- 제어: `CONTROL_CAR_CLIMATE`, `CONTROL_CAR_DOORS`, `CONTROL_CAR_DRIVE`, `CONTROL_CAR_ENERGY`, `CONTROL_CAR_INFO`, `CONTROL_CAR_LIGHTS`, `CONTROL_CAR_MIRRORS`, `CONTROL_CAR_SAFETY`, `CONTROL_CAR_SEATS`, `CONTROL_CAR_TOLL`, `CONTROL_CAR_WINDOWS`, `CONTROL_WINDSHIELD_WIPERS`
- `PRIVATE_*` 권한은 예제에 없음. 제품 앱은 최소 권한만 선언
- 원문에는 이보다 긴 목록이 있을 수 있음

## 관련 문서
- [API 호환성 및 가용성](https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/api-compatibility-availability)
- [Pleos Connect Emulator 설치 및 실행](https://document.pleos.ai/docs/connect/guide/getting-started/application-development/setup-connect-sdk-emulator)
