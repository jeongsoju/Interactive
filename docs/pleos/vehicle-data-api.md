# 차량 데이터 읽기: 기어·좌석·연비·주행거리·위치

확인일: 2026-10-25 · Vehicle SDK `ai.pleos.playground:Vehicle:2.3.2` · 에뮬레이터 `RELEASE.release_connect_v2.1.0`

> **함수 정보를 어디서 가져왔는지**
> 공식 API 레퍼런스(https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/entry)는 함수 **이름 목록만** 있고 각 페이지 본문이 비어 있다.
> 그래서 함수 모양(매개변수·콜백 타입)과 권한은 문서에 적힌 Maven 저장소의 SDK 2.3.2 파일을 직접 열어 확인했다.
> Maven에 있는 버전은 `2.0.3`, `2.3.2`뿐이다. 시작 가이드에 적힌 `2.3.1`은 없다.

## 공통 사용법 (문서: obtain-domain-object)
```kotlin
val vehicle = Vehicle(context)   // ai.pleos.playground.vehicle.Vehicle
vehicle.initialize()
val seat = vehicle.getSeat()
seat.getCurrentSeatOccupancy(SeatArea(SeatValue.ROW_1_LEFT),
    { occupied -> /* onComplete */ },
    { e -> /* onFailed */ })
// 콜백이 다 끝난 뒤, 앱 종료 시점에
vehicle.release()
```
- Get 함수 모양: `getXxx(onComplete: (값) -> Unit, onFailed: (Exception) -> Unit)`
- 콜백은 **메인 스레드가 아닌 곳**에서 온다(실측). 화면을 바꿀 때는 메인 스레드로 넘겨야 한다.
- Gradle 설정
  - `settings.gradle.kts`의 repositories에 `maven { url = uri("https://nexus-playground.pleos.ai/repository/maven-releases/") }`
  - 의존성 `implementation("ai.pleos.playground:Vehicle:2.3.2")`
- SDK가 스스로 넣는 권한: `ai.pleos.playground.service.vehicle.VEHICLE_BINDING`. 필요한 Vehicle Service는 2.3.13 이상이다.

