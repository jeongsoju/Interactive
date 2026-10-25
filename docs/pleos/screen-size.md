# Pleos 앱 화면 크기 (실측 + FormFactorConsole)

관련 문서
- 폼 팩터 테스트 가이드: https://document.pleos.ai/docs/pleos-only/app-test-guide/form-factor-test-guide
- 앱 구조 및 화면 모델: [app-structure-screen-model.md](app-structure-screen-model.md)
- Display Layout: https://document.pleos.ai/docs/connect/guide/docs-design/getting-started/layout

확인일: 2026-10-25 · 에뮬레이터 `RELEASE.release_connect_v2.1.0`

## 화면 형태별 앱 창 크기 (FormFactorConsole 프리셋)
출처: 에뮬레이터 안 FormFactorConsole의 프리셋 표(`window-policy.js`). 공식 문서 본문에는 이 숫자가 없다.
"앱 창" = FormFactorConsole이 가상 디스플레이를 만들 때 쓰는 앱 영역 크기다.

| 화면 형태 | 기기 해상도 | DPI | 세로형(Portrait, 창 1개) dp | 가로형(Landscape, 창 2개) dp |
|---|---|---|---|---|
| Connect-S 17″ | 2560×1440 | 160 | **840×1184** | **1700×1184** |
| Connect-S 14.6″ | 2048×1152 | 200 | **672×948** | **1360×948** |
| Connect-S 12.9″ | 1920×1080 | 160 | **628×855** | **1274×855** |
| Connect-WS 24.6″ | 3840×850 | 160 | **800×810** | **1640×810** |
| Connect-WE 24.6″ | 3840×1440 | 160 | **800×1400** | **1640×1400** |
| Connect-WF 18.7″ | 3072×816 | 200 | **648×800** | **1256×800** |

- px = dp × DPI ÷ 160. 160DPI 화면은 dp와 px가 같다. 200DPI 화면은 px가 1.25배다.
  - 예: 14.6″ 세로형 672×948dp → 840×1185px
- **가장 좁은 창: 628dp**(12.9″ 세로형) · **가장 낮은 창: 800dp**(WF 가로·세로)
- **가장 넓은 창: 1700dp**(17″ 가로형) · **가장 높은 창: 1400dp**(WE)
- 가로형은 세로형 폭의 약 2배다. "창 2개를 합친 크기"라는 뜻이다.

### 실제 확인
가상 디스플레이를 만들고 spike 화면을 띄워 실제 창 크기를 쟀다.

| 시험 | 앱이 받은 창 크기 |
|---|---|
| 12.9″ Portrait (628×855, 160dpi) | **628×855dp, portrait** ✅ |
| 12.9″ Landscape (1274×855, 160dpi) | **1274×855dp, landscape** ✅ |

## 기본 에뮬레이터 화면 (실측)
| 영역 | px = dp (160dpi) | 비고 |
|---|---|---|
| 전체 디스플레이 | 2560×1440 | |
| Driving view (왼쪽, 앱이 못 가림) | 853×1300 | |
| **앱 창** | **1682×1210** | 17″ 가로형 프리셋(1700×1184)과 비슷 |
| 상태바 (위) | 높이 90 | |
| GNB (아래) | 높이 140 | |

- `screenOrientation` 지정 여부와 상관없이 1682×1210이었다.

## FormFactorConsole 사용법 (확인 완료)
준비: Go 1.22 이상(설치됨: go1.27.3) + `C:\Users\M3700X27\tools\host-display-bridge-1.0.0\host-display-bridge`

1. 에뮬레이터를 켠다.
2. PowerShell에서 bridge를 실행한다. 창은 닫지 않는다.
   ```
   cd C:\Users\M3700X27\tools\host-display-bridge-1.0.0\host-display-bridge
   go run . --avd "Pleos_Connect_v2.1.0"
   ```
   `listening on http://127.0.0.1:8096`이 나오면 준비 완료다. 처음에는 부품 다운로드 때문에 1~2분 걸린다.
3. 에뮬레이터 아래쪽 GNB에서 **앱 목록(네모 4개 아이콘)** → **FormFactorConsole**을 연다.
   - 위 상태가 `UNAVAILABLE`이면 오른쪽 위 **Refresh**를 누른다. `DISCONNECTED` + "DISPLAY2가 없습니다."가 나오면 준비된 것이다.
4. **Display preset**에서 화면 형태를, **Orientation**에서 `Portrait`/`Landscape`를 고른다.
5. **가상 디스플레이 생성**을 누른다. PC에 새 에뮬레이터 창(`DISPLAY2`)이 뜬다.
6. **앱 목록 실행**을 누르고, 새 창에서 우리 앱을 실행한다.
7. 끝낼 때는 **가상 디스플레이 제거** → bridge 창에서 `Ctrl+C` 순서로 한다.

- 참고: 같은 웹 콘솔(`adb forward tcp:8095 tcp:8095` → `http://127.0.0.1:8095/`)에 CarUxRestrictions 모드를 바꾸는 화면도 있다. 주행 규제 테스트에 쓸 수 있다(미시험).

## 화면 형태 종류 (Display Layout 문서)
| 형태 | 설명 |
|---|---|
| Connect-S (Standard) | 기본 크기. 17", 14.6", 12.9"(이전 Connect-L) |
| Connect-W (Wide) | Connect-S보다 넓음. 주로 고급 차종 (프리셋 WS·WE) |
| Connect-W-Extended | 넓은 화면, 차량 상태에 따라 높이가 늘어남 |
| Connect-WF (Waterfall) | Wide 계열, 해상도는 Wide보다 낮음 |
