# 구글 없이 쓸 수 있는 지도: 후보와 확인 방법

확인일: 2026-10-25

## 에뮬레이터 환경 (실측)
- 구글 서비스(GMS, Play 스토어): **없음** (`gms`, `vending` 패키지 없음)
- 인터넷: **됨** (8.8.8.8 ping 응답 41ms)
- 그래픽: **OpenGL ES 3.1**. 지도 엔진들이 요구하는 GLES 3.0을 만족한다
- WebView: 있음 (`ai.pleos.android.webview`)
- 기본 내비 앱: `ai.umos.maps.android.navigation.app` (외부 앱이 지도를 빌려 쓸 수는 없음)

## 후보 비교
| 후보 | 가입·키 | 비용 | 한국 지도 품질 | GMS 필요 |
|---|---|---|---|---|
| **MapLibre Native Android** | 라이브러리는 필요 없음. 지도 그림(타일) 서버는 따로 필요 | 무료(오픈소스) | 타일에 따라 다름 | 아니오 |
| └ 데모 타일 `demotiles.maplibre.org` | 없음 | 무료 | 나라 경계만 있는 시험용 | |
| └ OpenFreeMap / MapTiler / Stadia 등 | 업체에 따라 가입·키 필요 | 무료 구간 있음 | 도로·지명 있음 | |
| **네이버 지도 SDK** | **네이버 클라우드 플랫폼 가입 + 결제수단 + Client ID** | 무료 사용량 있음, 넘으면 과금 | 매우 좋음 | 아니오(자체 엔진) |
| **카카오맵 SDK** | **카카오 개발자 가입 + 앱 키** | 무료 사용량 있음 | 매우 좋음 | 아니오 |
| WebView + Leaflet + OSM 타일 | 없음 | 무료. 단 OSM 공식 타일은 대량 사용 금지 정책 | 보통 | 아니오 |

- 네이버·카카오는 **가입과 키가 필요**하다. 키는 코드에 직접 쓰지 않고 `local.properties` 같은 곳에 둬야 한다(CLAUDE.md 보안 규칙).
- 공모전 시연용이라면 **MapLibre + 무료 타일**이 가장 부담이 없다. 한국 지명·도로 품질이 중요하면 네이버를 고려한다.

## 에뮬레이터에서 뜨는지 확인하는 방법 (아직 실행 안 함)
### 1단계: MapLibre (가입·키 없음 → 바로 가능)
1. spike 브랜치 `automotive` 모듈에 `org.maplibre.gl:android-sdk` 의존성 추가 (Maven Central, 버전은 추가할 때 최신 안정판 확인)
2. 실험 화면에 `MapView`를 넣고 스타일을 `https://demotiles.maplibre.org/style.json`으로 지정
3. 빌드 → 설치 → `adb shell am start --user 10 -n com.example.interactive/.spike.SpikeMapActivity`
4. `adb exec-out screencap -p`로 캡처해 지도가 그려졌는지 확인
5. 함께 볼 것: 로그에 GL 오류가 없는지, 앱 메모리(`adb shell dumpsys meminfo com.example.interactive`)가 400MB 이하인지
6. 강릉 좌표(37.7519, 128.8961)로 카메라를 옮겨 마커·선(경로) 그리기까지 시험

### 2단계: 네이버 지도 (가입 필요 → 사용자 승인 후)
1. 사용자가 네이버 클라우드 플랫폼에 가입하고 Maps 서비스에서 Application을 등록한다. 패키지 이름은 `com.example.interactive`
2. Client ID를 받아 `local.properties`에 넣는다
3. 네이버 저장소(`https://repository.map.naver.com/archive/maven`)에서 `com.naver.maps:map-sdk` 추가 → 같은 방식으로 캡처 확인

### 판단 기준
- 지도 화면이 1682×1210 창에서 깨지지 않고 그려지는지
- 처음 뜨는 시간(실행 10초 이내 규칙), 메모리(400MB 이하)
- 기어 D로 바꾸면 화면이 규제되므로, 지도 화면은 P에서만 쓰는 설계와 잘 맞는다
