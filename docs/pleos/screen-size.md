# Pleos 앱 화면 크기 (실측 + FormFactorConsole 안내)

관련 문서
- 폼 팩터 테스트 가이드: https://document.pleos.ai/docs/pleos-only/app-test-guide/form-factor-test-guide
- 앱 구조 및 화면 모델: [app-structure-screen-model.md](app-structure-screen-model.md)
- Display Layout: https://document.pleos.ai/docs/connect/guide/docs-design/getting-started/layout

확인일: 2026-10-25 · 에뮬레이터 `RELEASE.release_connect_v2.1.0`

## 기본 에뮬레이터 화면 (실측)
density가 160dpi라서 **1dp = 1px**이다.

| 영역 | px (가로×세로) | dp | 비고 |
|---|---|---|---|
| 전체 디스플레이 | 2560×1440 | 2560×1440 | 가로형, 160dpi |
| Driving view (왼쪽, 앱이 못 가림) | 853×1300 | 853×1210(앱 기준) | 좌표 (0,0)–(853,1300) |
| **앱 창** | **1682×1210** | **1682×1210** | 좌표 (853,90)–(2535,1300) |
| 상태바 (위) | 높이 90 | 90 | |
| GNB (아래, 공조·앱 버튼) | 높이 140 | 140 | 1300–1440 |

- 앱 창의 smallestWidth는 1440dp, 크기 분류는 `xlrg`, 방향은 `land`, uiMode는 `car`다.
- `screenOrientation`을 지정하지 않은 화면과 `landscape`로 지정한 화면 **둘 다 1682×1210**으로 똑같이 떴다.
  - 문서에는 "지정 안 하면 portrait(창 1개)"라고 되어 있지만, 이 기본 에뮬레이터는 앱 창 영역이 하나뿐이라 차이가 나지 않았다.
- 시스템 로그상 우리 앱은 `slotNo=0, slotWeight=2`로 배치됐다. 앱 영역이 2칸이고 그중 2칸을 다 쓴다는 뜻으로 보인다.

## 세로형/가로형(창 1개/2개) 크기 → FormFactorConsole 필요
- 12.9" Portrait, 14.6" Landscape 같은 **다른 차량 화면**은 FormFactorConsole로 가상 디스플레이(`DISPLAY2`)를 만들어 확인한다. 이 앱이 dp·DPI·px 값을 화면에 직접 보여준다고 문서에 나와 있다.
- 공식 문서 본문에는 화면 형태별 숫자 표가 없다.
- FormFactorConsole 앱은 에뮬레이터에 **이미 설치되어 있다**: `ai.pleos.connectwebconsole/.FormFactorConsoleActivity`
- 하지만 가상 디스플레이를 만들려면 PC 쪽 준비가 필요하다.
  1. **Go 1.22 이상** 설치 (현재 PC에 없음)
  2. `host-display-bridge.zip` 다운로드: https://nexus-playground.pleos.ai/repository/raw-releases/host-display-bridge/1.0.0/host-display-bridge.zip
  3. 압축을 푼 폴더에서 `go run . --avd "Pleos_Connect_v2.1.0"` 실행 (창 닫지 말 것, 8095·8096 포트 사용)
  4. 에뮬레이터에서 FormFactorConsole 실행 → **Display preset** 선택 → **Orientation** `Portrait`/`Landscape` → **가상 디스플레이 생성** → **앱 목록 실행**으로 우리 앱 실행
  5. 끝낼 때: **가상 디스플레이 제거** → bridge 창에서 `Ctrl+C` → `adb forward --remove tcp:8095`
- 1~2번(설치·다운로드)은 사용자 승인 후 진행한다.

## 화면 형태 종류 (Display Layout 문서)
| 형태 | 설명 |
|---|---|
| Connect-S (Standard) | 기본 크기. 17", 14.6", 12.9"(이전 Connect-L) |
| Connect-W (Wide) | Connect-S보다 넓음. 주로 고급 차종 |
| Connect-W-Extended | 넓은 화면, 차량 상태에 따라 높이가 늘어남 |
| Connect-WF (Waterfall) | Wide 계열, 해상도는 Wide보다 낮음 |
