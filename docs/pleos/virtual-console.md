# Pleos Connect Virtual Console 사용법 (정리 + 실제 확인)

원문
- 셋업: https://document.pleos.ai/docs/connect/guide/getting-started/application-development/use-vehicle-emulator
- 사용 가이드: https://document.pleos.ai/docs/pleos-only/app-test-guide/virtual-console-guide

수집·확인일: 2026-10-25 · 에뮬레이터 `RELEASE.release_connect_v2.1.0`에서 실제로 동작 확인

## 준비 조건
- Pleos Connect Emulator **v2.1.0 이상**이 실행 중일 것
- PC에서 `adb` 명령을 쓸 수 있을 것

## 여는 방법 (클릭 단위)
1. Android Studio에서 **Device Manager**를 열고 `Pleos_Connect_v2.1.0` 옆의 ▶ 버튼을 눌러 에뮬레이터를 켠다. 홈 화면이 나올 때까지 기다린다.
2. Windows 시작 버튼을 누르고 `powershell`을 입력한 뒤 **Windows PowerShell**을 연다.
3. 아래 명령을 붙여넣고 Enter를 누른다. 결과에 `emulator-5554  device`가 보이면 연결된 상태다.
   ```
   & "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices
   ```
4. 아래 명령을 붙여넣고 Enter를 누른다. 에뮬레이터 안의 콘솔을 PC 브라우저로 연결하는 명령이다. `8088`이 출력되면 성공이다.
   ```
   & "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" forward tcp:8088 tcp:8088
   ```
5. Chrome이나 Edge 주소창에 `http://127.0.0.1:8088/`을 입력한다. **Pleos Connect Virtual Console** 화면이 열린다.
   - 에뮬레이터를 껐다 켜면 4번부터 다시 해야 한다.

## 화면 구성 (실제 확인)
- 위쪽: 주행 시각화(Highway 테마). 기어가 P일 때 왼쪽 위 **Theme** 버튼으로 Highway/Forest를 바꿀 수 있다.
- 아래쪽: **ACCEL**(가속 페달), **BRAKE**(브레이크), **STEERING**, SYSTEMS(GEAR·SPEED 표시), CLIMATE, BODY 패널.
  - 화면 안내 문구: "Select D + hold ACCEL". 기어를 D로 두고 ACCEL을 누르고 있으면 속도가 올라간다. **속도 시뮬레이션이 있다**는 뜻이다.
- 왼쪽의 **Emulation Controls** 카드: 차량 값을 직접 바꾸는 패널이 열린다.

## 자주 쓸 조작 (클릭 단위)
### 기어 바꾸기
1. **Emulation Controls** 카드를 클릭한다.
2. 왼쪽 목록에서 **Motion**을 누르거나, 위 검색창에 `Gear`를 입력한다.
3. **Gear Selection** 카드에서 **P / R / N / D** 중 하나를 누른다. 카드 오른쪽 위에 현재 기어(예: `Park`)가 표시된다.
4. 에뮬레이터 왼쪽 위의 `P R N D` 표시가 바뀌었는지 확인한다.

### 좌석 탑승 바꾸기
1. Emulation Controls → **Convenience** → **Seat Occupancy** 카드
2. 좌석(운전석/동승석/뒷좌석 좌·우)을 고르고 **Empty** 또는 **Occupied**를 누른다.

### 그 밖의 카드 (도메인별)
| 도메인 | 카드 |
|---|---|
| Vehicle | Notification 1 |
| Motion | Gear Selection, Brake Pedal, Odometer, Steering Angle, Steering Wheel Heat |
| ADAS | Cruise Control State, Cruise Target Speed |
| Convenience | ETCS Power, ETCS Scenario, Door Position, Door Lock, Trunk / Frunk Position, Cabin Lights Switch, Reading Lights, Ambient Brightness, Ambient Color, Exterior Lights Mode, High Beam, Hazard Lights, Stop Lights, Door Lights, Turn Signal State, Window Position, Window Lock, Seat Occupancy, Windshield Wiper Switch, Glove Box |
| Safety | Telltales |
| Energy | Remaining Range, Battery Energy, Battery SOC, Battery Capacity, Charge Current Limit, Charge Percent Limit, Charge Switch, Charge Port |
| Climate | HVAC Power, Air Conditioning, HVAC Auto, HVAC Dual, Air Recirculation, Fan Speed, Fan Direction, Defroster, Target Temperature, Seat Temperature, Air Cleaner |

- **연료(Fuel)·위치 카드는 없다.** 위치는 adb로 넣는다 (아래 참고).

## 위치 넣기 (Virtual Console 아님, adb)
```
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" emu geo fix 128.8961 37.7519
```
- 순서가 **경도, 위도**인 점에 주의 (예시는 강릉)

## 실제로 확인한 동작 (2026-10-25)
- Gear Selection에서 D를 누르면 앱이 읽는 기어 값이 `GEAR_DRIVE`로 바뀌었다.
- 동시에 차 주행 상태가 parked → idling으로 바뀌고 주행 규제가 켜졌다.
- 그러자 DO(주행 중 사용 최적화) 선언이 없는 화면은 **"주행 중에는 사용할 수 없습니다."** 화면으로 가려졌다.
- 다시 P를 누르면 원래대로 돌아왔다.

## 문제 해결
1. 에뮬레이터가 켜져 있는지 확인
2. `adb devices`로 연결 확인
3. `adb forward tcp:8088 tcp:8088` 다시 실행
4. PC의 8088 포트를 다른 프로그램이 쓰고 있지 않은지 확인
5. 브라우저에서 `http://127.0.0.1:8088/` 새로고침
