# 앱 구조 및 동작 모델 (요약)

원문: https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/app-structure-screen-model
수집일: 2026-10-25 · 원문을 정리한 요약

## 화면 구성 및 멀티 윈도우
- Pleos Connect 화면 = **Driving view 영역** + **앱 영역**
- Driving view는 고정 영역이며 앱이 가리거나 종료할 수 없다
- 앱 영역은 차량·디스플레이 구성에 따라 window 1개 또는 2개

### Portrait / Landscape
- `portrait`: 앱 영역의 window 1개 사용
- `landscape`: 앱 하나가 앱 영역 2개를 확장해 사용
- 두 방향 모두 설계: portrait는 핵심 기능, landscape는 부가 정보·확장 기능 중심

### 앱 실행 및 window 전환
- 새 앱은 현재 표시 중인 앱의 화면 방향 기준으로 실행된다
- 화면에서 사라진 앱은 종료되지 않고 백그라운드에 남았다가 다시 foreground로 올 수 있다
- 사용자가 window bar나 제스처로 위치·크기를 바꿀 수 있으므로 크기가 바뀌어도 핵심 기능이 동작해야 한다

## 화면 방향 및 크기 대응
- `screenOrientation` 미지정 → Activity는 기본 portrait, window 1개
- `screenOrientation="landscape"` → window 2개 영역 사용 가능
- 반응형 레이아웃 사용. 방향은 `AndroidManifest.xml`의 `screenOrientation` 설정 기준

## 포그라운드·백그라운드 전환
- 포그라운드 진입: 상태 복원, 화면 크기·방향에 맞게 UI 재구성
- 백그라운드 전환: 화면 갱신, 애니메이션, 불필요한 센서 수집 등 정리
- 오디오·통신·다운로드처럼 백그라운드에서 계속할 작업은 실행 조건과 중단·재개 정책을 정의
- 백그라운드에서는 상주, 타이머, 폴링, 네트워크 요청 최소화
- 앱 종료·프로세스 재생성에 대비해 사용자 데이터와 진행 상태를 복원할 수 있게 저장
  - 우리 앱 관점: 여행 기록은 백그라운드에서도 계속돼야 하므로 실행 조건·중단 정책 설계가 필요
