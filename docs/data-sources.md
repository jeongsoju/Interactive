# 외부 데이터 출처

## 행정동 경계 (강릉시)
- 파일: `automotive/src/main/assets/dongs_gangneung.json` (21개 동, 약 59KB)
- 원본: 통계청 SGIS(통계지리정보서비스) 행정동 경계를 정리한 공개 저장소 **vuski/admdongkor** (https://github.com/vuski/admdongkor), `ver20261001/HangJeongDong_ver20261001.geojson` (전국 34.7MB, 2026-10-25 내려받음)
- 라이선스: 데이터 **CC BY 4.0**(저장소 설명 기준), 원본 SGIS는 공공누리 1유형(출처 표시). 가공본에도 출처 표시 의무가 이어진다 → **앱 안(설정 탭, 13단계)에 "통계청 SGIS · vuski/admdongkor" 출처를 표시한다.**
- 가공: 강릉시(시군구 코드 51150)만 골라 바깥 고리만 남기고, 약 5m 오차로 점을 줄이고 소수 5자리(약 1m)로 반올림했다. 동마다 경계 안쪽의 대표 지점(lat, lon)을 계산해 넣었다.
- 동 코드: 행정안전부 행정기관코드 10자리(`adm_cd2`, 예: 중앙동 `5115052000`). 이름이 같으면 코드가 유지되는 쪽을 골랐다.
- 알려진 한계: 저장소 안내에 "일부 지역은 실제 경계와 안 맞을 수 있다"고 되어 있고, 점을 줄이면서 이웃한 동 사이에 몇 m의 틈이 생길 수 있다(그 자리는 '경계 밖'). 시연에는 충분하다.
- 변환 도구는 저장소에 넣지 않았다(임시 Go 스크립트). 다시 만들 때는 위 가공 순서를 따른다.

## 지도 (MapLibre + OpenFreeMap)
- 라이브러리: MapLibre Android SDK 13.7.1(BSD-2), 타일·스타일: **OpenFreeMap**(`https://tiles.openfreemap.org/styles/liberty`). 가입·키 없음. 데이터는 OpenStreetMap(ODbL) 기반이고 OpenMapTiles 스키마.
- 출처 표시: "OpenFreeMap © OpenMapTiles, Data from OpenStreetMap". MapLibre가 지도 왼쪽 아래 ⓘ 버튼으로 자동 표시한다. 설정 탭(13단계)의 출처 화면에도 같이 적는다.
- 미리 받은 강릉 지도: `automotive/src/main/assets/mbgl-offline.db` (15.5MB, 2026-10-25). 강릉시 전체 범위(위도 37.503~37.918, 경도 128.580~129.073), 줌 0~14, 타일 860개, 받은 용량 13.8MB. 앱을 처음 켤 때 `files/mbgl-offline.db`로 복사한다(`OfflineMap.prepare`).
- OpenFreeMap 오프라인 조건: 한 번에 타일 3,000개·50MB 이하, 동시 요청 4개 이하, 고유한 User-Agent. 받을 때 이를 지켰다(요청 4개씩, User-Agent `Interactive-contest-app/1.0`).
- **제출 전 확인할 것**: 받은 지도 파일을 앱에 넣어 배포하는 것이 위 "오프라인 다운로드" 허용 범위인지 약관이 명확하지 않다. 제출 전에 OpenFreeMap 운영자에게 문의한다.
- 다시 받는 방법: 앱의 지도 탭에 "강릉 지도 미리 받기" 카드가 뜨는 때(지도 파일이 없을 때)만 보인다. 새로 받으려면 앱 데이터를 지우고 `assets/mbgl-offline.db`를 빼고 설치한 뒤 카드의 버튼을 누르고, 끝나면 `adb exec-out run-as com.example.interactive --user 10 cat files/mbgl-offline.db > automotive/src/main/assets/mbgl-offline.db`로 꺼낸다(에뮬레이터 사용자 번호 10 기준).
