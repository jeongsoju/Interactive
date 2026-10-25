# Vehicle SDK 2.3.2 API 메모 (6단계에서 확인한 것)

문서 사이트의 API 레퍼런스 본문이 비어 있어서, 내려받은 SDK(`Vehicle-2.3.2.aar`)의 클래스 목록(javap)으로 이름을 확인했다. 2026-10-25.

- Maven 저장소에는 `2.0.3`, `2.3.2`만 있다(시작 가이드의 `2.3.1`은 없음). 우리는 `2.3.2`.
- `Vehicle(context)` → `initialize()` → `getOdometer()` / `getSeat()` → … → `release()` (패키지 `ai.pleos.playground.vehicle`)
- Odometer: `checkCurrentDistanceDrivenCapabilities(onComplete: (CapabilityConfig)->Unit, onFailed)`, `getCurrentDistanceDriven(onComplete: (Float?)->Unit, onFailed)`, `registerDistanceDriven(DistanceDrivenListener)` (`onDistanceDrivenUpdated(Float?)`)
- Seat: `checkSeatOccupancyCapabilities(onComplete: (List<SeatArea>)->Unit, onFailed)`, `getCurrentSeatOccupancy(area, onComplete: (Boolean?)->Unit, onFailed)`, `registerSeatOccupancy(area, SeatOccupancyListener)` (`onSeatOccupancyUpdated(Boolean?)`)
- 리스너는 `VehicleListener.onFailed(Exception)`도 구현해야 한다.
- SDK에 `Vehicle.getGear()`(Gear 도메인)가 있지만 문서에 없다. CLAUDE.md 규칙대로 쓰지 않고, 기어는 AAOS `CarPropertyManager`로 읽는다(설명회 질문).
- 거리 값(`Float`)의 단위는 km다(Virtual Console Odometer에 9277 → 앱 9277.0 km로 확인).