## 1. 기어 (P/R/N/D) — **결정: AAOS 표준 `GEAR_SELECTION` 사용** (2026-10-25)
근거: Pleos API 호환성 문서가 "AAOS Vehicle Property는 `VehiclePropertyIds`·`CarPropertyManager`로 런타임에 지원 여부를 확인해 쓰라"고 안내한다.
참고 문서: [VehiclePropertyIds](https://developer.android.com/reference/android/car/VehiclePropertyIds), [CarPropertyManager](https://developer.android.com/reference/android/car/hardware/property/CarPropertyManager)

| 항목 | 내용 |
|---|---|
| 빌드 설정 | `automotive/build.gradle.kts`의 `android { useLibrary("android.car") }` |
| 권한 | `android.car.permission.CAR_POWERTRAIN` (매니페스트 선언만, 실행 중 허용 창 없음) |
| 연결 | `Car.createCar(context)` → `getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager` |
| 지원 확인 | `getCarPropertyConfig(VehiclePropertyIds.GEAR_SELECTION)`가 null이 아니면 지원 |
| 변경 알림 | `registerCallback(callback, VehiclePropertyIds.GEAR_SELECTION, CarPropertyManager.SENSOR_RATE_ONCHANGE)` → `onChangeEvent(CarPropertyValue)`의 `value`(Int). 등록 직후 현재 값이 한 번 온다 |
| 값 | `VehicleGear.GEAR_PARK`(P), `GEAR_REVERSE`(R), `GEAR_NEUTRAL`(N), `GEAR_DRIVE`(D) |
| 정리 | `unregisterCallback(callback)` → `car.disconnect()` |
| 에뮬레이터 결과 | ✅ 지원됨. 시작 시 `P`. Virtual Console에서 D → 즉시 `D`, 다시 P → 즉시 `P` |

### 정차/주행 상태 (주행 중 화면 숨기기용)
| 항목 | 내용 |
|---|---|
| 연결 | `car.getCarManager(Car.CAR_UX_RESTRICTION_SERVICE) as CarUxRestrictionsManager` |
| 현재 값 | `currentCarUxRestrictions` |
| 변경 알림 | `registerListener { restrictions -> }` / `unregisterListener()` |
| 판단 | `isRequiresDistractionOptimization == true`면 주행 규제 중 → 화면 숨김 |
| 권한 | 필요 없음 |
| 에뮬레이터 결과 | ✅ P: `requiresDO=false, active=0x0` / D: `requiresDO=true, active=0x50`. 기어 변경과 거의 동시에(약 20ms 차이) 바뀜 |

- 역할 나누기(결정): **기어(P/D) → 정차 시작·끝 기록, P 카드 띄우기 / UX 규제 → 화면 숨기기**
- 참고: Pleos Vehicle SDK 2.3.2 안에도 `vehicle.getGear()`(`getCurrentGearSelectionState`, 권한 `pleos.car.permission.CAR_DRIVE`)가 있고 동작한다. 하지만 **공식 문서에 없어서 쓰지 않는다** → 설명회 질문 목록에 올림

### 설명회 질문 목록에 넣을 것
- Vehicle SDK 2.3.2에 있는 `Gear` 도메인(`getCurrentGearSelectionState`, `registerGearSelection`)이 문서에 없는데, 공식 지원 API인지?
- AAOS 표준 `CarPropertyManager`(`GEAR_SELECTION`, 권한 `android.car.permission.CAR_POWERTRAIN`)를 3rd-party 앱이 써도 심사에 문제가 없는지?

## 2. 좌석 탑승 여부
| 항목 | 내용 |
|---|---|
| 도메인 | `vehicle.getSeat()` → `Seat` |
| 조회 | `getCurrentSeatOccupancy(area: SeatArea, onComplete: (Boolean) -> Unit, onFailed: (Exception) -> Unit)` |
| 지원 확인 | `checkSeatOccupancyCapabilities(onComplete: (List<SeatArea>) -> Unit, onFailed)` |
| 변경 알림 | `registerSeatOccupancy(area, SeatOccupancyListener)`. 리스너는 `onSeatOccupancyUpdated(Boolean)` |
| 좌석 | `SeatArea(SeatValue.ROW_1_LEFT)` 등. `ROW_1~3` × `LEFT/CENTER/RIGHT` |
| 권한 | `pleos.car.permission.CAR_SEATS` |
| 에뮬레이터 결과 | ✅ 4개 좌석 모두 true/false로 읽힘. Virtual Console의 Seat Occupancy로 바꿀 수 있다 |
| 참고 | 안전벨트도 같은 방식: `getCurrentSeatBeltBuckledState(area, ...)` |

## 3. 연비 / 전비
에뮬레이터 차량은 전기차(아이오닉 계열)로 보인다.

| 함수 | 도메인 | 권한 | 에뮬레이터 결과 |
|---|---|---|---|
| `getCurrentFuelEconomy((Float)->Unit, onFailed)` | Fuel | `CAR_ENERGY` | ❌ ERROR_NOT_SUPPORTED_PROPERTY |
| `getCumulativeFuelEconomy(...)` | Fuel | `CAR_ENERGY` | ❌ not supported |
| `getFuelPercent((Int)->Unit, onFailed)` | Fuel | `CAR_ENERGY` | ❌ not supported |
| `getCurrentEvEfficiency((Float)->Unit, onFailed)` | EvBattery | `CAR_ENERGY` | ❌ ERROR_NOT_SUPPORTED_PROPERTY |
| `getEvBatteryStateOfCharge((Int)->Unit, onFailed)` | EvBattery | `CAR_ENERGY` | ✅ 100 (%) |
| `getRangeRemainingDistance((Float)->Unit, onFailed)` | Odometer | `CAR_DRIVE` | ✅ 500000.0 (화면 500km → **미터** 단위로 보임) |

- 그 밖에 Fuel의 `getInstantaneousFuelEconomy`, `getSinceChargeFuelEconomy`와 EvBattery의 `getInstantaneous/SinceCharge/CumulativeEvEfficiency`가 있다.
- **에뮬레이터에서는 연비·전비를 읽을 수 없다.** 연료비 카드는 Replay 데이터(가짜 여행)나 "거리 × 단가" 계산으로 만들어야 한다.
- 실제 차에서도 차종마다 다르므로 반드시 `check...Capabilities`로 먼저 확인한다.

### 실제 차(그랜저·아반떼)에서 Fuel 값이 나올까? (2026-10-25 조사)
- **공식 문서에는 차종별 지원표가 없다.**
  - API 호환성 문서: "Vehicle SDK는 차종에 따라 가용성이 다르며 `checkXXXCapabilities`로 확인"
  - Vehicle Data API 호환성 페이지(클라우드 API용)도 차종 이름 없이 "차종에 따라 다르다"고만 적혀 있다.
  - 확인한 문서: https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/api-compatibility-availability , https://document.pleos.ai/api-reference/vehicle-data-api/api-compatibility
- 공개 보도로는 Pleos Connect가 **더 뉴 그랜저에 처음 탑재**됐고, **디 올 뉴 아반떼**에도 들어간다(14.6″/12.9″ 화면). 둘 다 내연기관·하이브리드 모델이 있으므로 Fuel 도메인이 지원될 **가능성은 있다**. 다만 **문서로 확인된 것은 아니다.**
  - [현대차그룹 보도자료](https://www.hyundaimotorgroup.com/ko/news/hyundai-motor-group-redefines-in-vehicle-experience-with-pleos-connect-next-generation-infotainment-system), [OSEN 아반떼 기사](https://osen.co.kr/article/G1112829022)
- → **설명회 질문 목록**: "그랜저·아반떼 실차에서 Fuel(`getCurrentFuelEconomy`, `getFuelPercent`)과 Odometer 트립 값이 지원되는지, 차종별 지원표가 있는지"

### 연료비 추정 대안 (Fuel 값이 없을 때)
우선순위대로 시도하고, 실패하면 다음 방법으로 넘어간다(fallback).

| 순위 | 방법 | 필요한 값 | 상태 |
|---|---|---|---|
| 1 | 차가 주는 평균 연비로 계산: `거리 ÷ 연비(km/L) × 유가` | Fuel `getCurrentFuelEconomy` 또는 `getSinceChargeFuelEconomy` | 실차 지원 시 |
| 2 | 연료량 차이로 계산: `(출발 연료% − 도착 연료%) × 탱크 용량 × 유가` | `getFuelPercent` + 탱크 용량(사용자 설정) | 주유하면 계산이 틀어짐 |
| 3 | **공인 복합연비로 계산: `거리 ÷ 공인연비 × 유가`** | 누적 주행거리 차이(✅ 에뮬레이터에서 읽힘) + 차종 공인연비(설정 화면에서 선택, 예: 그랜저 2.5 가솔린) | **에뮬레이터·시연에서 바로 가능 → 기본안 추천** |
| 4 | GPS 경로 길이로 거리 대신 | 위치 기록 | 주행거리 값도 없을 때 |
| EV | `(출발 SoC − 도착 SoC) × 배터리 용량(kWh) × 전기 단가` | `getEvBatteryStateOfCharge`(✅ 읽힘) | 에뮬레이터 차량용 |

- **유가**: 기본값을 앱에 넣고 설정에서 바꿀 수 있게 한다. 실시간 유가가 필요하면 한국석유공사 **오피넷 API**를 쓸 수 있지만, **회원가입과 API 키가 필요하다** → 쓰기 전에 사용자 승인을 받는다.
- **표시 문구**: 실제 측정값이 아니면 "약 4만 2천 원 (추정)"처럼 **추정임을 표시**한다.
- **시연**: Replay 데이터에 거리·연비를 넣어 두면 화면은 항상 같은 값으로 재현된다.

## 4. 주행거리
| 함수 | 결과 |
|---|---|
| `getCurrentDistanceDriven((Float)->Unit, onFailed)` (누적 주행거리) | ✅ 0.0 |
| `getTripCurrentDrivingDistance((Float)->Unit, onFailed)` | ❌ not supported |
| `getTripCumulativeDrivingDistance`, `getTripSinceRefillDrivingDistance`, `getTripInstantaneousDrivingDistance` | 미시험 |
| `getTripCurrentDrivingTime((Int)->Unit, onFailed)` 등 시간 함수 | 미시험 |

- 도메인 `vehicle.getOdometer()`, 권한 `pleos.car.permission.CAR_DRIVE`
- 여행 거리는 **시작·끝 시점의 누적 거리 차이**로 계산하는 편이 안전하다. 어차피 우리 여행 기록 기준과도 맞는다.

## 5. 현재 위치
- Pleos의 **FusedLocation SDK는 아직 신청 불가**라서 쓰지 않았다(CLAUDE.md 규칙).
- 대신 Android 표준 `LocationManager`로 시험했다.
  - 권한 `android.permission.ACCESS_FINE_LOCATION` (실행 중 사용자 허용 필요)
  - 제공자: `passive`, `fused`, `gps`
  - `adb emu geo fix 128.8961 37.7519`로 위치를 넣자 세 제공자 모두 `37.7519, 128.8961`을 돌려줬다 ✅
- ⚠️ Pleos 하드웨어 접근 정책: "차량의 물리적 제어나 센서에 직접 접근 금지, SDK 플랫폼 API로". GPS를 표준 `LocationManager`로 읽는 것이 허용되는지는 문서에 명시가 없다 → FusedLocation 신청이 열리면 그쪽으로 바꾸는 게 안전하다.
- NaviHelper(3.0.5)는 목적지·경로 기능이다. 현재 좌표를 주는지는 문서에서 확인하지 못했다.

## 시험 방법 (spike 브랜치)
- 실험 화면: `automotive/src/main/java/com/example/interactive/spike/SpikeActivity.kt`
- 실행: `adb shell am start --user 10 -n com.example.interactive/.spike.SpikeActivity`
- 결과: `adb logcat -s SPIKE:I` (기어는 `AAOS_GEAR`, 주행 규제는 `UX` 줄)
- ⚠️ 에뮬레이터 사용자는 **user 10**이다. 권한 부여도 `adb shell pm grant --user 10 ...`로 해야 한다.
