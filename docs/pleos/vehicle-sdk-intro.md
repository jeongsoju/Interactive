# Vehicle SDK 소개 (요약)

원문: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/intro
수집일: 2026-10-25 · 원문을 정리한 요약

- 서드파티 앱은 Vehicle SDK로 Pleos Connect의 Vehicle Service에 연결해 차량 정보를 조회·제어한다.
- Vehicle Service는 Android Automotive OS 위에서 동작한다.
- 문서 기준 버전: **Vehicle SDK v2.3.2**, **Vehicle Service v2.3.21**

## 진입점과 도메인 구조
- 도메인 객체는 직접 생성하지 않는다. 진입점 `Vehicle`의 `getXxx()`(예: `getDoor()`, `getBrake()`)로 얻는다.
- 도메인마다 API 4종류:
  - **Capability** (`checkXXXCapabilities`): 기능 지원 여부, 허용 값 범위 확인
  - **Get**: 현재 값 조회
  - **Set**: 값 변경
  - **Callback**: 상태 변경 리스너 등록
- 연결 `initialize()`, 해제 `release()`
- 차량 통신은 비동기 IPC. 결과는 `onComplete` / `onFailed` 람다로 받는다.
- 대기 중인 비동기 콜백이 끝나기 전에 `release()`를 호출하지 않는다.

| 도메인 | 설명 |
|---|---|
| Brake | 브레이크 페달, 주차 브레이크 상태 조회 |
| CarInfo | 차량 기본 정보(외형 크기, 제조사, 연식 등) 조회 |
| Display | 인포테인먼트 표시 단위(예: 온도 단위) 조회·설정 |
| Door | 문 열림/닫힘, 잠금 상태 조회·제어 |
| DrivingMode | 주행 모드, 크루즈 컨트롤 정보 조회 |
| EvBattery | 전기차 배터리, 충전 상태, 전비 조회 |
| Fuel | 연비 등 연료 정보 조회 |
| HVAC | 공조(온도, 풍량, 습도 등) 조회·제어 |
| Light | 조명 상태 조회, 앰비언트 라이트 제어 |
| Odometer | 누적 거리, 트립 거리·시간, 거리 단위 조회 |
| Safety | 차량 경고 정보 조회 |
| Seat | 시트 위치, 각도, 기능 조회·제어 |
| SideMirror | 사이드미러 열선 상태 조회·설정 |
| Steeringwheel | 스티어링 휠 상태·열선 조회·제어 |
| Tire | 타이어 상태 조회 |
| Toll | 하이패스(ETCS) 음성 안내 볼륨 조회·설정 |
| TurnSignal | 방향지시등 상태 조회 |
| Window | 창문 상태 조회·제어 |
| Wiper | 와이퍼 상태 조회·제어 |

## 에뮬레이터 설정 (순서대로)
1. [소프트웨어 개발 환경 요구사항 확인](https://document.pleos.ai/docs/connect/guide/getting-started/application-development/setup-connect-sdk-emulator#소프트웨어-개발-환경)
2. [Pleos Connect Emulator 설치](https://document.pleos.ai/docs/connect/guide/getting-started/application-development/setup-connect-sdk-emulator#installing-the-pleos-connect-emulator)
3. [AVD 생성 및 실행](https://document.pleos.ai/docs/connect/guide/getting-started/application-development/setup-connect-sdk-emulator#setting-up-the-emulator)
4. [Pleos Connect Virtual Console 설정](https://document.pleos.ai/docs/connect/guide/getting-started/application-development/use-vehicle-emulator)
5. [마이크 접근 설정](https://document.pleos.ai/docs/connect/guide/getting-started/application-development/configure-pleos-connect-emulator#마이크-접근-설정)
6. [Cold boot 설정](https://document.pleos.ai/docs/connect/guide/getting-started/application-development/configure-pleos-connect-emulator#cold-boot-설정)

## 다음 단계
1. [도메인 객체 획득](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/obtain-domain-object): 도메인 객체 얻기, Get/Set/Callback/Capability 호출
2. [Permission](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-permission): 도메인별 권한
3. [Error code](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-error-code): 에러 코드, `onFailed` 처리
4. [API Reference](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/entry): API 명세
5. [Vehicle SDK Sample App 사용 가이드](https://document.pleos.ai/docs/pleos-only/tutorial/vehicle-sdk-sample-app)
