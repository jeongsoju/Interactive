# 주행 규제 개발 가이드 (요약)

원문: https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/driving-restriction-development-guide
수집일: 2026-10-25 · 원문을 정리한 요약 (샘플 코드 전문은 원문 참고)

Pleos Connect 앱은 플랫폼이 주는 `CarUxRestrictions` 상태를 기준으로 주행 중 UX 제한을 적용해야 한다.
- 지역·기능별 제한 범위: [지역별 주행 규제 정책](https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/driving-restiction-policy)
- 운전 중 UX 설계 원칙: [주행 안전을 고려한 설계](https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/driver-distraction-guideline)

## CarUxRestrictions 연동
- AAOS [`CarUxRestrictionsManager`](https://developer.android.com/reference/android/car/drivingstate/CarUxRestrictionsManager)로 [`CarUxRestrictions`](https://developer.android.com/reference/android/car/drivingstate/CarUxRestrictions) 값을 얻는다.
- `registerListener()`로 등록하면 `onUxRestrictionsChanged()` 콜백으로 변경을 받는다.
- DO(Distraction Optimization, 주행 중 사용 최적화) **미선언** 앱의 상황별 사용 가능 여부 → 지역별 주행 규제 정책 문서 참고
- DO를 **선언한** 앱은 주행 규제가 켜지면 `CarUxRestrictions`와 `ActiveRestrictions`를 확인해 기능별 제한을 직접 적용해야 한다.
  - 참고: 현재 우리 매니페스트의 `CarAppActivity`에 `distractionOptimized=true`가 들어 있음(템플릿 기본값)

## 샘플 코드 개요
- 동영상 재생 제한: `isRequiresDistractionOptimization()`이 참이고 `getActiveRestrictions()` 결과에 `UX_RESTRICTIONS_NO_VIDEO` 비트가 있으면 `stopPlayingVideo()`로 재생 중지

## UX 규제 항목과 CarUxRestrictions 매핑
- 기능별 제한은 콜백으로 받은 `CarUxRestrictions`의 `ActiveRestrictions` 기준으로 적용
- 예: 동영상(DMB) → `UX_RESTRICTIONS_NO_VIDEO`
- UX 규제 피그마 사양서는 Pleos Playground에 공개되어 있지 않음
- 플랫폼이 지역 규제에 맞는 `ActiveRestrictions`를 내려주므로 앱은 받은 값을 반영하면 된다. 키보드·검색 등 기능별 허용 여부는 지역별 주행 규제 정책 문서 참고
